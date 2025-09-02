package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.jobs.Job;
import it.uniroma2.models.sys.ServerStats;
import it.uniroma2.models.sys.StationaryStats;
import it.uniroma2.models.sys.SystemStats;
import it.uniroma2.models.sys.TransientStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SpikedInfrastructureDecorator implements IServerInfrastructure{
    private final BaseServerInfrastructure base;
    private final SpikeServer spikeServer;
    private final List<AbstractServer> allServers;
    SystemStats systemStats;
    TransientStats transientStats;

    public SpikedInfrastructureDecorator(BaseServerInfrastructure base) {
        this.base = base;
        this.spikeServer = new SpikeServer(SPIKE_CAPACITY * this.getNumWebServersByState(ServerState.ACTIVE));

        this.allServers = new ArrayList<>();
        this.allServers.add(this.spikeServer);
        this.allServers.addAll(base.webServers);

        List<ServerStats> serverStats = this.allServers.stream().map(AbstractServer::getStats).toList();
        systemStats = new SystemStats(serverStats);
        transientStats = new TransientStats(serverStats);
    }

    public int getNumWebServersByState(ServerState state) {
        return base.getNumWebServersByState(state);
    }

    public void addJobsData(double endTs, String eventType, Double jobSize) {
        base.addJobsData(endTs, eventType, jobSize);
        INTRA_RUN_DATA.addField(endTs, JOBS_IN_SYSTEM, allServers.stream().mapToInt(AbstractServer::size).sum());
        INTRA_RUN_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(0), spikeServer.size());
        INTRA_RUN_DATA.addField(endTs, SPIKE_CURRENT_CAPACITY, spikeServer.getCapacity());
    }

    public double computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException {
        Integer completionServerIndex = null;
        Double completedJobResponseTime = null;
        Job completedJob;

        if (isCompletion) {
            completionServerIndex = getCompletingServerIndex();
            AbstractServer minServer = allServers.get(completionServerIndex);

            completedJob = minServer.getMinRemainingLifeJob();
            completedJobResponseTime = endTs - completedJob.getArrivalTime();
            // minServer.getStats().updateOnCompletion(completedJobResponseTime);

            INTRA_RUN_DATA.addField(endTs, COMPLETING_SERVER_INDEX, completionServerIndex);
            INTRA_RUN_DATA.addField(endTs, PER_JOB_RESPONSE_TIME, completedJobResponseTime);

            boolean isServerRemoved = minServer.removeJob(completedJob);
            if (isServerRemoved && completionServerIndex != 0) // spike server can't be removed
                INTRA_RUN_DATA.addField(endTs, EVENT_TYPE_SCALING, ServerState.REMOVED);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < allServers.size(); currIndex++) {
            AbstractServer server = allServers.get(currIndex);
            server.computeJobsAdvancement(
                    startTs, endTs,
                    Objects.equals(currIndex, completionServerIndex) ? completedJobResponseTime : null);
        }

        /* Compute the moving exponential average of the response time */
        if (isCompletion) {
            base.addStateToScalingData(endTs);
            INTRA_RUN_DATA.addField(endTs, R_0, completedJobResponseTime);
            INTRA_RUN_DATA.addField(endTs, SCALING_INDICATOR, base.scalingIndicator);
            this.systemStats.updateStationaryStats(endTs);
        }

        this.updateScalingIndicator();

        this.transientStats.updateStats(endTs, completionServerIndex, completedJobResponseTime);

        return base.scalingIndicator;
    }

    int getCompletingServerIndex() {
        IServer minServer = null;
        double lifeRemaining, minRemainingLife = INFINITY;

        for (AbstractServer server : allServers) {
            Job j = server.getMinRemainingLifeJob();
            if (j != null) {
                lifeRemaining = j.getRemainingLife() * server.size() / server.getCapacity();
                if (lifeRemaining < minRemainingLife) {
                    minRemainingLife = lifeRemaining;
                    minServer = server;
                }
            }
        }

        assert minServer != null;
        return allServers.indexOf(minServer);
    }

    public void assignJob(Job job) {
        if (base.webServersSize() >= SI_MAX * getNumWebServersByState(ServerState.ACTIVE) ) {
            spikeServer.addJob(job);
        } else {
            base.assignJob(job);
        }
    }

    public boolean activeJobExists() {
        return base.activeJobExists() || spikeServer.activeJobExists();
    }

    public double computeNextCompletionTs(double endTs) {
        double spikeServerMinRemainingLife = spikeServer.activeJobExists() ?
                endTs + spikeServer.getMinRemainingLife() * spikeServer.size() / spikeServer.getCapacity()
                : INFINITY;
        return Math.min(spikeServerMinRemainingLife, base.computeNextCompletionTs(endTs));
    }

    public boolean isCompletedStationaryStats(){
        boolean isServerStationaryStatsCompleted =
                this.allServers.stream().map(AbstractServer::getStats)
                        .map(ServerStats::getStationaryStats)
                        .map(StationaryStats::isCompleted)
                        .reduce(true, (a, b) -> a && b);
        boolean isSystemStationaryStatsCompleted = this.systemStats.getStationaryStats().isCompleted();
        return isSystemStationaryStatsCompleted && isServerStationaryStatsCompleted;
    }

    public void printServerStats(double currentTs) {
        System.out.print("\n\nSpikeServer : ");
        this.spikeServer.printServerStats(currentTs);

        base.printServerStats(currentTs);
    }

    public void printSystemStats(double currentTs) {
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SPIKE_JOBS_COMPLETED, spikeServer.getStats().getCompletedJobs());
        systemStats.processStats(currentTs);
        systemStats.getStationaryStats().printIntervalEstimation();
    }

    public WebServer requestScaleOut(double endTs, double turnOnTime) {
        return base.requestScaleOut(endTs, turnOnTime);
    }

    public WebServer findNextScaleOut() {
        return base.findNextScaleOut();
    }

    public void scaleIn(double endTs) {
        base.scaleIn(endTs);
        this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumWebServersByState(ServerState.ACTIVE));
    }

    public void scaleOut(double endTs, WebServer targetWebServer) {
        base.scaleOut(endTs, targetWebServer);
        this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumWebServersByState(ServerState.ACTIVE));
    }

    void updateScalingIndicator() {
        base.scalingIndicator = allServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
                .map(AbstractServer::size)
                .reduce(0, Integer::sum);
    }

}

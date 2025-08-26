package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.ServerStats;
import it.uniroma2.models.sys.SystemStats;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SpikedInfrastructureDecorator implements IServerInfrastructure{
    private final BaseServerInfrastructure base;
    private final SpikeServer spikeServer;
    private final List<AbstractServer> allServers;
    SystemStats systemStats;

    public SpikedInfrastructureDecorator(BaseServerInfrastructure base) {
        this.base = base;
        this.spikeServer = new SpikeServer(SPIKE_CAPACITY * this.getNumWebServersByState(ServerState.ACTIVE));

        this.allServers = new ArrayList<>();
        this.allServers.add(this.spikeServer);
        this.allServers.addAll(base.webServers);
        systemStats = new SystemStats(this.allServers.stream().map(AbstractServer::getStats).toList());
    }

    public int getNumWebServersByState(ServerState state) {
        return base.getNumWebServersByState(state);
    }

    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? this.getCompletingServerIndex() : -1;

        Job removedJob = null;
        if (completionServerIndex != -1) {
            AbstractServer minServer = allServers.get(completionServerIndex);
            removedJob = minServer.getMinRemainingLifeJob();

            double jobResponseTime = endTs - removedJob.getArrivalTime();
            minServer.getStats().updateSLO(jobResponseTime);

            INTRA_RUN_DATA.addField(endTs, COMPLETING_SERVER_INDEX, completionServerIndex);
            INTRA_RUN_DATA.addField(endTs, PER_JOB_RESPONSE_TIME, jobResponseTime);

            boolean isServerRemoved = minServer.removeJob(removedJob);
            if (isServerRemoved)
                INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, ServerState.REMOVED);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < allServers.size(); currIndex++) {
            AbstractServer server = allServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex == completionServerIndex ? 1 : 0);
        }

        /* Compute the moving exponential average of the response time */
        if (completionServerIndex != -1) {
            assert removedJob != null;
            double lastResponseTime = endTs - removedJob.getArrivalTime();
            base.updateMovingExpResponseTime(lastResponseTime);

            base.addStateToScalingData(endTs);
            INTRA_RUN_DATA.addField(endTs, R_0, lastResponseTime);
            INTRA_RUN_DATA.addField(endTs, MOVING_R_O, base.movingExpMeanResponseTime);
        }

        return base.movingExpMeanResponseTime;
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

    public void printServerStats(double currentTs) {
        System.out.print("\n\nSpikeServer : ");
        this.spikeServer.printServerStats(currentTs);

        base.printServerStats(currentTs);
    }

    public void printSystemStats(double currentTs) {
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SPIKE_JOBS_COMPLETED, spikeServer.getStats().getCompletedJobs());
        systemStats.processStats(currentTs);
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

    public void logFineJobs(double endTs, String eventType) {
        base.logFineJobs(endTs, eventType);
        INTRA_RUN_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(0), spikeServer.size());
        INTRA_RUN_DATA.addField(endTs, SPIKE_CURRENT_CAPACITY, spikeServer.getCapacity());
    }
}

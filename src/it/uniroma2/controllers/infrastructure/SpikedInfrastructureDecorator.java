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
import static it.uniroma2.utils.DataCSVWriter.CSV_DATA;
import static it.uniroma2.utils.DataField.*;

public class SpikedInfrastructureDecorator implements IServerInfrastructure{
    private final BaseServerInfrastructure base;
    private final SpikeServer spikeServer;
    private final List<AbstractServer> allServers;

    public SpikedInfrastructureDecorator(BaseServerInfrastructure base) {
        this.base = base;
        this.spikeServer = new SpikeServer(SPIKE_CAPACITY * this.getNumWebServersByState(ServerState.ACTIVE));

        this.allServers = new ArrayList<>();
        this.allServers.add(this.spikeServer);
        this.allServers.addAll(base.webServers);
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
            boolean isServerRemoved = minServer.removeJob(removedJob);
            if (isServerRemoved)
                CSV_DATA.addField(endTs, EVENT_TYPE, ServerState.REMOVED);
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
            CSV_DATA.addField(endTs, R_0, lastResponseTime);
            CSV_DATA.addField(endTs, MOVING_R_O, base.movingExpMeanResponseTime);
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
        if (base.webServersSize() >= SI_MAX ) {
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

    public void printServerStats(DecimalFormat f, double currentTs) {
        System.out.print("\n\nSpikeServer : ");
        this.spikeServer.printServerStats(f, currentTs);

        base.printServerStats(f, currentTs);
    }

    public void printSystemStats(DecimalFormat f, double currentTs) {
        List<ServerStats> serverStats = this.allServers.stream()
                .map(AbstractServer::getStats)
                .toList();

        SystemStats sysStats = new SystemStats(serverStats);
        sysStats.processStats(f, currentTs);
    }

    public WebServer requestScaleOut(double endTs) {
        return base.requestScaleOut(endTs);
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
        CSV_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(0), spikeServer.size());
        CSV_DATA.addField(endTs, SPIKE_CURRENT_CAPACITY, spikeServer.getCapacity());
    }
}

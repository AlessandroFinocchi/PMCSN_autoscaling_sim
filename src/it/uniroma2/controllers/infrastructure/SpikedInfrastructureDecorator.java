package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.JOBS_DATA;
import static it.uniroma2.utils.DataCSVWriter.SCALING_DATA;
import static it.uniroma2.utils.DataField.*;

public class SpikedInfrastructureDecorator extends AbstractServerInfrastructure {
    private final AbstractServerInfrastructure base;
    private SpikeServer spikeServer;
    private final List<AbstractServer> allServers;

    public SpikedInfrastructureDecorator(AbstractServerInfrastructure base) {
        super();
        this.base = base;

        this.spikeServer = new SpikeServer(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));
        this.allServers = new ArrayList<>();
        this.allServers.add(this.spikeServer);
        this.allServers.addAll(this.webServers);
    }

    public void assignJob(Job job) {
        if (base.webServersSize() >= SI_MAX) {
            spikeServer.addJob(job);
            return;
        }
        base.assignJob(job);
    }

    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? getCompletingServerIndex() : -1;

        Job removedJob = null;
        if (completionServerIndex != -1) {
            AbstractServer minServer = allServers.get(completionServerIndex);
            removedJob = minServer.getMinRemainingLifeJob();
            boolean isServerRemoved = minServer.removeJob(removedJob);
            if (isServerRemoved)
                SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.REMOVED);
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
            this.updateMovingExpResponseTime(lastResponseTime);

            addStateToScalingData(endTs);
            SCALING_DATA.addField(endTs, R_0, lastResponseTime);
            SCALING_DATA.addField(endTs, MOVING_R_O, this.movingExpMeanResponseTime);
        }

        return this.movingExpMeanResponseTime;
    }

    public int getCompletingServerIndex() {
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

    public int webServersSize() {
        return base.webServersSize() + spikeServer.size();
    }

    public boolean activeJobExists() {
        return this.spikeServer.activeJobExists() || base.activeJobExists();
    }

    public double computeNextCompletionTs(double endTs) {
        double spikeServerMinRemainingLife = spikeServer.getMinRemainingLife() * spikeServer.size() / spikeServer.getCapacity();

        return Math.min(spikeServerMinRemainingLife, base.computeNextCompletionTs(endTs));
    }

    public void printStats(double currentTs) {
        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");
        System.out.print("\nSpikeServer : ");
        this.spikeServer.printStats(f, currentTs);

        base.printStats(currentTs);
    }

    public void scaleIn(double endTs) {
        base.scaleIn(endTs);
        this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));
    }

    public void scaleOut(double endTs, WebServer targetWebServer) {
        base.scaleOut(endTs, targetWebServer);
        this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));
    }

    public void logFineJobs(double endTs, String eventType) {
        base.logFineJobs(endTs, eventType);
        JOBS_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(0), spikeServer.size());
        JOBS_DATA.addField(endTs, SPIKE_CURRENT_CAPACITY, spikeServer.getCapacity());
    }



}

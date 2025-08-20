package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.AbstractServer;
import it.uniroma2.controllers.servers.IServer;
import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.WebServer;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;

import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.utils.DataCSVWriter.JOBS_DATA;
import static it.uniroma2.utils.DataCSVWriter.SCALING_DATA;
import static it.uniroma2.utils.DataField.*;

public class BaseServerInfrastructure extends AbstractServerInfrastructure{

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    public void assignJob(Job job) {
        WebServer target = this.scheduler.select(this.webServers);
        target.addJob(job);
    }

    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? getCompletingServerIndex() : -1;

        Job removedJob = null;
        if (completionServerIndex != -1) {
            AbstractServer minServer = webServers.get(completionServerIndex);
            removedJob = minServer.getMinRemainingLifeJob();
            boolean isServerRemoved = minServer.removeJob(removedJob);
            if (isServerRemoved)
                SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.REMOVED);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            AbstractServer server = webServers.get(currIndex);
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

    private int getCompletingServerIndex() {
        IServer minServer = null;
        double lifeRemaining, minRemainingLife = INFINITY;

        for (WebServer server : webServers) {
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
        return webServers.indexOf(minServer);
    }

    public boolean activeJobExists() {
        for (IServer server : webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

    public double computeNextCompletionTs(double endTs) {
        double currRemainingLife, minRemainingLife = INFINITY;

        for (AbstractServer server : webServers.stream().filter(server -> server.size() != 0).toList()) {
            currRemainingLife = server.getMinRemainingLife() * server.size() / server.getCapacity();
            if (currRemainingLife < minRemainingLife) {
                minRemainingLife = currRemainingLife;
            }
        }

        return endTs + minRemainingLife;
    }

    public void printStats(double currentTs) {
        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");

        for (WebServer server : webServers) {
            System.out.printf("\nWebServer %d: ", webServers.indexOf(server) + 1);
            server.printStats(f, currentTs);
        }
    }

    public void scaleIn(double endTs) {
        WebServer minServer = findScaleInTarget();

        /* If found server, make it to be removed */
        if (minServer != null) {
            minServer.setServerState(ServerState.TO_BE_REMOVED);

            SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.TO_BE_REMOVED);
            addStateToScalingData(endTs);
        }

        /* If no server is found, only 1 server is active */
        else System.out.println("No active servers found!");
    }

    public void scaleOut(double endTs, WebServer targetWebServer) {
        targetWebServer.setServerState(ServerState.ACTIVE);
        SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.ACTIVE);
        addStateToScalingData(endTs);
    }

    public void logFineJobs(double endTs, String eventType) {
        int i = 1;
        JOBS_DATA.addField(endTs, EVENT_TYPE, eventType);
        for (WebServer server : webServers) {
            JOBS_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(i), server.size());
            JOBS_DATA.addFieldWithSuffix(endTs, STATUS_OF_SERVER, String.valueOf(i), server.getServerState().toString());
            i++;
        }
    }
}

package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.utils.DataCSVWriter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.*;

public class ServerInfrastructure {
    private int nextAssigningServer;
    private List<WebServer> webServers;
    private double movingExpMeanResponseTime;

    public ServerInfrastructure() {
        this.nextAssigningServer = 0;
        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i++) {
            var serverState = i < START_NUM_SERVERS ? ServerState.ACTIVE : ServerState.REMOVED;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, serverState));
        }
    }

    public String[] getServerStatusInfo(double endTs, ServerState state) {
        String[] status = new String[ServerState.values().length + 2];
        int active = 0, toBeRemoved = 0, removed = 0;
        for (WebServer webServer : webServers) {
            switch (webServer.getServerState()) {
                case ACTIVE -> active++;
                case TO_BE_REMOVED -> toBeRemoved++;
                case REMOVED -> removed++;
            }
        }
        status[0] = String.valueOf(endTs);
        status[1] = String.valueOf(active);
        status[2] = String.valueOf(toBeRemoved);
        status[3] = String.valueOf(removed);
        status[4] = String.valueOf(state);
        return status;
    }

    /**
     * Processes the job advancement in their execution
     * @param startTs the computation interval start
     * @param endTs the computation interval end
     * @param completed if the advancement is a completion or not
     * @return the medium response time of the webservers
     */
    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? getCompletingServerIndex() : -1;
        Job removedJob = null;
        if (completionServerIndex != -1) {
            WebServer minServer = webServers.get(completionServerIndex);
            removedJob = minServer.getMinRemainingLifeJob();
            minServer.removeJob(removedJob);
            if (!minServer.activeJobExists() && minServer.getServerState() == ServerState.TO_BE_REMOVED) {
                minServer.setServerState(ServerState.REMOVED);
                DataCSVWriter.SERVERS.addArrayData(getServerStatusInfo(endTs, ServerState.REMOVED));
            }

        }

        /* Compute the advancement of each job in each Server */
        for (int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            WebServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex == completionServerIndex ? 1 : 0);
        }

        /* Compute the moving exponential average of the response time */
        if (completionServerIndex != -1) {
            assert removedJob != null;
            double lastResponseTime = endTs - removedJob.getArrivalTime();
            DataCSVWriter.R0.addData(endTs, lastResponseTime);
            this.updateMovingExpResponseTime(lastResponseTime);
            DataCSVWriter.MOVING_R0.addData(endTs, this.movingExpMeanResponseTime);
        }

        return this.movingExpMeanResponseTime;
    }

    /**
     * Removes the job in the server farm with minimum life
     * @return the index of the server with the job removed
     */
    public int getCompletingServerIndex() {
        IServer minServer = webServers.get(0);
        double lifeRemaining, minRemainingLife = INFINITY;

        for (IServer server : webServers) {
            Job j = server.getMinRemainingLifeJob();
            if (j != null) {
                lifeRemaining = j.getRemainingLife() * server.size();
                if (lifeRemaining < minRemainingLife) {
                    minRemainingLife = lifeRemaining;
                    minServer = server;
                }
            }
        }

        return webServers.indexOf(minServer);
    }

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    public void assignJob(Job job) {
        WebServer leastUsedServer = webServers
                .stream()
                .filter(server -> server.getServerState() == ServerState.ACTIVE)
                .min(Comparator.comparingDouble(WebServer::size))
                .stream().toList().get(0);

        leastUsedServer.addJob(job);

//        for(int currIndex, i = 0; i < webServers.size(); i++) {
//            currIndex = (nextAssigningServer + i) % webServers.size();
//            WebServer server = webServers.get(currIndex);
//            if (server.getServerState() == ServerState.ACTIVE) {
//                server.addJob(job);
//                nextAssigningServer = (currIndex + 1) % webServers.size();
//                return;
//            }
//        }
//
//        throw new RuntimeException("No active server found");
    }

    /**
     * Checks weather an active job exists
     * @return if an active job exists
     */
    public boolean activeJobExists() {
        for (IServer server : webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

    /**
     * Computes when the next completion will happen
     * @param endTs the starting point from which computing the completion time
     * @return the completion time
     */
    public double computeNextCompletionTs(double endTs) {
        double currRemainingLife, minRemainingLife = INFINITY;

        for (AbstractServer server : webServers.stream().filter(webServer -> webServer.size() != 0).toList()) {
            currRemainingLife = server.getMinRemainingLife() * server.size() / server.capacity;
            if (currRemainingLife < minRemainingLife) {
                minRemainingLife = currRemainingLife;
            }
        }

        return endTs + minRemainingLife;
    }

    public void printStats(double currentTs) {
        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");

        for (IServer server : webServers) {
            server.printStats(f, currentTs);
        }
    }

    public WebServer findScaleOutTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active but to be removed
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_REMOVED)
//                .max(Comparator.comparing(WebServer::getCapacity))
                .min(Comparator.comparingDouble(w -> webServers.indexOf(w)))
                .orElse(null);

        // If no servers are active but to be removed, look for a removed one
        if (targetWebServer == null) {
            targetWebServer = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.REMOVED)
//                    .max(Comparator.comparing(WebServer::getCapacity))
                    .min(Comparator.comparingDouble(w -> webServers.indexOf(w)))
                    .orElse(null);
        }

        return targetWebServer;
    }

    public WebServer findScaleInTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
//                .min(Comparator.comparingDouble(WebServer::getRemainingServerLife))
                .max(Comparator.comparingDouble(w -> webServers.indexOf(w)))
                .orElse(null);

        return targetWebServer;
    }

    public void scaleOut(double endTs) {
        WebServer targetWebServer = findScaleOutTarget();

        /* If found server, make it active */
        if (targetWebServer != null) targetWebServer.setServerState(ServerState.ACTIVE);

            /* If no server is found, all servers are active */
        else System.out.println("All servers are active");

        DataCSVWriter.SERVERS.addArrayData(getServerStatusInfo(endTs, ServerState.ACTIVE));
    }

    public void scaleIn(double endTs) {
        WebServer minServer = findScaleInTarget();

        /* If found server, make it to be removed */
        if (minServer != null) minServer.setServerState(ServerState.TO_BE_REMOVED);

            /* If no server is found, all servers are active */
        else System.out.println("No active servers found!");

        DataCSVWriter.SERVERS.addArrayData(getServerStatusInfo(endTs, ServerState.TO_BE_REMOVED));
    }

    public void updateMovingExpResponseTime(double lastResponseTime) {
        this.movingExpMeanResponseTime = this.movingExpMeanResponseTime * ALPHA +
                lastResponseTime * (1 - ALPHA);
    }

    public int numServerActive() {
        return webServers
                .stream()
                .filter(w -> w.getServerState() == ServerState.ACTIVE)
                .toList()
                .size();
    }
}

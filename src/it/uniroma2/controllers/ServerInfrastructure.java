package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemStats;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.JOBS_DATA;
import static it.uniroma2.utils.DataCSVWriter.SCALING_DATA;
import static it.uniroma2.utils.DataField.*;

public class ServerInfrastructure {
    private int nextAssigningServer; // todo: refactor into a scheduler class
    private final List<WebServer> webServers;
    private SpikeServer spikeServer = null;
    private final List<AbstractServer> allServers;
    private double movingExpMeanResponseTime;
    private SystemStats stats;

    public ServerInfrastructure() {
        this.nextAssigningServer = 0;
        this.allServers = new ArrayList<>();
        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i++) {
            var serverState = i < START_NUM_SERVERS ? ServerState.ACTIVE : ServerState.REMOVED;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, serverState));
        }
        if(SPIKESERVER_ACTIVE) {
            this.spikeServer = new SpikeServer(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));
            this.allServers.add(this.spikeServer);
        }
        this.allServers.addAll(webServers);
    }

    /**
     * Returns the number of Web Servers in a given state
     * @param state the state of the web servers
     * @return the number of web servers in the state 'state'
     */
    public int getNumServersByState(ServerState state) {
        return (int) webServers.stream().filter(server -> server.getServerState() == state).count();
    }

    /**
     * Utils method to quickly add all possible states to the scaling Data Table
     */
    private void addStateToScalingData(double endTs) {
        SCALING_DATA.addField(endTs, TO_BE_ACTIVE, getNumServersByState(ServerState.TO_BE_ACTIVE));
        SCALING_DATA.addField(endTs, ACTIVE, getNumServersByState(ServerState.ACTIVE));
        SCALING_DATA.addField(endTs, TO_BE_REMOVED, getNumServersByState(ServerState.TO_BE_REMOVED));
        SCALING_DATA.addField(endTs, REMOVED, getNumServersByState(ServerState.REMOVED));
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

    /**
     * Removes the job in the server farm with minimum life
     * @return the index of the server with the job removed
     */
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

    /**
     * Get the total number of active jobs in all the servers
     * @return the total number of active jobs in all the servers
     */
    public int webServersSize() {
        int size = 0;
        for (WebServer server : webServers) {
            size += server.size();
        }
        return size;
    }

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    public void assignJob(Job job) {
        AbstractServer target;
        if (!SPIKESERVER_ACTIVE || this.webServersSize() < SI_MAX ) {
            /* Find the least used Web Server */
            target = webServers
                    .stream()
                    .filter(server -> server.getServerState() == ServerState.ACTIVE)
                    .min(Comparator.comparingDouble(WebServer::size))
                    .stream().toList().get(0);
        } else {
            target = spikeServer;
        }

        target.addJob(job);

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
        for (IServer server : allServers) {
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

        for (AbstractServer server : allServers.stream().filter(server -> server.size() != 0).toList()) {
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

        if(SPIKESERVER_ACTIVE) {
            System.out.print("\nSpikeServer : ");
            this.spikeServer.printStats(f, currentTs);
        }

        for (IServer server : webServers) {
            System.out.printf("\nWebServer %d: ", allServers.indexOf(server) + 1);
            server.printStats(f, currentTs);
        }
    }

    public WebServer findScaleOutTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active but to be removed
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_REMOVED)
                .min(Comparator.comparingDouble(webServers::indexOf))
                .orElse(null);

        // If no servers are active but to be removed, look for a removed one
        if (targetWebServer == null) {
            targetWebServer = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.REMOVED)
                    .min(Comparator.comparingDouble(webServers::indexOf))
                    .orElse(null);
        }

        return targetWebServer;
    }

    public WebServer findScaleInTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_ACTIVE)
                .max(Comparator.comparingDouble(webServers::indexOf))
                .orElse(null);

        // If no servers are to be active, look for an active one
        if (targetWebServer == null) {
            targetWebServer = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
                    .max(Comparator.comparingDouble(webServers::indexOf))
                    .orElse(null);
        }

        return targetWebServer;
    }

    public WebServer requestScaleOut(double endTs) {
        WebServer targetWebServer = findScaleOutTarget();

        /* If found server, make it active */
        if (targetWebServer != null) {
            targetWebServer.setServerState(ServerState.TO_BE_ACTIVE);
            targetWebServer.setActivationTimestamp(endTs + 1); // #TODO: change

            SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.TO_BE_ACTIVE);
            addStateToScalingData(endTs);

            return targetWebServer;
        }

        /* If no server is found, all servers are active */
        else System.out.println("All servers are active");

        return null;
    }

    public WebServer findNextScaleOut() {
        return webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_ACTIVE)
                .min(Comparator.comparingDouble(WebServer::getActivationTimestamp))
                .orElse(null);
    }

    public void scaleIn(double endTs) {
        WebServer minServer = findScaleInTarget();

        /* If found server, make it to be removed */
        if (minServer != null) {
            minServer.setServerState(ServerState.TO_BE_REMOVED);

            SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.TO_BE_REMOVED);
            addStateToScalingData(endTs);
        }

        if(SPIKESERVER_ACTIVE) // todo: apply decorator for deleting these ifs
            this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));

        /* If no server is found, only 1 server is active */
        else System.out.println("No active servers found!");
    }

    public void scaleOut(double endTs, WebServer targetWebServer) {
        targetWebServer.setServerState(ServerState.ACTIVE);
        SCALING_DATA.addField(endTs, EVENT_TYPE, ServerState.ACTIVE);
        addStateToScalingData(endTs);
        if(SPIKESERVER_ACTIVE) // todo: apply decorator for deleting these ifs
            this.spikeServer.setCapacity(SPIKE_CAPACITY * this.getNumServersByState(ServerState.ACTIVE));
    }

    public void updateMovingExpResponseTime(double lastResponseTime) {
        this.movingExpMeanResponseTime = this.movingExpMeanResponseTime * ALPHA +
                lastResponseTime * (1 - ALPHA);
    }

    public void logFineJobs(double endTs, String eventType) {
        int i = 1;
        JOBS_DATA.addField(endTs, EVENT_TYPE, eventType);
        for (WebServer server : webServers) {
            JOBS_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(i), server.size());
            JOBS_DATA.addFieldWithSuffix(endTs, STATUS_OF_SERVER, String.valueOf(i), server.getServerState().toString());
            i++;
        }
        if(SPIKESERVER_ACTIVE) {
            JOBS_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(0), spikeServer.size());
            JOBS_DATA.addField(endTs, SPIKE_CURRENT_CAPACITY, spikeServer.getCapacity());
        }
    }
}

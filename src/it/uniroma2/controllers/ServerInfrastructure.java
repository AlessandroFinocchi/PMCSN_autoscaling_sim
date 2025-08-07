package it.uniroma2.controllers;

import it.uniroma2.models.Job;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.models.Config.WEBSERVER_CAPACITY;

public class ServerInfrastructure {
    private int lastAssignedServer;
    private List<WebServer> webServers;

    public ServerInfrastructure() {
        this.lastAssignedServer = 0;
        this.webServers = new ArrayList<>();
        this.webServers.add(new WebServer(WEBSERVER_CAPACITY));
    }

    /**
     * Processes the job advancement in their execution
     * @param startTs the computation interval start
     * @param endTs the computation interval end
     * @param completed if the advancement is a completion or not
     */
    public void computeJobsAdvancement(double startTs, double endTs, int completed) {
        int completionServerIndex = completed == 1 ?
                removeMinRemainingLifeJob() : -1;

        /* Compute the advancement of each job in each Server */
        for(int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            WebServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex == completionServerIndex ? 1 : 0);
        }
    }

    /**
     * Removes the job in the server farm with minimum life
     * @return the index of the server with the job removed
     */
    public int removeMinRemainingLifeJob() {
        IServer minServer = webServers.get(0);
        Job minJob = new Job(0, INFINITY);

        for(IServer server: webServers) {
            Job j = server.getMinRemainingLifeJob();
            if(j != null && j.getRemainingLife() < minJob.getRemainingLife()) {
                minServer = server;
                minJob = j;
            }
        }

        minServer.removeJob(minJob);
        return webServers.indexOf(minServer);
    }

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    public void assignJob(Job job) {
        webServers.get(lastAssignedServer).addJob(job);
        lastAssignedServer = (lastAssignedServer + 1) % webServers.size();
    }

    /**
     * Checks weather an active job exists
     * @return if an active job exists
     */
    public boolean activeJobExists() {
        for(IServer server: webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

    /**
     * Computes when the next completion will happen
     * @param endTs the starting point from which computing the completion timee
     * @return the completion time
     */
    public double computeNextCompletionTs(double endTs) {
        double currLife, minRemainingLife = INFINITY;
        AbstractServer minServer = webServers.get(0);

        for(AbstractServer server: webServers) {
            currLife = server.getMinRemainingLife();
            if(currLife < minRemainingLife) {
                minRemainingLife = currLife;
                minServer = server;
            }
        }

        return endTs + minRemainingLife / (minServer.getCapacity() / minServer.size());
    }

    public void printStats(double currentTs) {
        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");

        for(IServer server: webServers) {
            server.printStats(f,  currentTs);
        }
    }
}

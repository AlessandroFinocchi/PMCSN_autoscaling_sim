package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.models.Config.WEBSERVER_CAPACITY;

public class ServerInfrastructure {
    private int nextAssigningServer;
    private List<WebServer> webServers;

    public ServerInfrastructure() {
        this.nextAssigningServer = 0;
        this.webServers = new ArrayList<>();
        this.webServers.add(new WebServer(WEBSERVER_CAPACITY));
        this.webServers.add(new WebServer(WEBSERVER_CAPACITY));
    }

    /**
     * Processes the job advancement in their execution
     * @param startTs the computation interval start
     * @param endTs the computation interval end
     * @param completed if the advancement is a completion or not
     */
    public void computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
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
        double lifeRemaining, minRemainingLife = INFINITY;

        for(IServer server: webServers) {
            Job j = server.getMinRemainingLifeJob();
            if(j != null) {
                lifeRemaining = j.getRemainingLife() * server.size();
                if(lifeRemaining < minRemainingLife) {
                    minRemainingLife = lifeRemaining;
                    minServer = server;
                }
            }
        }

        minServer.removeJob(minServer.getMinRemainingLifeJob());
        return webServers.indexOf(minServer);
    }

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    public void assignJob(Job job) {
        webServers.get(nextAssigningServer).addJob(job);
        nextAssigningServer = (nextAssigningServer + 1) % webServers.size();
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
        double currRemainingLife, minRemainingLife = INFINITY;

        for(AbstractServer server: webServers.stream().filter(webServer -> webServer.size() != 0).toList()) {
            currRemainingLife = server.getMinRemainingLife() * server.size() / server.capacity;
            if(currRemainingLife < minRemainingLife) {
                minRemainingLife = currRemainingLife;
            }
        }

        return endTs + minRemainingLife;
    }

    public void printStats(double currentTs) {
        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");

        for(IServer server: webServers) {
            server.printStats(f,  currentTs);
        }
    }
}

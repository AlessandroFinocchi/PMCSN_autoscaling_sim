package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;
import java.util.*;

import static it.uniroma2.models.Config.*;

public class ServerInfrastructure {
    private int nextAssigningServer;
    private List<WebServer> webServers;

    public ServerInfrastructure() {
        this.nextAssigningServer = 0;
        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i ++) {
            var isActive = i < START_NUM_SERVERS;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, isActive));
        }
    }

    /**
     * Processes the job advancement in their execution
     * @param startTs the computation interval start
     * @param endTs the computation interval end
     * @param completed if the advancement is a completion or not
     * @return the medium response time of the webservers
     */
    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? removeMinRemainingLifeJob() : -1;

        /* Compute the advancement of each job in each Server */
        for(int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            WebServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex == completionServerIndex ? 1 : 0);
        }

        /* Compute the (weighted) mean response time */ //todo: ha senso pesare così?
        double meanResponseTime = 0.0f;
        double totalCapacity = 0.0f;
        var activeServer = webServers.stream().filter(WebServer::isActive).toList();
        if(completed == 1) {
            for(WebServer server : activeServer) {
                meanResponseTime += server.getResponseTime() * server.getCapacity();
                totalCapacity += server.getCapacity();
            }
        }
        return meanResponseTime / totalCapacity;
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
        for(int currIndex, i = 0; i < webServers.size(); i++) {
            currIndex = (nextAssigningServer + i) % webServers.size();
            WebServer server = webServers.get(currIndex);
            if (!server.isToBeRemoved() && server.isActive()) {
                server.addJob(job);
                nextAssigningServer = (currIndex + 1) % webServers.size();
                return;
            }
        }
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
     * @param endTs the starting point from which computing the completion time
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

    public void scaleOut() {
        // Search if there is a server still active but to be removed
        var removingWebServer = webServers.stream()
                .filter(WebServer::isToBeRemoved)
                .filter(WebServer::isActive)
                .max(Comparator.comparing(WebServer::getCapacity))
                .orElse(null);

        if(removingWebServer != null) {
            removingWebServer.setToBeRemoved(false);
        } else {
            // If there aren't server still active but to be removed power on a server
            webServers.stream()
                    .filter(ws -> !ws.isActive())
                    .max(Comparator.comparing(WebServer::getCapacity))
                    .ifPresent(unactiveWebServer -> {
                        unactiveWebServer.setActive(true);
                        unactiveWebServer.setToBeRemoved(false);
                    });
        }
    }

    public void scaleIn() {
        WebServer minServer = webServers.stream()
                        .filter(WebServer::isActive)
                        .min(Comparator.comparingDouble(WebServer::getRemainingServerLife))
                        .get();

        if (!minServer.isToBeRemoved()) {
            if (minServer.activeJobExists()) {
                minServer.setToBeRemoved(true);
            } else {
                minServer.setActive(false);
            }
        }
    }
}

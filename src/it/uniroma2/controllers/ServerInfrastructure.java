package it.uniroma2.controllers;

import it.uniroma2.models.Job;

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

    public void computeJobsAdvancement(double startTs, double endTs, int completed) {
        int completionServerIndex = completed == 1 ?
                removeMinRemainingLifeJob() : -1;

        /* Compute the advancement of each job in each Server */
        for(int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            WebServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex = completionServerIndex);
        }
    }

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

    public void assignJob(Job job) {
        webServers.get(lastAssignedServer).addJob(job);
    }

    public boolean activeJobExists() {
        for(IServer server: webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

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
}

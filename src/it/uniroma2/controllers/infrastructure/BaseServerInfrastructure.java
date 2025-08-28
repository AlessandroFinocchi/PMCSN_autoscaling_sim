package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.scheduler.IScheduler;
import it.uniroma2.controllers.scheduler.SchedulerFactory;
import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.ServerStats;
import it.uniroma2.models.sys.SystemStats;
import it.uniroma2.models.sys.TransientStats;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class BaseServerInfrastructure implements IServerInfrastructure {
    final IScheduler scheduler;
    final List<WebServer> webServers;
    double movingExpMeanResponseTime;
    SystemStats systemStats;
    TransientStats transientStats;

    public BaseServerInfrastructure() {
        this.scheduler = SchedulerFactory.create();
        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i++) {
            var serverState = i < START_NUM_SERVERS ? ServerState.ACTIVE : ServerState.REMOVED;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, serverState, i+1));
        }

        List<ServerStats> serverStats = this.webServers.stream().map(AbstractServer::getStats).toList();
        systemStats = new SystemStats(serverStats);
        transientStats = new TransientStats(serverStats);
    }

    public int getNumWebServersByState(ServerState state) {
        return (int) webServers.stream().filter(server -> server.getServerState() == state).count();
    }

    void addStateToScalingData(double endTs) {
        INTRA_RUN_DATA.addField(endTs, TO_BE_ACTIVE, getNumWebServersByState(ServerState.TO_BE_ACTIVE));
        INTRA_RUN_DATA.addField(endTs, ACTIVE, getNumWebServersByState(ServerState.ACTIVE));
        INTRA_RUN_DATA.addField(endTs, TO_BE_REMOVED, getNumWebServersByState(ServerState.TO_BE_REMOVED));
        INTRA_RUN_DATA.addField(endTs, REMOVED, getNumWebServersByState(ServerState.REMOVED));
    }

    public void addJobsData(double endTs, String eventType) {
        INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, eventType);
        int i = 1;
        for (WebServer server : webServers) {
            INTRA_RUN_DATA.addFieldWithSuffix(endTs, JOBS_IN_SERVER, String.valueOf(i), server.size());
            INTRA_RUN_DATA.addFieldWithSuffix(endTs, STATUS_OF_SERVER, String.valueOf(i), server.getServerState().toString());
            i++;
        }
        INTRA_RUN_DATA.addField(endTs, JOBS_IN_SYSTEM, webServers.stream().mapToInt(AbstractServer::size).sum());
    }

    public double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        int completionServerIndex = completed == 1 ? getCompletingServerIndex() : -1;
        Double lastResponseTime = null;

        Job removedJob = null;
        if (completionServerIndex != -1) {
            WebServer minServer = webServers.get(completionServerIndex);
            removedJob = minServer.getMinRemainingLifeJob();

            double jobResponseTime = endTs - removedJob.getArrivalTime();
            minServer.getStats().updateSLO(jobResponseTime);

            INTRA_RUN_DATA.addField(endTs, COMPLETING_SERVER_INDEX, completionServerIndex + 1); // +1 because there is no spike server
            INTRA_RUN_DATA.addField(endTs, PER_JOB_RESPONSE_TIME, jobResponseTime);

            boolean isServerRemoved = minServer.removeJob(removedJob);
            if (isServerRemoved)
                INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, ServerState.REMOVED);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            AbstractServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(startTs, endTs, currIndex == completionServerIndex ? 1 : 0);
        }

        /* Compute the moving exponential average of the response time */
        if (completionServerIndex != -1) {
            assert removedJob != null;
            lastResponseTime = endTs - removedJob.getArrivalTime();
            this.updateMovingExpResponseTime(lastResponseTime);

            addStateToScalingData(endTs);
            INTRA_RUN_DATA.addField(endTs, R_0, lastResponseTime);
            INTRA_RUN_DATA.addField(endTs, MOVING_R_O, this.movingExpMeanResponseTime);
        }

        this.transientStats.updateStats(completionServerIndex, startTs, endTs, lastResponseTime);
        this.systemStats.updateStationaryStats(endTs);

        return this.movingExpMeanResponseTime;
    }

    int getCompletingServerIndex() {
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

    int webServersSize() {
        int size = 0;
        for (WebServer server : webServers) {
            size += server.size();
        }
        return size;
    }

    public void assignJob(Job job) {
        AbstractServer target = scheduler.select(this.webServers);
        target.addJob(job);
    }

    public boolean activeJobExists() {
        for (WebServer server : webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

    public double computeNextCompletionTs(double endTs) {
        double currRemainingLife, minRemainingLife = INFINITY;

        for (WebServer server : webServers.stream().filter(server -> server.size() != 0).toList()) {
            currRemainingLife = server.getMinRemainingLife() * server.size() / server.getCapacity();
            if (currRemainingLife < minRemainingLife) {
                minRemainingLife = currRemainingLife;
            }
        }

        return endTs + minRemainingLife;
    }

    public int getCompletedJobNumber(){
        return webServers.stream()
                .map(AbstractServer::getStats)
                .map(ServerStats::getCompletedJobs).reduce(0, Integer::sum);
    }

    public void printServerStats(double currentTs) {
        for (WebServer server : webServers) {
            System.out.printf("\nWebServer %d: ", webServers.indexOf(server) + 1);
            server.printServerStats(currentTs);
        }
    }

    public void printSystemStats(double currentTs) {
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SPIKE_JOBS_COMPLETED, "");
        systemStats.processStats(currentTs);
    }

    WebServer findScaleOutTarget() {
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

    WebServer findScaleInTarget() {
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

    public WebServer requestScaleOut(double endTs, double turnOnTime) {
        WebServer targetWebServer = findScaleOutTarget();

        /* If found server, make it active */
        if (targetWebServer != null) {
            targetWebServer.setServerState(ServerState.TO_BE_ACTIVE);
            targetWebServer.setActivationTimestamp(endTs + turnOnTime);

            INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, ServerState.TO_BE_ACTIVE);
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

            INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, ServerState.TO_BE_REMOVED);
            addStateToScalingData(endTs);
        }

        /* If no server is found, only 1 server is active */
        else System.out.println("No active servers found!");
    }

    public void scaleOut(double endTs, WebServer targetWebServer) {
        targetWebServer.setServerState(ServerState.ACTIVE);
        INTRA_RUN_DATA.addField(endTs, EVENT_TYPE, ServerState.ACTIVE);
        addStateToScalingData(endTs);
    }

    void updateMovingExpResponseTime(double lastResponseTime) {
        this.movingExpMeanResponseTime = this.movingExpMeanResponseTime * ALPHA +
                lastResponseTime * (1 - ALPHA);
    }
}

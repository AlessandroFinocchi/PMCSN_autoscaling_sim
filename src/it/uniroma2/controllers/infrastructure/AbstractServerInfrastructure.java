package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.scheduler.IScheduler;
import it.uniroma2.controllers.scheduler.SchedulerFactory;
import it.uniroma2.controllers.servers.AbstractServer;
import it.uniroma2.controllers.servers.IServer;
import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.WebServer;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemStats;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.models.Config.ALPHA;
import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.utils.DataCSVWriter.JOBS_DATA;
import static it.uniroma2.utils.DataCSVWriter.SCALING_DATA;
import static it.uniroma2.utils.DataField.*;
import static it.uniroma2.utils.DataField.STATUS_OF_SERVER;

public abstract class AbstractServerInfrastructure implements IServerInfrastructure{
    protected final IScheduler scheduler;
    protected final List<WebServer> webServers;
    protected double movingExpMeanResponseTime;
    protected SystemStats stats;

    protected AbstractServerInfrastructure() {
        this.scheduler = SchedulerFactory.create();

        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i++) {
            var serverState = i < START_NUM_SERVERS ? ServerState.ACTIVE : ServerState.REMOVED;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, serverState));
        }
    }

    public int getNumServersByState(ServerState state) {
        return (int) webServers.stream().filter(server -> server.getServerState() == state).count();
    }

    protected void addStateToScalingData(double endTs) {
        SCALING_DATA.addField(endTs, TO_BE_ACTIVE, getNumServersByState(ServerState.TO_BE_ACTIVE));
        SCALING_DATA.addField(endTs, ACTIVE, getNumServersByState(ServerState.ACTIVE));
        SCALING_DATA.addField(endTs, TO_BE_REMOVED, getNumServersByState(ServerState.TO_BE_REMOVED));
        SCALING_DATA.addField(endTs, REMOVED, getNumServersByState(ServerState.REMOVED));
    }

    int webServersSize() {
        int size = 0;
        for (WebServer server : webServers) {
            size += server.size();
        }
        return size;
    }

    protected WebServer findScaleOutTarget() {
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

    protected WebServer findScaleInTarget() {
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

    protected void updateMovingExpResponseTime(double lastResponseTime) {
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
    }
}

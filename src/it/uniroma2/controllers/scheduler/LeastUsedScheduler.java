package it.uniroma2.controllers.scheduler;

import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.WebServer;

import java.util.Comparator;
import java.util.List;

public class LeastUsedScheduler implements IScheduler{

    @Override
    public WebServer select(List<WebServer> webServers) {
        return webServers
                .stream()
                .filter(server -> server.getServerState() == ServerState.ACTIVE)
                .min(Comparator.comparingDouble(WebServer::size))
                .orElseThrow(() -> new IllegalStateException("No active server found"));
    }
}

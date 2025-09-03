package it.uniroma2.controllers.scheduler;

import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.AbstractServer;

import java.util.Comparator;
import java.util.List;

public class LeastUsedScheduler implements IScheduler{

    @Override
    public AbstractServer select(List<AbstractServer> webServers) {
        return webServers
                .stream()
                .filter(server -> server.getServerState() == ServerState.ACTIVE)
                .min(Comparator.comparingDouble(s -> s.size() / s.getCapacity()))
                .orElseThrow(() -> new IllegalStateException("No active server found"));
    }
}

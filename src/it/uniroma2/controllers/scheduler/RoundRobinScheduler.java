package it.uniroma2.controllers.scheduler;

import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.WebServer;

import java.util.List;

public class RoundRobinScheduler implements IScheduler{
    private int nextAssigningServer;

    public RoundRobinScheduler(){
        nextAssigningServer = 0;
    }

    @Override
    public WebServer select(List<WebServer> webServers) {
        for(int currIndex, i = 0; i < webServers.size(); i++) {
            currIndex = (nextAssigningServer + i) % webServers.size();
            WebServer server = webServers.get(currIndex);
            if (server.getServerState() == ServerState.ACTIVE) {
                nextAssigningServer = (currIndex + 1) % webServers.size();
                return server;
            }
        }

        throw new RuntimeException("No active server found");
    }
}

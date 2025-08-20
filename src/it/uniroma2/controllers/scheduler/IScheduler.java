package it.uniroma2.controllers.scheduler;

import it.uniroma2.controllers.servers.AbstractServer;
import it.uniroma2.controllers.servers.WebServer;

import java.util.List;

public interface IScheduler {
    /**
     * Select the active target Webserver (SpikeServer not included)
     * to which assign the job
     * @param webServers the list of servers
     * @return the server chosen
     */
    AbstractServer select(List<WebServer> webServers);
}

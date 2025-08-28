package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

public interface IServerInfrastructure {
    /**
     * Returns the number of Web Servers in a given state
     * @param state the state of the web servers
     * @return the number of web servers in the state 'state'
     */
    int getNumWebServersByState(ServerState state);

    /**
     * Processes the job advancement in their execution
     * @param startTs the computation interval start
     * @param endTs the computation interval end
     * @param completed if the advancement is a completion or not
     * @return the medium response time of the webservers
     */
    double computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException;

    /**
     * Assign the job to a server with a round-robin policy
     * @param job the job to assign
     */
    void assignJob(Job job);

    /**
     * Checks weather an active job exists
     * @return if an active job exists
     */
    boolean activeJobExists();

    /**
     * Computes when the next completion will happen
     * @param endTs the starting point from which computing the completion time
     * @return the completion time
     */
    double computeNextCompletionTs(double endTs);

    boolean isCompletedStationaryStats();

    void printServerStats(double currentTs);

    void printSystemStats(double currentTs);

    WebServer requestScaleOut(double endTs, double turnOnTime);

    WebServer findNextScaleOut();

    void scaleIn(double endTs);

    void scaleOut(double endTs, WebServer targetWebServer);

    void addJobsData(double endTs, String eventType);
}

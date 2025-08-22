package it.uniroma2.controllers.infrastructure;

import it.uniroma2.controllers.servers.*;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;

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

    void printServerStats(DecimalFormat f, double currentTs);

    void printSystemStats(DecimalFormat f, double currentTs);

    WebServer requestScaleOut(double endTs, double turnOnTime);

    WebServer findNextScaleOut();

    void scaleIn(double endTs);

    void scaleOut(double endTs, WebServer targetWebServer);

    void logFineJobs(double endTs, String eventType);
}

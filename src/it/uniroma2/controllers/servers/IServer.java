package it.uniroma2.controllers.servers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.jobs.Job;

public interface IServer {
    void computeJobsAdvancement(double startTs, double endTs, Double completedJobResponseTime) throws IllegalLifeException;

    void addJob(Job job);

    boolean removeJob(Job job);

    Job getMinRemainingLifeJob();
    
    boolean activeJobExists();

    int size();

    double getMinRemainingLife();

    void printServerStats(double currentTs);
}

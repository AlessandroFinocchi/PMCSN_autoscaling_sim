package it.uniroma2.controllers.servers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;

import java.text.DecimalFormat;

public interface IServer {
    void computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException;

    void addJob(Job job);

    boolean removeJob(Job job);

    Job getMinRemainingLifeJob();
    
    boolean activeJobExists();

    int size();

    double getMinRemainingLife();

    double getResponseTime();

    void printServerStats(DecimalFormat f, double currentTs);
}

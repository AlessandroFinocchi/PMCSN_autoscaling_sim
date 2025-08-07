package it.uniroma2.controllers;

import it.uniroma2.models.Job;

public interface IServer {
    void computeJobsAdvancement(double startTs, double endTs, int completed);

    void addJob(Job job);

    void removeJob(Job job);

    Job getMinRemainingLifeJob();
    
    boolean activeJobExists();

    int size();

    double getMinRemainingLife();
}

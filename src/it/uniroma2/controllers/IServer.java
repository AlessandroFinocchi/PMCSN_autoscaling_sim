package it.uniroma2.controllers;

import it.uniroma2.models.Job;

import java.text.DecimalFormat;

public interface IServer {
    void computeJobsAdvancement(double startTs, double endTs, int completed);

    void addJob(Job job);

    void removeJob(Job job);

    Job getMinRemainingLifeJob();
    
    boolean activeJobExists();

    int size();

    double getMinRemainingLife();

    void printStats(DecimalFormat f, double currentTs);
}

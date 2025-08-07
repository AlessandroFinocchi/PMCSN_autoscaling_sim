package it.uniroma2.controllers;

import it.uniroma2.models.Job;
import it.uniroma2.models.JobList;
import it.uniroma2.models.sys.SystemStats;
import lombok.Getter;

public abstract class AbstractServer implements IServer {
    @Getter protected double capacity;
    protected JobList jobs;
    protected SystemStats stats;

    public AbstractServer(double capacity) {
        this.capacity = capacity;
        this.jobs = new  JobList();
        stats = new SystemStats();
    }

    @Override
    public void addJob(Job job) {
        jobs.add(job);
    }

    @Override
    public void removeJob(Job job) {
        jobs.removeJob(job);
    }
    
    @Override
    public boolean activeJobExists(){
        return jobs.activeJobExists();
    }

    @Override
    public int size() {
        return jobs.size();
    }

    @Override
    public double getMinRemainingLife() {
        return jobs.minRemainingLife();
    }

    @Override
    public Job getMinRemainingLifeJob() {
        return jobs.getMinRemainingLifeJob();
    }
}

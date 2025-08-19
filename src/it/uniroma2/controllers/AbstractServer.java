package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.JobList;
import it.uniroma2.models.sys.SystemStats;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

public abstract class AbstractServer implements IServer {
    @Getter @Setter private ServerState serverState;
    @Getter protected double capacity;
    protected JobList jobs;
    protected SystemStats stats;

    public AbstractServer(double capacity, ServerState serverState) {
        this.serverState = serverState;
        this.capacity = capacity;
        this.jobs = new JobList();
        stats = new SystemStats();
    }

    /**
     * Computes the advancement of all jobs using a Processor Sharing scheduling
     * @param startTs the interval start time
     * @param endTs the interval end time
     * @param completed weather a job is completing or not
     */
    @Override
    public void computeJobsAdvancement(double startTs, double endTs, int completed) throws IllegalLifeException {
        /*  At this point the job has already been removed, so if this is the server
         *   where completion happened, it must be taken into consideration */
        int jobAdvanced = jobs.size() + completed;

        stats.updateSystemStats(startTs, endTs, jobAdvanced, completed);

        /* Compute the advancement of each job */
        double quantum = (this.capacity / jobAdvanced) * (endTs - startTs);
        for(Job job: this.jobs.getJobs()) {
            job.decreaseRemainingLife(quantum);
        }
    }

    @Override
    public void addJob(Job job) {
        jobs.add(job);
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

    @Override
    public void printStats(DecimalFormat f, double currentTs) {
        System.out.println("for " + stats.getCompletedJobs() + " jobs");
        System.out.println("   average interarrival time =   " + f.format(currentTs / stats.getCompletedJobs()));
        System.out.println("   average response time ... =   " + f.format(stats.getNodeSum() / stats.getCompletedJobs()));
        System.out.println("   average service time .... =   " + f.format(stats.getServiceSum() / stats.getCompletedJobs()));
        System.out.println("   average # in the node ... =   " + f.format(stats.getNodeSum() / currentTs));
        System.out.println("   utilization ............. =   " + f.format(stats.getServiceSum() / currentTs));
    }

    public double getResponseTime() {
        return stats.getNodeSum() / stats.getCompletedJobs();
    }
}

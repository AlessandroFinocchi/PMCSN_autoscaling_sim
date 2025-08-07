package it.uniroma2.controllers;

import it.uniroma2.models.Job;

public class WebServer extends AbstractServer {

    public WebServer(double capacity) {
        super(capacity);
    }

    @Override
    public void computeJobsAdvancement(double startTs, double endTs, int completed) {
        stats.updateSystemStats(startTs, endTs, jobs.size(), 0);

        /* Compute the advancement of each job */
        double quantum = (this.capacity / this.jobs.size()) * (endTs - startTs);
        for(Job job: this.jobs.getJobs()) {
            job.decreaseRemainingLife(quantum);
        }
    }
}

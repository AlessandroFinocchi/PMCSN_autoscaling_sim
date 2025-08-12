package it.uniroma2.controllers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import lombok.Getter;
import lombok.Setter;

public class WebServer extends AbstractServer {
    @Getter @Setter private boolean toBeRemoved;
    @Getter @Setter private boolean active;

    public WebServer(double capacity, boolean isActive) {
        super(capacity);
        this.toBeRemoved = false;
        this.active = isActive;
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
    
    public double getRemainingServerLife() {
        return jobs.getSumRemainingLife();
    }
}

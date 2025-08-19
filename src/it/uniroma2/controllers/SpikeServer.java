package it.uniroma2.controllers;

import it.uniroma2.models.Job;

public class SpikeServer extends AbstractServer{

    public SpikeServer(double capacity) {
        super(capacity);
    }

    @Override
    public boolean removeJob(Job job) {
        jobs.removeJob(job);
        return false;
    }

}

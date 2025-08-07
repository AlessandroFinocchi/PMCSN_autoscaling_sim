package it.uniroma2.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.INFINITY;

public class JobList {
    @Getter private List<Job> jobs;

    public JobList() {
        jobs = new ArrayList<>();
    }

    public int size() {
        return jobs.size();
    }

    public boolean activeJobExists() {
        return !jobs.isEmpty();
    }

    public void removeJob(Job job) {
        jobs.remove(job);
    }

    public void add(Job newJob) {
        jobs.add(newJob);
    }

    public void removeMinRemainingLifeJob() {
        Job minJob = getMinRemainingLifeJob();
        if (minJob != null) {
            jobs.remove(minJob);
        }
    }

    public Job getMinRemainingLifeJob() {
        return jobs.stream().min(Comparator.comparing(Job::getRemainingLife)).orElse(null);
    }

    public double minRemainingLife() {
        if (jobs.isEmpty()) return INFINITY;
        return jobs.stream().min(Comparator.comparing(Job::getRemainingLife)).get().getRemainingLife();
    }
}

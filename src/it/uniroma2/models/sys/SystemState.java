package it.uniroma2.models.sys;

import it.uniroma2.models.Job;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.events.Event;
import it.uniroma2.models.events.EventCalendar;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static it.uniroma2.models.Config.*;

public class SystemState {
    @Getter private final double capacity;
    @Getter @Setter private double current;                 /* current time                        */
    private EventCalendar calendar;
    @Getter private List<Job> jobs;
    @Getter private final Distribution arrivalVA;
    @Getter private final Distribution servicesVA;

    public SystemState(EventCalendar calendar, Distribution arrivalVA, Distribution servicesVA) {
        this.capacity = WEBSERVER_CAPACITY;
        this.current = START;
        this.calendar = calendar;
        this.jobs = new ArrayList<>();
        this.arrivalVA = arrivalVA;
        this.servicesVA = servicesVA;
    }

    public boolean jobActiveExist() {
        return !this.jobs.isEmpty();
    }

    public void addEvent(Event event) {
        this.calendar.addEvent(event);
    }

    public void removeMinRemainingLifeJob() {
        jobs.stream().min(Comparator.comparing(Job::getRemainingLife)).ifPresent(this::removeJob);
    }

    public double minRemainingLife() {
        if (jobs.isEmpty()) return INFINITY;
        return jobs.stream().min(Comparator.comparing(Job::getRemainingLife)).get().getRemainingLife();
    }

    public void removeJob(Job job) {
        jobs.remove(job);
    }
}
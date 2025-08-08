package it.uniroma2.models;

import it.uniroma2.exceptions.IllegalLifeException;
import lombok.Getter;


public class Job {
    @Getter private final double arrivalTime;
    @Getter private double remainingLife;

    public Job(double arrivalTime, double executionLife) {
        this.arrivalTime = arrivalTime;
        this.remainingLife = executionLife;
    }

    public void decreaseRemainingLife(double executedTime) throws IllegalLifeException {
        this.remainingLife -= executedTime;

        if (remainingLife < 0) {
            System.out.println("Remaining life: " + remainingLife);
            throw new IllegalLifeException("Remaining time cannot be negative");
        }
    }
}

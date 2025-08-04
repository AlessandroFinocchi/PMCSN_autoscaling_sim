package it.uniroma2.models;

import lombok.Getter;


public class Job {
    @Getter private final double arrivalTime;
    @Getter private double remainingLife;

    public Job(double arrivalTime, double executionLife) {
        this.arrivalTime = arrivalTime;
        this.remainingLife = executionLife;
    }

    public void decreaseRemainingLife(double executedTime) {
        this.remainingLife -= executedTime;

        if (remainingLife < 0) {
            System.out.println("Remaining life: " + remainingLife);
            throw new RuntimeException("Remaining time cannot be negative");
        }
    }
}

package it.uniroma2.models;

import it.uniroma2.exceptions.JobCompletionException;
import lombok.Getter;

import static it.uniroma2.models.Config.QUANTUM_THR;

public class Job {
    @Getter private final double arrivalTime;
    @Getter private double remainingLife;

    public Job(double arrivalTime, double executionLife) {
        this.arrivalTime = arrivalTime;
        this.remainingLife = executionLife;
    }

    public void decreaseRemainingLife(double executedTime) throws JobCompletionException {
        this.remainingLife -= executedTime;

        if (remainingLife < -QUANTUM_THR) {
            System.out.println("Remaining life: " + remainingLife);
            throw new RuntimeException("Remaining time cannot be negative");
        }
        else if (remainingLife < QUANTUM_THR) throw new JobCompletionException();

    }
}

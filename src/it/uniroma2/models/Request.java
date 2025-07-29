package it.uniroma2.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Request {
    @Getter private double arrivalTime;
    @Getter private double executionTime;

    @Override
    public String toString() {
        return "Arrival Time: " + arrivalTime + " - Execution Time: " + executionTime;
    }
}

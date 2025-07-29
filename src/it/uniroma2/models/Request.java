package it.uniroma2.models;

import it.uniroma2.libs.Rng;
import lombok.Getter;

public class Request {
    @Getter private double arrivalTime;
    @Getter private double serviceTime;

    @Getter private static long LAST = 10000;                    /* number of reqs processed */
    @Getter private static double START = 0.0;                   /* initial time             */
    private double sarrival = START;              /* sum of arrivals          */

    public Request(Rng r) {
        this.arrivalTime = getArrival(r);
        this.serviceTime = getService(r);
    }

    @Override
    public String toString() {
        return "Arrival Time: " + arrivalTime + " - Execution Time: " + serviceTime;
    }


    double exponential(double m, Rng r) {
        return (-m * Math.log(1.0 - r.random()));
    }

    double uniform(double a, double b, Rng r) {
        return (a + (b - a) * r.random());
    }

    double getArrival(Rng r) {
        sarrival += exponential(2.0, r);
        return sarrival;
    }

    double getService(Rng r) {
        return (uniform(1.0, 2.0, r));
    }
}

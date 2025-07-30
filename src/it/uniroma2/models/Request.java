package it.uniroma2.models;

import it.uniroma2.libs.Rngs;
import lombok.Getter;

public class Request {
    @Getter private double arrivalTime;
    @Getter private double serviceTime;

    @Getter private static long LAST = 10000;                    /* number of reqs processed */
    @Getter private static double START = 0.0;                   /* initial time             */
    private double sarrival = START;              /* sum of arrivals          */

    public Request(Rngs r) {
        this.arrivalTime = getArrival(r);
        this.serviceTime = getService(r);
    }

    @Override
    public String toString() {
        return "Arrival Time: " + arrivalTime + " - Execution Time: " + serviceTime;
    }


    double exponential(double m, Rngs r) {
        return (-m * Math.log(1.0 - r.random()));
    }

    double uniform(double a, double b, Rngs r) {
        return (a + (b - a) * r.random());
    }

    double getArrival(Rngs r) {
        r.selectStream(0);
        sarrival += exponential(2.0, r);
        return sarrival;
    }

    double getService(Rngs r) {
        r.selectStream(1);
        return (uniform(1.0, 2.0, r));
    }
}

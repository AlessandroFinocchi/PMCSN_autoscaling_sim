package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class Exponential extends Distribution {

    public Exponential(Rngs r, int stream, double mean) {
        super(r, stream, mean);
    }

    @Override
    protected double newTime() {
        return -this.mean * Math.log(1.0 - this.r.random());
    }

    @Override
    public void setMean(double mean) {
        this.mean = mean;
    }
}

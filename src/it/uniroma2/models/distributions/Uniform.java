package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class Uniform extends Distribution {
    private final double min;
    private final double max;

    public Uniform(Rngs r, int stream, double mean, double intervalRange) {
        super(r, stream, mean);
        this.min = mean - intervalRange;
        this.max = mean + intervalRange;
    }

    @Override
    public double newTime() {
        return this.min + (this.max - this.min) * this.r.random();
    }

    @Override
    public void setMean(double mean) {
        throw new UnsupportedOperationException("Cannot change mean of Uniform distribution");
    }
}

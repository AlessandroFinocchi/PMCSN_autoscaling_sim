package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public abstract class Distribution implements IDistribution {
    protected Rngs r;
    protected final int stream;
    protected double mean;

    protected Distribution(Rngs r, int stream, double mean) {
        this.r = r;
        this.stream = stream;
        this.mean = mean;
    }

    public double gen() {
        r.selectStream(stream);
        return newTime();
    }

    abstract protected double newTime();
}

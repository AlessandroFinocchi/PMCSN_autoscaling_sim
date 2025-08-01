package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public abstract class Distribution implements IDistribution {
    protected Rngs r;
    protected final int stream;

    protected Distribution(Rngs r, int stream) {
        this.r = r;
        this.stream = stream;
    }

    public double gen() {
        r.selectStream(stream);
        return newTime();
    }

    abstract protected double newTime();
}

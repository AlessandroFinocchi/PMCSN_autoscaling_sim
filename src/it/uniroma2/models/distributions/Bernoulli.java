package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class Bernoulli extends Distribution {
    private final double p;

    public Bernoulli(Rngs r, int stream, double p) {
        super(r, stream);
        this.p = p;
    }

    @Override
    protected double newTime() {
        return (r.random() < p) ? 1.0 : 0.0;
    }
}

package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class Exponential extends Distribution {
    private final double lambda;

    public Exponential(Rngs r, int stream, double lambda) {
        super(r, stream);
        this.lambda = lambda;
    }

    @Override
    protected double newTime() {
        return (-this.lambda * Math.log(1.0 - this.r.random()));
    }
}

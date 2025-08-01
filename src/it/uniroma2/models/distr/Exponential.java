package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class Exponential extends Distribution {
    /** f(x) = λ*e(-λx) => mu = 1/λ */
    private final double mu;

    public Exponential(Rngs r, int stream, double lambda) {
        super(r, stream);
        this.mu = lambda;
    }

    @Override
    protected double newTime() {
        return -this.mu * Math.log(1.0 - this.r.random());
    }
}

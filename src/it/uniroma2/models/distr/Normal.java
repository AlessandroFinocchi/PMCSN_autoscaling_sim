package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class Normal extends Distribution {
    private final double mu;
    private final double s2;

    public Normal(Rngs r, int stream, double mu, double s2) {
        super(r, stream);
        this.mu = mu;
        this.s2 = s2;
    }

    @Override
    protected double newTime() {
        return this.mu + this.s2 * this.r.random();
    }
}

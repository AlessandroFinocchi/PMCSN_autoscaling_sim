package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class Normal extends Distribution {
    private final double mu;
    private final double s2;
    private final Uniform u1VA;
    private final Uniform u2VA;

    public Normal(Rngs r, int stream1, int stream2, double mu, double s2) {
        super(r, 0);
        this.u1VA = new Uniform(r, stream1, 0, 1);
        this.u2VA = new Uniform(r, stream2, 0, 1);
        this.mu = mu;
        this.s2 = s2;
    }

    @Override
    protected double newTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double gen() {
        double u1 = u1VA.newTime();
        double u2 = u2VA.newTime();
        double z0 = Math.sqrt(- 2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
        return mu + z0 * Math.sqrt(s2);
    }
}

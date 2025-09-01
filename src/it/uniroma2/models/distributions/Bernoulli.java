package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class Bernoulli extends Distribution {

    public Bernoulli(Rngs r, int stream, double mean) {
        super(r, stream, mean);
    }

    @Override
    protected double newTime() {
        return (r.random() < this.mean) ? 1.0 : 0.0;
    }

    @Override
    public void setMean(double mean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

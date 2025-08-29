package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class HyperExponential extends Distribution {
    private final Exponential exp1;
    private final Exponential exp2;
    private final Bernoulli bernoulli;

    public HyperExponential(Rngs r, int stream1, double mu1, int stream2, double mu2, int stream3, double p) {
        super(r, 0);
        this.bernoulli = new Bernoulli(r, stream3, p);
        this.exp1 = new Exponential(r, stream1, mu1);
        this.exp2 = new Exponential(r, stream2, mu2);
    }

    @Override
    public double gen() {
        var x = this.bernoulli.gen();
        if (x == 1.0) {
            return this.exp1.gen();
        } else {
            return this.exp2.gen();
        }
    }

    @Override
    protected double newTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

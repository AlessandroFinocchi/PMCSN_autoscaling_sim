package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class CHyperExponential extends Distribution {
    private final Exponential exp1;
    private final Exponential exp2;
    private final Bernoulli bernoulli;

    protected CHyperExponential(Rngs r, double variationCoefficient, double mean, int stream1, int stream2, int stream3) {
        super(r, 0);
        double p = 1.0 / 2.0 * (1 + Math.sqrt((variationCoefficient - 1.0) / (variationCoefficient + 1.0)));
        double mu1 = mean / (2.0 * p);
        double mu2 = mean / (2.0 * (1.0 - p));
        this.exp1 = new Exponential(r, stream1, mu1);
        this.exp2 = new Exponential(r, stream2, mu2);
        this.bernoulli = new Bernoulli(r, stream3, p);
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

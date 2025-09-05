package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;

public class CHyperExponential extends Distribution {
    private final Exponential exp1;
    private final Exponential exp2;
    private final Bernoulli bernoulli;
    private final double p;

    public CHyperExponential(Rngs r, double variationCoefficient, double mean, int stream1, int stream2, int stream3) {
        super(r, null, mean);
        this.p = 1.0 / 2.0 * (1 + Math.sqrt((variationCoefficient - 1.0) / (variationCoefficient + 1.0)));
        double mu1 = mean / (2.0 * p);
        double mu2 = mean / (2.0 * (1.0 - p));
        this.exp1 = new Exponential(r, stream1, mu1);
        this.exp2 = new Exponential(r, stream2, mu2);
        this.bernoulli = new Bernoulli(r, stream3, p);

        setMean(mean);
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

    @Override
    public void setMean(double mean) {
        double mu1 = mean / (2.0 * p);
        double mu2 = mean / (2.0 * (1.0 - p));
        this.exp1.setMean(mu1);
        this.exp2.setMean(mu2);
    }
}

package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class HyperExponential extends Distribution {
    private final Exponential exp1;
    private final Exponential exp2;
    private final Bernoulli bernoulli;

    public HyperExponential(Rngs r, int stream, double mu1, double mu2, Bernoulli bernoulli) {
        super(r, stream);
        this.bernoulli = bernoulli;
        this.exp1 = new Exponential(r, stream, mu1);
        this.exp2 = new Exponential(r, stream, mu2);
    }

    @Override
    protected double newTime() {
        var x = this.bernoulli.newTime();
        if (x == 1.0) {
            return this.exp1.newTime();
        } else {
            return this.exp2.newTime();
        }
    }
}

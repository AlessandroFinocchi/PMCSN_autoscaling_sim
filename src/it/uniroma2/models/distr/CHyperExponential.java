package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class CHyperExponential extends Distribution {

    private final double variationCoefficient;

    protected CHyperExponential(Rngs r, int stream, double variationCoefficient) {
        super(r, stream);
        this.variationCoefficient = variationCoefficient;
    }

    @Override
    protected double newTime() {
        return 0;
    }
}

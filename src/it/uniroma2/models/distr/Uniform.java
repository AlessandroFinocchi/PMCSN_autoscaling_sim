package it.uniroma2.models.distr;

import it.uniroma2.libs.Rngs;

public class Uniform extends Distribution {
    private final double min;
    private final double max;

    public Uniform(Rngs r, int stream, double min, double max) {
        super(r, stream);
        this.min = min;
        this.max = max;
    }

    @Override
    public double newTime() {
        return this.min + (this.max - this.min) * this.r.random();
    }
}

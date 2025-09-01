package it.uniroma2.models.distributions;

public interface IDistribution {
    double gen();

    void setMean(double mean);
}

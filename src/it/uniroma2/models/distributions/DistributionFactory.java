package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static it.uniroma2.models.Config.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributionFactory {

    public static Distribution createArrivalDistribution(Rngs rngs) {
        return switch (ARRIVALS_DISTR) {
            case "exp" -> new Exponential(rngs, 0, ARRIVALS_MU);
            case "h2"  -> new CHyperExponential(rngs, ARRIVALS_CV, ARRIVALS_MU, 0, 1, 2);
            default -> throw new IllegalArgumentException("Invalid arrival distribution type: " + ARRIVALS_DISTR);
        };
    }

    public static Distribution createServiceDistribution(Rngs rngs) {
        return switch (SERVICES_DISTR) {
            case "exp" -> new Exponential(rngs, 1, SERVICES_Z);
            case "h2"  -> new CHyperExponential(rngs, SERVICES_CV, SERVICES_Z, 3, 4, 5);
            default -> throw new IllegalArgumentException("Invalid service distribution type: " + SERVICES_DISTR);
        };
    }
}

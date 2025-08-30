package it.uniroma2.models.configurations.experiments;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static it.uniroma2.models.Config.EXPERIMENT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperimentFactory {

    public static Experiment create() {
        return switch (EXPERIMENT) {
            case "val_b" -> new ExperimentBaseValidation();
            case "trans_b" -> new ExperimentBaseTransient();
            default -> throw new IllegalArgumentException("Invalid experimenti type: " + EXPERIMENT);
        };
    }
}

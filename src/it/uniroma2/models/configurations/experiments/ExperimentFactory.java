package it.uniroma2.models.configurations.experiments;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static it.uniroma2.models.Config.EXPERIMENT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperimentFactory {

    public static Experiment create() {
        return switch (EXPERIMENT) {
            case "simple_run" -> new SimpleRun();
            case "ver_b" -> new ExperimentBaseVerification();
            case "val_b" -> new ExperimentBaseValidation();
            case "trans_b" -> new ExperimentBaseTransient();
            case "base1" -> new ExperimentBase1();
            case "val_adv" -> new ExperimentAdvancedValidation();
            case "trans_adv" -> new ExperimentAdvancedTransient();
            default -> throw new IllegalArgumentException("Invalid experiment type: " + EXPERIMENT);
        };
    }
}

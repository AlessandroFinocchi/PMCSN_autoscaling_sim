package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAdvancedTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();
    private int i;

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        // // Group 1
        // i = 1;
        // addConfiguration(i++, 4.0, 1.0, 4, 10, null, 0.2, null);
        // addConfiguration(i++, 4.0, 1.0, 4, 10, null, 5.0, null);
        // addConfiguration(i++, 4.0, 1.0, 4, 10, null, 100.0, null);
        // // Group 2
        // i = 4;
        // addConfiguration(i++, 4.0, 1.0, 4, 10, 3.0, 0.2, null);
        // addConfiguration(i++, 4.0, 1.0, 4, 10, 3.0, 5.0, null);
        // addConfiguration(i++, 4.0, 1.0, 4, 10, 3.0, 100.0, null);
        // Group 3
        i = 7;
        addConfiguration(i++, 8.0, 1.0, 4, 10, null, 0.1, null);
        addConfiguration(i++, 8.0, 1.0, 4, 10, 3.0, 5.0, null);

        return result;
    }

    void addConfiguration(
            int index, double lambda, double z, double cv, int maxWsNumber,
            Double siMax, Double scalingThreshold,
            Double fastLambda
    ) {
        RunConfiguration c = new RunConfiguration("trans_base_" + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "1");
        c.put("log.intra_run", "true");
        c.put("stats.batch.size", "INFINITY");
        c.put("system.stop", "10000");
        c.put("system.empty_jobs", "false");

        /* Specific */
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.arrivals.cv", String.valueOf(cv));
        c.put("distribution.services.z", String.valueOf(z));
        c.put("distribution.services.cv", String.valueOf(cv));
        c.put("infrastructure.max_num_server", String.valueOf(maxWsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(siMax != null));
        c.put(
                "infrastructure.si_max",
                (siMax == null) ? "-1.0" : String.valueOf(siMax)
        );
        c.put(
                "webserver.scaling.out_thr",
                (scalingThreshold == null) ? "INFINITY" : String.valueOf(scalingThreshold)
        );

        /* Add configuration with long-term fluctuations */
        if (fastLambda != null) {
            c.put("distribution.arrivals.fast_interval", "100");
            c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / fastLambda));
        }

        // extra
        c.put("infrastructure.start_num_server", String.valueOf(maxWsNumber / 2));

        result.add(c);
    }
}

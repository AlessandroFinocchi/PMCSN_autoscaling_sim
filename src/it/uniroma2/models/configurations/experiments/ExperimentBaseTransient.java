package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int shortRunLength  = 10000;
        int longRunLength   = 25000;

        // Group 1
        addConfiguration(1, 4.0, 1.0, 4, 3, null, null, shortRunLength);
        addConfiguration(2, 4.0, 1.0, 4, 5, null, null, longRunLength);
        // Group 2
        addConfiguration(3, 10.0, 0.4, 4, 5, null, null, shortRunLength);
        addConfiguration(4, 4.0, 1.0, 40, 5, null, null, longRunLength);
        // Group 3
        addConfiguration(5, 4.0, 1.0, 4, 5, 0.1, null, longRunLength);
        addConfiguration(6, 4.0, 1.0, 4, 5, 3.0, null, longRunLength);
        addConfiguration(7, 4.0, 1.0, 4, 5, 100.0, null, longRunLength);
        // Group 4
        addConfiguration(8, 5.5, 1.0, 4, 5, 2.0, null, longRunLength);
        addConfiguration(9, 6.5, 1.0, 4, 5, 2.0, null, shortRunLength);
        // Group 5a
        addConfiguration(10, 4.0, 1.0, 4, 5, null, 6.0, shortRunLength);
        addConfiguration(11, 4.0, 1.0, 4, 5, null, 8.0, shortRunLength);
        addConfiguration(12, 4.0, 1.0, 4, 5, null, 16.0, shortRunLength);
        // Group 5b
        addConfiguration(13, 4.0, 1.0, 4, 4, 2.0, 6.0, shortRunLength);
        addConfiguration(14, 4.0, 1.0, 4, 4, 2.0, 8.0, shortRunLength);
        addConfiguration(15, 4.0, 1.0, 4, 4, 2.0, 16.0, shortRunLength);

        return result;
    }

    void addConfiguration(
            int index,
            double lambda, double z, double cv, int wsNumber, Double siMax,
            Double fastLambda,
            int runLength
    ) {
        RunConfiguration c = new RunConfiguration("trans_base_" + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "4");
        c.put("log.intra_run", "true");
        c.put("stats.batch.size", "INFINITY");
        c.put("system.stop", String.valueOf(runLength));
        c.put("system.empty_jobs", "false");
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");

        /* Specific */
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.arrivals.cv", String.valueOf(cv));
        c.put("distribution.services.z", String.valueOf(z));
        c.put("distribution.services.cv", String.valueOf(cv));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(siMax != null));
        c.put(
                "infrastructure.si_max",
                (siMax == null) ? "-1.0" : String.valueOf(siMax)
        );

        /* Add configuration with long-term fluctuations */
        if (fastLambda != null) {
            c.put("distribution.arrivals.fast_interval", "100");
            c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / fastLambda));
        }

        // extra
        c.put("infrastructure.max_num_server", String.valueOf(wsNumber));

        result.add(c);
    }
}

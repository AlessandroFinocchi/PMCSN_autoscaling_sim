package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        addConfiguration(1, 4.0, 1, 4, 3, false, null);
        addConfiguration(2, 4.0, 1, 4,5, false, null);
        addConfiguration(3, 4.0, 1, 4,8, false, null);
        addConfiguration(4, 4.8, 1, 4,5, false, null);
        addConfiguration(5, 10, 0.4, 4,5, false, null);
        addConfiguration(6, 2, 2, 4,5, false, null);
        addConfiguration(7, 10, 0.4, 16,5, false, null);
        addConfiguration(8, 4, 1, 4,5, true, 0.1);
        addConfiguration(9, 4, 1, 4,5, true, 0.3);
        addConfiguration(10, 4, 1, 4,5, true, 1.0);
        addConfiguration(11, 4, 1, 4,5, true, 100.0);
        addConfiguration(12, 4, 1.5, 4,5, true, 2.0);
        addConfiguration(13, 4, 1.5, 4,5, true, 4.0);
        addConfiguration(14, 4, 1.5, 4,5, true, 10.0);
        addConfiguration(15, 4, 1.5, 4,5, true, 20.0);

        return result;
    }

    void addConfiguration(int index, double lambda, double z, double cv, int wsNumber, boolean ssActive, Double siMax) {
        RunConfiguration c = new RunConfiguration("trans_b_" + String.format("%02d", index));

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
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(ssActive));
        if (siMax != null) c.put("infrastructure.si_max", String.valueOf(siMax));

        // extra
        c.put("infrastructure.max_num_server", String.valueOf(wsNumber));

        result.add(c);
    }
}

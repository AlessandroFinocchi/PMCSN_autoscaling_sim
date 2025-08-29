package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        for (int i = 1; i <= 15; i++){
            RunConfiguration c = new RunConfiguration("trans_b_" + String.format("%02d", i));
            c.put("random.repeat_config", "5");
            c.put("log.intra_run", "true");
            c.put("stats.batch.size", "INFINITY");
            c.put("system.stop", "10000");
            c.put("system.empty_jobs", "false");
            result.add(c);
        }

        setConfiguration(1, 4.0, 1, 3, false, null);
        setConfiguration(2, 4.0, 1, 5, false, null);
        setConfiguration(3, 4.0, 1, 8, false, null);
        setConfiguration(4, 4.8, 1, 5, false, null);
        setConfiguration(5, 10, 0.4, 5, false, null);
        setConfiguration(6, 2, 2, 5, false, null);
        setConfiguration(7, 10, 0.4, 8, false, null);
        setConfiguration(8, 4, 1, 5, true, 0.1);
        setConfiguration(9, 4, 1, 5, true, 0.3);
        setConfiguration(10, 4, 1, 5, true, 1.0);
        setConfiguration(11, 4, 1, 5, true, 100.0);
        setConfiguration(12, 4, 1.5, 5, true, 2.0);
        setConfiguration(13, 4, 1.5, 5, true, 4.0);
        setConfiguration(14, 4, 1.5, 5, true, 10.0);
        setConfiguration(15, 4, 1.5, 5, true, 20.0);

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
        }

        return result;
    }

    void setConfiguration(int index, double lambda, double z, int wsNumber, boolean ssActive, Double siMax) {
        RunConfiguration c = result.get(index - 1);
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.services.z", String.valueOf(z));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(ssActive));
        if (siMax != null) c.put("infrastructure.si_max", String.valueOf(siMax));
    }
}

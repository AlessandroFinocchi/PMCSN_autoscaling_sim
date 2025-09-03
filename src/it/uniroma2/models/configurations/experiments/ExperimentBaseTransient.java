package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        double lambda = 6.66;
        double z = 0.16;
        int i = 1;

        // addConfiguration(i++, lambda, z, 4,1, false, -1.0);
        // addConfiguration(i++, lambda, z, 4,5, false, -1.0);
        // addConfiguration(i++, lambda, z, 4,7, false, -1.0);
        // for (double si = 0.0; si < 1.0; si += 0.1){
        //     addConfiguration(i++, lambda, z, 4,5, true, si);
        // }
        // for (double si = 1.0; si <= 10.0; si += 1.0){
        //     addConfiguration(i++, lambda, z, 4,5, true, si);
        // }
        // addConfiguration(i++, lambda, z, 4,5, true, 100.0);

        addConfiguration(i++, lambda, z, 4,1, false, -1.0);
        for (double si = 10.0; si <= 160.0; si += 10.0){
            addConfiguration(i++, lambda, z, 4,1, true, si);
        }

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
        // c.put("distribution.arrivals.fast_interval", "100");

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

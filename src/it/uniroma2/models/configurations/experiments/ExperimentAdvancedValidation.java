package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAdvancedValidation implements Experiment{
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int index = 1;

        setConfiguration(index++, 4, 1, 5, 10, 2);

        return result;
    }

    void setConfiguration(int index, double lambda, double z, int startWsNum, int maxWsNum, double scalingThreshold) {
        RunConfiguration c = new RunConfiguration("val_adv_" + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "1");
        c.put("log.intra_run", "true");
        c.put("stats.batch.size", "INFINITY");
        c.put("system.stop", "10000");
        c.put("system.empty_jobs", "false");
        c.put("infrastructure.si_max", "3");
        c.put("webserver.capacity", "1");
        c.put("spikeserver.version", "2");
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");
        c.put("infrastructure.spikeserver.active", "true");
        c.put("distribution.arrivals.fast_interval", "100");
        c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / 8.0));

        /* Specific */
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.services.z",  String.valueOf(z));
        c.put("infrastructure.start_num_server", String.valueOf(startWsNum));
        c.put("infrastructure.max_num_server", String.valueOf(maxWsNum));
        c.put("webserver.scaling.out_thr", String.valueOf(scalingThreshold));

        result.add(c);
    }
}

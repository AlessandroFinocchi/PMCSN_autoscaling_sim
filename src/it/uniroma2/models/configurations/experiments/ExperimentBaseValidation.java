package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseValidation implements Experiment{
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        String EXP = "exp";
        String H2 = "h2";

        setConfiguration(1, H2, H2,   1/0.15, 0.16, 1, 1, true);
        setConfiguration(2, EXP, EXP, 0.6,    1, 2, 1, false);
        setConfiguration(3, EXP, EXP, 0.6,    1, 1, 2, false);

        return result;
    }

    void setConfiguration(int index, String arrivalDistr, String completionDistr, double lambda, double z,
                          int wsNumber, double wsCapacity, boolean spikeServerActive) {
        RunConfiguration c = new RunConfiguration("val_b_" + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "1");
        c.put("log.intra_run", "true");
        c.put("stats.batch.num", "64");
        c.put("stats.batch.size", "512");
        c.put("system.stop", "100000");
        c.put("system.empty_jobs", "false");
        c.put("infrastructure.si_max", "140");
        c.put("spikeserver.version", "1");

        /* Specific */
        c.put("distribution.arrivals.type", arrivalDistr);
        c.put("distribution.services.type", completionDistr);
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.services.z",  String.valueOf(z));
        c.put("webserver.capacity", String.valueOf(wsCapacity));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(spikeServerActive));

        // extra
        c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));

        result.add(c);
    }
}

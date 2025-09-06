package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseVV implements Experiment{
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        String EXP = "exp";
        String H2 = "h2";

        setConfiguration(1, EXP, EXP, 0.6, 1, 1, 1);
        setConfiguration(2, EXP, EXP, 0.8, 1, 1, 1);
        setConfiguration(3, EXP, H2,  0.6, 1, 1, 1);
        setConfiguration(4, EXP, H2,  0.8, 1, 1, 1);
        setConfiguration(5, EXP, EXP, 0.6, 1, 2, 1);
        setConfiguration(6, EXP, EXP, 0.6, 1, 1, 2);

        return result;
    }

    void setConfiguration(int index, String arrivalDistr, String completionDistr, double lambda, double z,
                          int wsNumber, double wsCapacity) {
        String name = index < 5 ? "ver_b_" : "val_b_";
        RunConfiguration c = new RunConfiguration(name + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "1");
        c.put("log.intra_run", "false");
        c.put("infrastructure.spikeserver.active", "false");
        c.put("stats.batch.num", "128");
        c.put("stats.batch.size", "512");
        c.put("system.stop", "100000");
        c.put("system.empty_jobs", "false");

        /* Specific */
        c.put("distribution.arrivals.type", arrivalDistr);
        c.put("distribution.services.type", completionDistr);
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.services.z",  String.valueOf(z));
        c.put("webserver.capacity", String.valueOf(wsCapacity));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));

        // extra
        c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));

        result.add(c);
    }
}

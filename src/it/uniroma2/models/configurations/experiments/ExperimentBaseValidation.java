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

        setConfiguration(1, EXP, EXP, 3, 1, 1);
        setConfiguration(2, EXP, EXP, 4, 1, 1);
        setConfiguration(3, EXP, H2,  3, 1, 1);
        setConfiguration(4, EXP, H2,  4, 1, 1);
        setConfiguration(5, EXP, EXP, 6, 2, 1);
        setConfiguration(6, EXP, EXP, 8, 2, 1);
        setConfiguration(7, EXP, EXP, 3, 2, 1);
        setConfiguration(8, EXP, EXP, 3, 1, 2);
        setConfiguration(9, H2,  H2,  3, 1, 1);
        setConfiguration(10,H2,  H2,  4, 1, 1);

        return result;
    }

    void setConfiguration(int index, String arrivalDistr, String completionDistr, double lambda,
                          int wsNumber, double wsCapacity) {
        RunConfiguration c = new RunConfiguration("val_b_" + String.format("%02d", index));

        /* Common */
        c.put("random.repeat_config", "1");
        c.put("log.intra_run", "true");
        c.put("stats.batch.size", "INFINITY");
        c.put("system.stop", "10000");
        c.put("system.empty_jobs", "false");

        /* Specific */
        c.put("distribution.arrivals.type", arrivalDistr);
        c.put("distribution.services.type", completionDistr);
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("webserver.capacity", String.valueOf(wsCapacity));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));

        // extra
        c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));

        result.add(c);
    }
}

package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAdvancedTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();
    private int index;

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /*  */
        index = 100;
        setConfiguration(++index, 10, 2.0, 3.0, "r0");
        setConfiguration(++index, 10, 2.0, 3.0, "jobs");

        index = 200;
        setConfiguration(++index, 6, 2.0, 5.0, "jobs");

        return result;
    }

    void setConfiguration(int index, int maxWsNum, double siMax, Double scalingThr, String scalingType) {
        //        RunConfiguration c = new RunConfiguration("base_1_" + String.format("%02d", index));
        RunConfiguration c = new RunConfiguration(String.valueOf(index));

        /* For transient */
        c.put("log.fine", "false");
        c.put("log.intra_run", "true");
        c.put("random.repeat_config", "64");
        c.put("stats.batch.size", "INFINITY");
        c.put("system.stop", "15000");
        c.put("system.empty_jobs", "false");

        /* Common */
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / 4.0));
        c.put("distribution.services.z", "1");
        c.put("infrastructure.spikeserver.active", "true");
        c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / 8.0));
        c.put("distribution.arrivals.fast_interval", String.valueOf(100.0));
        c.put("spikeserver.version", "2");

        /* Specific */
        c.put("infrastructure.max_num_server", String.valueOf(maxWsNum));
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("webserver.scaling.out_thr", String.valueOf(scalingThr));
        c.put("webserver.scaling.type", scalingType);

        // extra
        c.put("infrastructure.start_num_server", String.valueOf(1));

        result.add(c);
    }
}

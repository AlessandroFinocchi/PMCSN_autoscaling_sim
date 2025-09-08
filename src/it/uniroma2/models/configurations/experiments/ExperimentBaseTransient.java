package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int index;

        /* group 1 */
        index = 100;
        setConfiguration(++index, 5.0,  6, 1, 100);

        /* group 2 */
        index = 200;
        setConfiguration(++index, 0.4,  6, 2, 100);
        setConfiguration(++index, 20.0, 6, 2, 100);

        return result;
    }

    void setConfiguration(int index, double siMax, int wsNum, int spikeVersion, double fast_interval) {
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

        /* Specific */
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("infrastructure.start_num_server", String.valueOf(wsNum));
        c.put("spikeserver.version", String.valueOf(spikeVersion));
        c.put("distribution.arrivals.fast_interval", String.valueOf(fast_interval));

        // extra
        c.put("infrastructure.max_num_server", String.valueOf(wsNum));

        result.add(c);
    }
}

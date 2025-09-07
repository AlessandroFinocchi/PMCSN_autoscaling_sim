package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.INFINITY;

public class ExperimentAdvanced2 implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int index;
        index = 100;
        for (double scalingThr = 0.1; scalingThr <= 5.0; scalingThr += 0.1) {
            setConfiguration(++index, 2.0, 10, scalingThr, true, 100, "r0");
        }
        index = 200;
        for (double scalingThr = 0.1; scalingThr <= 5.0; scalingThr += 0.1) {
            setConfiguration(++index, 2.0, 10, scalingThr, true, 100, "jobs");
        }

        return result;
    }

    void setConfiguration(
            int index,
            double siMax, int maxWsNum, Double scaling_thr,
            boolean logIntraRun, double fast_interval,
            String scalingType
    ) {
        RunConfiguration c = new RunConfiguration(String.valueOf(index));

        /* Common */
        c.put("log.fine", "true");
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / 4.0));
        c.put("distribution.services.z", "1");
        c.put("infrastructure.spikeserver.active", "true");
        c.put("spikeserver.version", "2");
        c.put("system.empty_jobs", "false");
        c.put("random.repeat_config", "1");
        c.put("stats.batch.num", "128");
        c.put("stats.batch.size", "512");
        c.put("system.stop", "INFINITY");
        c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / 8.0));

        /* Specific */
        scaling_thr = scaling_thr == null ? INFINITY : scaling_thr;
        c.put("log.intra_run", String.valueOf(logIntraRun));
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("infrastructure.start_num_server", String.valueOf(1));
        c.put("infrastructure.max_num_server", String.valueOf(maxWsNum));
        c.put("webserver.scaling.out_thr", String.valueOf(scaling_thr));
        c.put("distribution.arrivals.fast_interval", String.valueOf(fast_interval));
        c.put("webserver.scaling.type", scalingType);

        result.add(c);
    }

    private List<Double> getSiMaxList() {
        List<Double> siMaxList = new ArrayList<>();

        for (double si = 1; si < 5; si += 1) {
            siMaxList.add(si);
        }
        for (double si = 7 ; si <= 10; si += 3) {
            siMaxList.add(si);
        }

        return siMaxList;
    }
}

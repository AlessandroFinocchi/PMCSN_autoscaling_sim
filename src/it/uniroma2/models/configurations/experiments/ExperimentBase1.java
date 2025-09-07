package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBase1 implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int index;
        List<Double> siMaxList = this.getSiMaxList();

        /* group 1 */
        index = 100;
        for (Double siMax : siMaxList) {
            for (int ssVersion = 1; ssVersion <= 2; ssVersion++) {
                setConfiguration(++index, siMax, 6, ssVersion, false, 100);
            }
        }

        /* group 2 */
        index = 200;
        for (Double siMax : siMaxList) {
            for (int wsNum = 6; wsNum <= 8; wsNum++) {
                setConfiguration(++index, siMax, wsNum, 2, false, 100);
            }
        }

        /* group 3 */
        index = 300;
        setConfiguration(++index, 5, 6, 2, true, 0);
        setConfiguration(++index, 5, 6, 2, true, 100);

        return result;
    }

    void setConfiguration(int index, double siMax, int wsNum, int spikeVersion, boolean logIntraRun, double fast_interval) {
//        RunConfiguration c = new RunConfiguration("base_1_" + String.format("%02d", index));
        RunConfiguration c = new RunConfiguration(String.valueOf(index));

        /* Common */
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / 4.0));
        c.put("distribution.services.z", "1");
        c.put("infrastructure.spikeserver.active", "true");
        c.put("system.empty_jobs", "false");
        c.put("random.repeat_config", "1");
        c.put("stats.batch.num", "128");
        c.put("stats.batch.size", "512");
        c.put("system.stop", "INFINITY");
        c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / 8.0));

        /* Specific */
        c.put("log.intra_run", String.valueOf(logIntraRun));
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("infrastructure.start_num_server", String.valueOf(wsNum));
        c.put("spikeserver.version", String.valueOf(spikeVersion));
        c.put("distribution.arrivals.fast_interval", String.valueOf(fast_interval));

        // extra
        c.put("infrastructure.max_num_server", String.valueOf(wsNum));

        result.add(c);
    }

    private List<Double> getSiMaxList() {
        List<Double> siMaxList = new ArrayList<>();

        for (double si = 0.4; si < 4; si += 0.4) {
            siMaxList.add(si);
        }
        for (double si = 4.0; si < 10; si += 1) {
            siMaxList.add(si);
        }
        for (double si = 10.0; si <= 20; si += 5) {
            siMaxList.add(si);
        }

        return siMaxList;
    }
}

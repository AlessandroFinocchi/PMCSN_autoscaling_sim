package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBase1 implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        int index = 1;
        Double[] siMaxList = {1.0, 2.0, 3.0, 4.0, 6.0,
                              8.0, 10.0, 20.0, 30.0};

        for (Double siMax : siMaxList) {
            for(int wsNum=5; wsNum<8; wsNum+=1) {
                for(int ssVersion = 1; ssVersion<=2; ssVersion++) {
                    setConfiguration(index++, siMax, wsNum, ssVersion);
                }
            }
        }

        //todo: prendi i migliori 3 sistemi, levaci le fluttuazioni e osserva magari
        // che uno che con le fluttuazioni non funziona, senza funziona, oppure che
        // più in generale, aggiungere le fluttuazioni peggiora le performance.

        return result;
    }

    void setConfiguration(int index, double siMax, int wsNum, int spikeVersion) {
        RunConfiguration c = new RunConfiguration("base_1_" + String.format("%02d", index));

        /* Common */
        c.put("log.intra_run", "false");
        c.put("distribution.arrivals.type", "h2");
        c.put("distribution.services.type", "h2");
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / 4.0));
        c.put("distribution.services.z", "1");
        c.put("infrastructure.spikeserver.active", "true");
        c.put("system.empty_jobs", "false");
        c.put("random.repeat_config", "1");
        c.put("stats.batch.num", "64");
        c.put("stats.batch.size", "512");
        c.put("system.stop", "INFINITY");
        c.put("distribution.arrivals.fast_interval", "100");
        c.put("distribution.arrivals.fast_mu", String.valueOf(1.0 / 6.0));

        /* Specific */
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("infrastructure.start_num_server", String.valueOf(wsNum));
        c.put("spikeserver.version", String.valueOf(spikeVersion));

        // extra
        c.put("infrastructure.max_num_server", String.valueOf(wsNum));

        result.add(c);
    }
}

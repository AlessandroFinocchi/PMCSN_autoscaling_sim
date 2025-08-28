package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.List;

public class ExperimentSimpleBM implements Experiment {
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        Parameter parBatchNum = new Parameter("stats.batch.num").addValues("3");
        Parameter parBatchSize = new Parameter("stats.batch.size").addValues("10");
        Parameter parStopTime = new Parameter("system.stop").addValues("INFINITY");
        // Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.025");
        // Parameter parServicesZ = new Parameter("distribution.services.z").addValues("0.1");
        Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.25");
        Parameter parServicesZ = new Parameter("distribution.services.z").addValues("1");
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        /* W/o spike */
        Parameter parSpikeInactive = new Parameter("infrastructure.spikeserver.active").addValues("false");
        Parameter parStartNumServersWithoutSpike = new Parameter("infrastructure.start_num_server").addValues("6");

        List<RunConfiguration> result = ConfigurationFactory.createConfigurationsList(
                parStopTime, parBatchNum, parBatchSize,
                parArrivalMu, parServicesZ, parLogIntraRun,
                parSpikeInactive, parStartNumServersWithoutSpike
        );

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
        }

        return result;
    }
}

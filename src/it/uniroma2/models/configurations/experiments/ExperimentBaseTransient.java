package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    private static final Double Z_OVER_MU = 4.0;

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        Parameter parRepeatConfig = new Parameter("random.repeat_config").addValues("4");
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        Parameter parBatchSize = new Parameter("stats.batch.size").addValues("INFINITY");
        Parameter parStopTime = new Parameter("system.stop").addValues("10000");
        Parameter parEmptyJobs = new Parameter("system.empty_jobs").addValues("false");
        // Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.1", "0.25", "0.5");
        Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.1");

        /* W/o spike */
        Parameter parSpikeInactive = new Parameter("infrastructure.spikeserver.active").addValues("false");
        Parameter parStartNumServersWithoutSpike = new Parameter("infrastructure.start_num_server")
                .addValues("1");
        Parameter parWebServerCapacity = new Parameter("webserver.capacity")
                .addValues("5");


        /* With spike */
        Parameter parSpikeActive = new Parameter("infrastructure.spikeserver.active").addValues("true");
        Parameter parStartNumServersWithSpike = new Parameter("infrastructure.start_num_server")
                .addValues("1", "2", "3");
        Parameter parSiMax = new Parameter("infrastructure.si_max").addValues("2", "4", "10", "100", "1000");

        List<RunConfiguration> result;

        result = ConfigurationFactory.createConfigurationsList(
                parLogIntraRun, parRepeatConfig,
                parBatchSize, parEmptyJobs, parStopTime,
                parArrivalMu,
                parSpikeInactive, parStartNumServersWithoutSpike, parWebServerCapacity
        );

        // result = ConfigurationFactory.createConfigurationsList(
        //         parLogIntraRun, parStopTime, parEmptyJobs,
        //         parArrivalMu, parServicesZ,
        //         parSpikeActive, parStartNumServersWithSpike, parSiMax
        // );

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
            c.put("distribution.services.z", String.valueOf(Z_OVER_MU * Double.parseDouble(c.get("distribution.arrivals.mu"))));
        }

        return result;
    }
}

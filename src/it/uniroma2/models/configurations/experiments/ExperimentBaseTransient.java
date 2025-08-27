package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.List;

public class ExperimentBaseTransient implements Experiment {
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        Parameter parStopTime = new Parameter("system.stop").addValues("10000");
        Parameter parEmptyJobs = new  Parameter("system.empty_jobs").addValues("false");
        // Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.025");
        // Parameter parServicesZ = new Parameter("distribution.services.z").addValues("0.1");
        Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.25");
        Parameter parServicesZ = new Parameter("distribution.services.z").addValues("1");
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        /* W/o spike */
        Parameter parSpikeInactive = new Parameter("infrastructure.spikeserver.active").addValues("false");
        Parameter parStartNumServersWithoutSpike = new Parameter("infrastructure.start_num_server")
                .addValues("2", "4", "6");
        /* With spike */
        Parameter parSpikeActive = new Parameter("infrastructure.spikeserver.active").addValues("true");
        Parameter parStartNumServersWithSpike = new Parameter("infrastructure.start_num_server")
                .addValues("1", "2", "3");
        Parameter parSiMax = new Parameter("infrastructure.si_max").addValues("2", "4", "10", "100", "1000");

        List<RunConfiguration> result = ConfigurationFactory.createConfigurationsList(
                parStopTime, parEmptyJobs, parArrivalMu, parServicesZ, parLogIntraRun,
                parSpikeInactive, parStartNumServersWithoutSpike
        );

        result.addAll(ConfigurationFactory.createConfigurationsList(
                parStopTime, parEmptyJobs, parArrivalMu, parServicesZ, parLogIntraRun,
                parSpikeActive, parStartNumServersWithSpike, parSiMax
        ));

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
        }

        return result;
    }
}

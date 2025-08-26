package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.List;

public class ExperimentBaseTransitory implements Experiment {
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        Parameter parArrivalMu = new Parameter("distribution.arrivals.mu").addValues("0.025");
        Parameter parServicesZ = new Parameter("distribution.services.z").addValues("0.1");
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        // Parameter parStartNumServers = new Parameter("infrastructure.start_num_server").addValues("1", "2", "3", "4");
        Parameter parStartNumServers = new Parameter("infrastructure.start_num_server").addValues("3", "4");
        Parameter parSpikeActive = new Parameter("infrastructure.spikeserver.active").addValues("false", "true");

        List<RunConfiguration> result = ConfigurationFactory.createConfigurationsList(
                parArrivalMu, parServicesZ,
                parLogIntraRun, parStartNumServers, parSpikeActive
        );

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
        }

        return result;
    }
}

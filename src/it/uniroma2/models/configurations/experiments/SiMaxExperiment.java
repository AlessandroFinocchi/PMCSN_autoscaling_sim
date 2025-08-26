package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SiMaxExperiment implements Experiment{



    @Override
    public List<RunConfiguration> getRunConfigurations() {
        List<RunConfiguration> result = new ArrayList<>();

        /* Common */
        Parameter parArrivalMu = new Parameter("distribution.arrivals.mu")
                .addValues("0.025");

        Parameter parServicesZ = new Parameter("distribution.services.z")
                .addValues("0.1");

        Parameter parSpikeServerActive = new Parameter("infrastructure.spikeserver.active")
                .addValues("true");

        Parameter parSpikeServerInactive = new Parameter("infrastructure.spikeserver.active")
                .addValues("false");

        /* For single server w/o spike */
        Parameter parStartNumServersSingleServer = new Parameter("infrastructure.start_num_server")
                .addValues("1");

        Parameter parWebServerCapacity = new Parameter("webserver.capacity")
                .addValues("16", "8", "4", "2");

        List<RunConfiguration> configurationsWithSingleServer = ConfigurationFactory
                .createConfigurationsList(parArrivalMu, parServicesZ, parSpikeServerInactive, parStartNumServersSingleServer, parWebServerCapacity);

        /* For multiple server w/o spike */
        Parameter parStartNumServersWithoutSpike = new Parameter("infrastructure.start_num_server")
                .addValues("16", "8", "4", "2");

        List<RunConfiguration> configurationsWithoutSpike = ConfigurationFactory
                .createConfigurationsList(parArrivalMu, parServicesZ, parStartNumServersWithoutSpike, parSpikeServerInactive);

        /* For multiple server with spike */
        Parameter parStartNumServersWithSpike = new Parameter("infrastructure.start_num_server")
                .addValues("4", "3", "2", "1");
        // .addValues("8", "4", "2", "1");

        Parameter parSiMax = new Parameter("infrastructure.si_max");
        for (int si = 10000; si > 1000; si -= 1000) {
            parSiMax.addValues(String.valueOf(si));
        }
        for (int si = 1000; si > 100; si -= 100) {
            parSiMax.addValues(String.valueOf(si));
        }
        for (int si = 100; si > 20; si -= 10) {
            parSiMax.addValues(String.valueOf(si));
        }
        for (int si = 20; si >= 1; si--) {
            parSiMax.addValues(String.valueOf(si));
        }

        Parameter parSpikeServerCapacity = new Parameter("spikeserver.capacity")
                .addValues("1", "0.5", "0.25", "0.125");

        List<RunConfiguration> configurationsWithSpike = ConfigurationFactory.createConfigurationsList(
                parArrivalMu, parServicesZ, parSpikeServerActive,
                parSiMax, parSpikeServerCapacity, parStartNumServersWithSpike
        );

        /* Add all to configurations */
        result.addAll(configurationsWithSingleServer);
        // result.addAll(configurationsWithoutSpike);
        // result.addAll(configurationsWithSpike);

        for (RunConfiguration c : result) {
            c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
        }

        return result;
    }
}

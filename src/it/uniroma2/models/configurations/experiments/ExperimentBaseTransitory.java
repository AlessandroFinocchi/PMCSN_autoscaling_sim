package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBaseTransitory implements Experiment{
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        Parameter parSpikeActive = new Parameter("infrastructure.spikeserver.active").addValues("false");

        return ConfigurationFactory.createConfigurationsList(parLogIntraRun, parSpikeActive);
    }
}

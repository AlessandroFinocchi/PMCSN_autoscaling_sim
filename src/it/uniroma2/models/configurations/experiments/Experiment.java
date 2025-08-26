package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.List;

public interface Experiment {

    List<RunConfiguration> getRunConfigurations();
}

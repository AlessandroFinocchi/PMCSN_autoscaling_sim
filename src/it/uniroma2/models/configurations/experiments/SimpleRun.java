package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SimpleRun implements Experiment {
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        RunConfiguration c = new RunConfiguration("simple_run");
        List<RunConfiguration> result = new ArrayList<>();
        result.add(c);
        return result;
    }
}

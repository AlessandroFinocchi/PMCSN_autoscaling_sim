package it.uniroma2.controllers.configurations;

import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.configurations.MultiCounter;
import it.uniroma2.models.configurations.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFactory {

    private static int EXP_COUNTER = 0;

    /**
     * Return a list created as the cartesian product of all the possible parameters values.
     */
    public static List<RunConfiguration> createConfigurationsList(List<Parameter> parameters) {
        List<RunConfiguration> result = new ArrayList<>();

        // MultiCounter allows the translation from a single index to multiple subindexes
        MultiCounter mc = new MultiCounter(parameters.size());
        int totalNumber = 1;
        for (int i = 0; i < parameters.size(); i++) {
            mc.getMaxCounter()[i] = parameters.get(i).getValues().size();
            totalNumber *= parameters.get(i).getValues().size();
        }

        // For each possible configuration
        for (int i = 0; i < totalNumber; i++) {
            RunConfiguration c = new RunConfiguration("exp_" + EXP_COUNTER++);
            // For each parameter
            for (int j = 0; j < parameters.size(); j++) {
                String parameterName = parameters.get(j).getName();
                // Take the parameter values using its subindex
                String parameterValue = parameters.get(j).getValues().get(mc.getCounter()[j]);
                c.put(parameterName, parameterValue);
            }
            result.add(c);
            mc.increment();
        }

        return result;
    }
}

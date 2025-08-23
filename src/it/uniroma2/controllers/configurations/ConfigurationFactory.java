package it.uniroma2.controllers.configurations;

import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.configurations.MultiCounter;
import it.uniroma2.models.configurations.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFactory {

    /**
     * Return a list created as the cartesian product of all the possible parameters values.
     */
    public static List<RunConfiguration> createConfigurationsList(Parameter... parameters) {
        List<RunConfiguration> result = new ArrayList<>();

        // MultiCounter allows the translation from a single index to multiple subindexes
        MultiCounter mc = new MultiCounter(parameters.length);
        int totalNumber = 1;
        for (int i = 0; i < parameters.length; i++) {
            mc.getMaxCounter()[i] = parameters[i].getValues().size();
            totalNumber *= parameters[i].getValues().size();
        }

        // For each possible configuration
        for (int i = 0; i < totalNumber; i++) {
            RunConfiguration c = new RunConfiguration("exp_" + i);
            // For each parameter
            for (int j = 0; j < parameters.length; j++) {
                String parameterName = parameters[j].getName();
                // Take the parameter values using its subindex
                String parameterValue = parameters[j].getValues().get(mc.getCounter()[j]);
                c.put(parameterName, parameterValue);
            }
            result.add(c);
            mc.increment();
        }

        return result;
    }
}

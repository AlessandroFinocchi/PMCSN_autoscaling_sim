package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAdvancedTransient implements Experiment {
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        List<Parameter> commonPars = new ArrayList<>();
        commonPars.add(new Parameter("random.repeat_config").addValues("4"));
        commonPars.add(new Parameter("log.intra_run").addValues("true"));
        commonPars.add(new Parameter("stats.batch.size").addValues("INFINITY"));
        commonPars.add(new Parameter("system.stop").addValues("10000"));
        commonPars.add(new Parameter("system.empty_jobs").addValues("false"));
        commonPars.add(new Parameter("distribution.arrivals.mu").addValues(
                String.valueOf(1.0 / 4.0),
                String.valueOf(1.0 / 8.0)
        ));
        commonPars.add(new Parameter("distribution.services.z").addValues(String.valueOf(1.0)));
        commonPars.add(new Parameter("distribution.turn_on.mu").addValues("0.0"));
        commonPars.add(new Parameter("distribution.turn_on.std").addValues("0.0"));

        /* 0th group of experiment */
        List<Parameter> exp0Pars = new ArrayList<>(commonPars);
        exp0Pars.add(new Parameter("infrastructure.start_num_server").addValues("5", "8", "10"));
        exp0Pars.add(new Parameter("infrastructure.max_num_server").addValues("10"));
        exp0Pars.add(new Parameter("infrastructure.spikeserver.active").addValues("false"));
        exp0Pars.add(new Parameter("webserver.scaling.in_thr").addValues("0"));
        exp0Pars.add(new Parameter("webserver.scaling.out_thr").addValues("INFINITY"));

        List<RunConfiguration> group_0 = ConfigurationFactory.createConfigurationsList(exp0Pars);
        result.addAll(group_0);

        // /* 1st group of experiment */
        List<Parameter> exp1Pars = new ArrayList<>(commonPars);
        exp1Pars.add(new Parameter("infrastructure.max_num_server").addValues("10"));
        exp1Pars.add(new Parameter("webserver.scaling.in_thr")
                .addValues("2", "3", "4", "5", "8", "10", "20"));
        exp1Pars.add(new Parameter("infrastructure.spikeserver.active").addValues("false", "true"));
        exp1Pars.add(new Parameter("webserver.scaling.out_thr").addValues("INFINITY"));

        List<RunConfiguration> group_1 = ConfigurationFactory.createConfigurationsList(exp1Pars);
        for (RunConfiguration c : group_1) {
            Integer wsStart = (int) Math.floor(Double.parseDouble(c.get("infrastructure.max_num_server")) / 2.0);
            c.put("infrastructure.start_num_server", String.valueOf(wsStart));
            c.put("webserver.scaling.out_thr", c.get("webserver.scaling.in_thr"));
        }
        result.addAll(group_1);

        /* Common */

        return result;
    }
}

package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.ConfigurationFactory;
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
        commonPars.add(new Parameter("random.repeat_config").addValues("1"));
        commonPars.add(new Parameter("log.intra_run").addValues("true"));
        commonPars.add(new Parameter("stats.batch.size").addValues("INFINITY"));
        commonPars.add(new Parameter("system.stop").addValues("10000"));
        commonPars.add(new Parameter("system.empty_jobs").addValues("false"));
        commonPars.add(new Parameter("distribution.arrivals.mu").addValues(String.valueOf(1.0 / 4.0)));
        commonPars.add(new Parameter("distribution.services.z").addValues(String.valueOf(1.0)));
        commonPars.add(new Parameter("distribution.turn_on.mu").addValues("5"));
        commonPars.add(new Parameter("distribution.turn_on.std").addValues("0.5"));
        commonPars.add(new Parameter("distribution.arrivals.fast_interval").addValues("0", "100"));

        /* 0th group of experiment */
        List<Parameter> exp0Pars = new ArrayList<>(commonPars);
        exp0Pars.add(new Parameter("infrastructure.start_num_server").addValues("5"));
        exp0Pars.add(new Parameter("infrastructure.max_num_server").addValues("11"));
        exp0Pars.add(new Parameter("infrastructure.spikeserver.active").addValues("false"));
        exp0Pars.add(new Parameter("webserver.scaling.out_thr").addValues("INFINITY"));
        exp0Pars.add(new Parameter("infrastructure.si_max").addValues("-1.0"));

        List<RunConfiguration> group_0 = ConfigurationFactory.createConfigurationsList(exp0Pars);
        result.addAll(group_0);

        // /* 1st and 2nd group of experiment */
        List<Parameter> exp1Pars = new ArrayList<>(commonPars);
        exp1Pars.add(new Parameter("infrastructure.max_num_server").addValues("10"));
        // exp1Pars.add(new Parameter("infrastructure.spikeserver.active").addValues("false", "true"));
        exp1Pars.add(new Parameter("infrastructure.spikeserver.active").addValues("false", "true"));
        // exp1Pars.add(new Parameter("spikeserver.version").addValues("1", "2"));
        // exp1Pars.add(new Parameter("si_max_ratio").addValues(
        //         "0.0", "0.05", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6",
        //         "0.7", "0.8", "0.9", "1.0", "1.1", "1.2"
        // ));
        exp1Pars.add(new Parameter("webserver.scaling.out_thr")
                .addValues( "0.1", "0.5", "2", "4", "8", "20"));

        List<RunConfiguration> group_1 = ConfigurationFactory.createConfigurationsList(exp1Pars);
        for (RunConfiguration c : group_1) {
            Integer wsStart = (int) Math.floor(Double.parseDouble(c.get("infrastructure.max_num_server")) / 2.0);
            c.put("infrastructure.start_num_server", String.valueOf(wsStart));

            if (c.get("infrastructure.spikeserver.active").equals("true")){
                double scalingOutThr = Double.parseDouble(c.get("webserver.scaling.out_thr"));
                c.put("infrastructure.si_max", String.valueOf(0.9 * scalingOutThr));
                // double scalingOutRatio = Double.parseDouble(c.get("si_max_ratio"));
                // c.put("infrastructure.si_max", String.valueOf(scalingOutRatio * scalingOutThr));
            } else {
                c.put("infrastructure.si_max", "-1.0");
                int wsMax = Integer.parseInt(c.get("infrastructure.max_num_server"));
                c.put("infrastructure.max_num_server", String.valueOf(wsMax + 1));
            }
        }
        result.addAll(group_1);

        /* Common */

        return result;
    }
}

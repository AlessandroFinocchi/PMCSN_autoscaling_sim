package it.uniroma2.models.configurations.experiments;

import it.uniroma2.models.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBase1 implements Experiment {
    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("log.intra_run").addValues("false"));
        parameters.add(new Parameter("distribution.arrivals.mu").addValues(String.valueOf(1.0 / 4.0)));
        parameters.add(new Parameter("distribution.services.z").addValues("1"));
        parameters.add(new Parameter("infrastructure.start_num_server").addValues("5"));
        parameters.add(new Parameter("infrastructure.max_num_server").addValues("5"));
        parameters.add(new Parameter("system.empty_jobs").addValues("false"));
        parameters.add(new Parameter("random.repeat_config").addValues("1"));
        parameters.add(new Parameter("stats.batch.num").addValues("64"));
        parameters.add(new Parameter("stats.batch.size").addValues("512"));
        parameters.add(new Parameter("system.stop").addValues("100000"));

        Parameter parSiMax = new Parameter("infrastructure.si_max");
        parameters.add(parSiMax);
        for (int si=1; si<10; si+=1) {
            parSiMax.addValues(String.valueOf(si));
        }
        parSiMax.addValues(String.valueOf(20));
        parSiMax.addValues(String.valueOf(40));
        parSiMax.addValues(String.valueOf(60));

        List<RunConfiguration> result = ConfigurationFactory.createConfigurationsList(parameters);

        for(RunConfiguration run : result){
            run.setName("base_1_" + (result.indexOf(run) + 1));
        }

        return result;
    }
}

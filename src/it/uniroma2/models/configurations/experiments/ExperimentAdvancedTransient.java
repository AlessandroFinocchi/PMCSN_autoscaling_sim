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
        Parameter parRepeat = new Parameter("random.repeat_config").addValues("3");
        Parameter parLogIntraRun = new Parameter("log.intra_run").addValues("true");
        Parameter parBatchSize = new Parameter("stats.batch.size").addValues("INFINITY");
        Parameter parStop = new Parameter("system.stop").addValues("10000");
        Parameter parEmptyJobs = new Parameter("system.empty_jobs").addValues("false");
        Parameter parLambda = new Parameter("distribution.arrivals.mu").addValues(String.valueOf(1.0 / 4.0));
        Parameter parZ = new Parameter("distribution.services.z").addValues(String.valueOf(1.0));

        /* 1st group of experiment */
        Parameter parWsMax_1 = new Parameter("infrastructure.max_num_server").addValues("10");
        Parameter parSpikeActive_1 = new Parameter("infrastructure.spikeserver.active").addValues("false");
        Parameter parInThr_1 = new Parameter("webserver.response_time.in_thr")
                .addValues("0.1", "0.5", "1", "5", "10", "50");

        List<RunConfiguration> group_1 = ConfigurationFactory.createConfigurationsList(
                parRepeat, parLogIntraRun, parBatchSize, parStop, parEmptyJobs,
                parLambda, parZ,
                parWsMax_1, parSpikeActive_1, parInThr_1
        );
        for (RunConfiguration c : group_1) {
            c.put("webserver.response_time.out_thr", c.get("webserver.response_time.in_thr"));
        }
        result.addAll(group_1);

        /* Common */
        for (RunConfiguration c : result) {
            Integer wsStart = (int) Math.floor(Double.parseDouble(c.get("infrastructure.max_num_server")) / 2.0);
            c.put("infrastructure.start_num_server", String.valueOf(wsStart));
        }

        return result;
    }
}

package it.uniroma2.models.configurations.experiments;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBase1 implements Experiment {
    List<Parameter> commonParameters = new ArrayList<>();
    List<RunConfiguration> result = new ArrayList<>();

    @Override
    public List<RunConfiguration> getRunConfigurations() {
        /* Common */
        Parameter parLog1 = new Parameter("log.intra_run").addValues("false");
        Parameter parLog2 = new Parameter("log.bm").addValues("true");
        Parameter parArrivalsMu = new Parameter("distribution.arrivals.mu").addValues(String.valueOf(1.0 / 4.0));
        Parameter parServicesZ = new Parameter("distribution.services.z").addValues("1");
        Parameter parStartNumWS = new Parameter("infrastructure.start_num_server").addValues("5");
        Parameter parMaxNumWS = new Parameter("infrastructure.max_num_server").addValues("5");
        Parameter parEmptyJob = new Parameter("system.empty_jobs").addValues("false");
        Parameter parRepeat = new Parameter("random.repeat_config").addValues("1");
        Parameter parBatchSize = new Parameter("stats.batch.size").addValues("INFINITY");
        Parameter parSystemStop = new Parameter("system.stop").addValues("10000");

        Parameter parSiMax = new Parameter("infrastructure.si_max");
        Parameter parSLO = new Parameter("infrastructure.response_time_slo");
        for (int si=2; si<=10; si+=2) {
//            for (int si = 2; si <= 2; si+= 2) {
            parSiMax.addValues(String.valueOf(si));
        }
            for (double slo = 2; slo <= 6; slo += 1) {
//                for (double slo=2.0; slo <= 2.0; slo+= 0.5) {
            parSLO.addValues(String.valueOf(slo));
        }

        List<RunConfiguration> result = ConfigurationFactory.createConfigurationsList(
                parLog1, parLog2, parArrivalsMu, parServicesZ, parStartNumWS, parMaxNumWS,
                parEmptyJob, parRepeat, parBatchSize, parSystemStop, parSiMax, parSLO
        );

        for(RunConfiguration run : result){
            run.setName("base_" + (result.indexOf(run) + 1));
        }

        return result;
    }

    void addParameter(String parName, String... parValues) {
        commonParameters.add(new Parameter(parName).addValues(parValues));
    }

    void setConfiguration(int index, double lambda, double z, int wsNumber, boolean ssActive,
                          double siMax, double responseTimeSLO) {
        RunConfiguration c = result.get(index - 1);
        c.put("distribution.arrivals.mu", String.valueOf(1.0 / lambda));
        c.put("distribution.services.z", String.valueOf(z));
        c.put("infrastructure.start_num_server", String.valueOf(wsNumber));
        c.put("infrastructure.spikeserver.active", String.valueOf(ssActive));
        c.put("infrastructure.si_max", String.valueOf(siMax));
        c.put("infrastructure.response_time_slo", String.valueOf(responseTimeSLO));

        // extra
        c.put("infrastructure.max_num_server", c.get("infrastructure.start_num_server"));
    }
}

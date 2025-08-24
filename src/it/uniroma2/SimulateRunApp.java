package it.uniroma2;

import it.uniroma2.controllers.configurations.ConfigurationFactory;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Config;
import it.uniroma2.models.configurations.Parameter;
import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.distr.Normal;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.DataCSVWriter;
import it.uniroma2.utils.ProgressBar;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SimulateRunApp {

    private static final Rngs R = new Rngs();
    private static int REPEAT_CONFIGURATION;
    private static List<RunConfiguration> configurations = new ArrayList<>();

    public static void main(String[] args) throws IllegalLifeException {
        INTRA_RUN_DATA.setWritable(false);

        createConfigurations();

        for (RunConfiguration c : configurations) {
            setup(c);
            for (int i = 0; i < REPEAT_CONFIGURATION; i++) {
                run(c, i);
            }
        }
    }

    private static void createConfigurations() {
        REPEAT_CONFIGURATION = 1;

        List<Parameter> parameters = new ArrayList<>();

        Parameter parMaxServer = new Parameter("infrastructure.max_num_server");
        parMaxServer.addValues("10");
        parameters.add(parMaxServer);

        Parameter parStartNumServers = new Parameter("infrastructure.start_num_server");
        // parStartNumServers.addValues("10", "9", "8", "7", "6", "5", "4", "3");
        parStartNumServers.addValues("2");
        parameters.add(parStartNumServers);

        Parameter parMaxNumServers = new Parameter("infrastructure.spikeserver.active");
        parMaxNumServers.addValues("false");
        parameters.add(parMaxNumServers);

        Parameter parScheduler = new Parameter("infrastructure.scheduler");
        parScheduler.addValues("leastUsed");
        parameters.add(parScheduler);

        configurations = ConfigurationFactory.createConfigurationsList(parameters.toArray(Parameter[]::new));


        System.out.printf("Created %d configurations\n\n", configurations.size());
    }

    private static void setup(RunConfiguration c) {
        /* Reload default configuration */
        /* Update experiment specific configuration */
        Config.load(c);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, CONFIGURATION_ID, c.getName());
    }

    private static void run(RunConfiguration c, int repetition) throws IllegalLifeException {
        System.out.println();
        System.out.println(c.toString());
        System.out.println(repetition + 1 + " / " + REPEAT_CONFIGURATION + " repetition");
        System.out.println("---------------------------------------");

        INTER_RUN_DATA.addField(INTER_RUN_KEY, RUN_ID, repetition);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, RUN_DATETIME, String.valueOf(LocalDateTime.now()));

        /* Plant the seeds only if the first running the new configuration */
        if (repetition == 0) {
            R.plantSeeds(SEED);
        }

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA = new Exponential(R, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(R, 1, SERVICES_Z);
        Distribution turnOnVA = new Normal(R, 2, TURN_ON_MU, TURN_ON_STD);

        /* Log to CSV the initial seed for each stream for replayability */
        for (int stream = 0; stream < TOTAL_STREAMS; stream++) {
            R.selectStream(stream);
            INTER_RUN_DATA.addFieldWithSuffix(INTER_RUN_KEY, STREAM_SEED, String.valueOf(stream), R.getSeed());
        }

        /* Compute first arrival time */
        double nextArrival = arrivalVA.gen();
        Event firstArrival = new ArrivalEvent(nextArrival);
        Event firstCompletion = new CompletionEvent(INFINITY);

        /* Setup event schedule */
        EventCalendar calendar = new EventCalendar();
        calendar.addEvent(firstArrival);
        calendar.addEvent(firstCompletion);

        /* Setup state of the system */
        SystemState s = new SystemState(calendar, arrivalVA, servicesVA, turnOnVA);

        ProgressBar bar = new ProgressBar(STOP);
        while (s.getCurrent() < STOP || s.activeJobExists()) {
            /* Compute the next event time */
            Event nextEvent = calendar.nextEvent();
            bar.update(nextEvent.getTimestamp());

            nextEvent.process(s, visitor);
        }

        s.printStats();

        try {
            DataCSVWriter.flushAllInter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
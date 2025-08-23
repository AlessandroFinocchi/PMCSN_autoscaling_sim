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
import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_DATA;
import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_KEY;
import static it.uniroma2.utils.DataField.*;

public class SimulateRunApp {

    private static final Rngs R = new Rngs();
    private static final int REPEAT_CONFIGURATION = 3;
    private static List<RunConfiguration> configurations = new ArrayList<>();

    public static void main(String[] args) throws IllegalLifeException {
        createConfigurations();

        for (RunConfiguration c : configurations) {
            setup(c);
            for (int i = 0; i < REPEAT_CONFIGURATION; i++) {
                run(i);
            }
        }
    }

    private static void createConfigurations() {
        Parameter parStartNumServers = new Parameter("infrastructure.start_num_server");
        parStartNumServers.addValues("5", "4");

        Parameter parMaxNumServers = new Parameter("infrastructure.spikeserver.active");
        parMaxNumServers.addValues("false", "true");

        configurations = ConfigurationFactory.createConfigurationsList(parStartNumServers, parMaxNumServers);
    }

    private static void setup(RunConfiguration c) {
        /* Reload default configuration */
        /* Update experiment specific configuration */
        Config.load(c);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, CONFIGURATION_ID, c.getName());
    }

    private static void run(int repetition) throws IllegalLifeException {
        INTER_RUN_DATA.overwriteField(INTER_RUN_KEY, RUN_ID, repetition);

        if (repetition == 0) {
            R.plantSeeds(SEED);
        }

        INTER_RUN_DATA.addField(INTER_RUN_KEY, RUN_SEED, R.getSeed());

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA = new Exponential(R, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(R, 1, SERVICES_Z);
        Distribution turnOnVA = new Normal(R, 2, TURN_ON_MU, TURN_ON_STD);

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
            DataCSVWriter.flushAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
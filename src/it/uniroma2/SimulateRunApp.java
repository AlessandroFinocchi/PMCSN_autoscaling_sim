package it.uniroma2;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Config;
import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.distributions.CHyperExponential;
import it.uniroma2.models.distributions.Distribution;
import it.uniroma2.models.distributions.DistributionFactory;
import it.uniroma2.models.distributions.Normal;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.DataCSVWriter;
import it.uniroma2.utils.ProgressBar;

import java.time.LocalDateTime;
import java.util.List;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SimulateRunApp {

    private static final Rngs R = new Rngs();
    private static List<RunConfiguration> configurations;

    public static void main(String[] args) throws IllegalLifeException {
        configurations = Config.createConfigurations();
        for (RunConfiguration c : configurations) {
            setup(c);
            for (int i = 0; i < REPEAT_CONFIGURATION; i++) {
                System.out.println();
                System.out.println(c.getDescription());
                System.out.println(configurations.indexOf(c) + 1 + " / " + configurations.size() + " configuration");
                System.out.println(i + 1 + " / " + REPEAT_CONFIGURATION + " repetition");
                System.out.println("---------------------------------------");
                run(c, i);
                log(c, i);
            }
        }

        System.out.println("\n\nSimulation(s) completed:");
        for (RunConfiguration c : configurations) {
            System.out.println("\t" + c.getDescription());
        }
    }

    private static void setup(RunConfiguration c) {
        /* Reload default configuration */
        /* Update experiment specific configuration */
        Config.load(c);
        INTRA_RUN_DATA.setWritable(LOG_INTRA_RUN);
        INTRA_RUN_BM_DATA.setWritable(LOG_BM);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, CONFIGURATION_ID, c.getName());
        INTER_RUN_DATA.addField(INTER_RUN_KEY, CONFIGURATION_DESCRIPTION, c.getDescription());
    }

    private static void run(RunConfiguration c, int repetition) throws IllegalLifeException {
        INTER_RUN_DATA.addField(INTER_RUN_KEY, REPETITION_ID, repetition);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, RUN_DATETIME, String.valueOf(LocalDateTime.now()));

        /* Plant the seeds only if the first running the new configuration */
        if (repetition == 0) {
            R.plantSeeds(SEED);
        }

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA = DistributionFactory.createArrivalDistribution(R);
        Distribution servicesVA = DistributionFactory.createServiceDistribution(R);
        Distribution turnOnVA = new Normal(R, 6, 7, TURN_ON_MU, TURN_ON_STD);

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
        while (continueSimulating(s)) {
            /* Compute the next event time */
            Event nextEvent = calendar.nextEvent();
            bar.update(nextEvent.getTimestamp());

            nextEvent.process(s, visitor);
        }

        s.printStats();
    }

    private static boolean continueSimulating(SystemState s) {
        return (s.getCurrent() < STOP || (EMPTY_JOBS && s.activeJobExists()))
                && !s.getServers().isCompletedStationaryStats();
    }

    private static void log(RunConfiguration c, int repetition) {
        if (LOG_INTRA_RUN) DataCSVWriter.flushAllIntra(c, repetition);
        if (LOG_BM) DataCSVWriter.flushAllIntraBM(c, repetition);
        DataCSVWriter.flushAllInter();
    }

}
package it.uniroma2;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Config;
import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.distr.CHyperExponential;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Normal;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.DataCSVWriter;
import it.uniroma2.utils.ProgressBar;

import java.io.IOException;
import java.time.LocalDateTime;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SimulateRunApp {

    private static final Rngs R = new Rngs();

    public static void main(String[] args) throws IllegalLifeException {
        for (RunConfiguration c : createConfigurations()) {
            setup(c);
            for (int i = 0; i < REPEAT_CONFIGURATION; i++) {
                run(c, i);
                log();
            }
        }
    }

    private static void setup(RunConfiguration c) {
        /* Reload default configuration */
        /* Update experiment specific configuration */
        Config.load(c);
        INTRA_RUN_DATA.setWritable(LOG_INTRA_RUN);
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

        // Distribution arrivalVA = new Exponential(R, 0, ARRIVALS_MU);
        // Distribution servicesVA = new Exponential(R, 1, SERVICES_Z);
        Distribution arrivalVA = new CHyperExponential(R, 4, ARRIVALS_MU, 0, 1, 2);
        Distribution servicesVA = new CHyperExponential(R, 4, SERVICES_Z, 3, 4, 5);

        Distribution turnOnVA = new Normal(R, 6, TURN_ON_MU, TURN_ON_STD);

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
    }

    private static void log() {
        if (LOG_INTRA_RUN) DataCSVWriter.flushAllIntra();
        DataCSVWriter.flushAllInter();
    }

}
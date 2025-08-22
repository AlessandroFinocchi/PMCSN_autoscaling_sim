package it.uniroma2;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Config;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.distr.Normal;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.DataCSVWriter;
import it.uniroma2.utils.DataField;
import it.uniroma2.utils.ProgressBar;

import java.io.IOException;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_DATA;

public class SimulateRunApp {
    
    private static int[] EXP_START_NUM_SERVERS;

    public static void main(String[] args) throws IllegalLifeException {
        EXP_START_NUM_SERVERS = new int[]{5, 4, 3, 2};
        
        for (int i = 0; i < EXP_START_NUM_SERVERS.length; i++) {
            setup(i);
            run();
        }
    }

    private static void setup(int runID) {
        /* Reload default configuration */
        Config.load();
        INTER_RUN_DATA.addField(DataCSVWriter.INTER_RUN_KEY, DataField.RUN_ID, runID);

        /* Update experiment specific configuration */
        START_NUM_SERVERS = EXP_START_NUM_SERVERS[runID];
    }

    private static void run() throws IllegalLifeException {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(r, 1, SERVICES_Z);
        Distribution turnOnVA = new Normal(r, 2, TURN_ON_MU, TURN_ON_STD);

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
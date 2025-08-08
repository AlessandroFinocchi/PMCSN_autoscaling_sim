package it.uniroma2;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.ProgressBar;

import static it.uniroma2.models.Config.*;

public class ExampleApp {

    public static void main(String[] args) throws IllegalLifeException {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA  = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(r, 1, SERVICES_Z);

        /* Compute first arrival time */
        double nextArrival = arrivalVA.gen();
        Event firstArrival = new ArrivalEvent(nextArrival);
        Event firstCompletion = new CompletionEvent(INFINITY);

        /* Setup event schedule */
        EventCalendar calendar = new EventCalendar();
        calendar.addEvent(firstArrival);
        calendar.addEvent(firstCompletion);

        /* Setup state of the system */
        SystemState s = new SystemState(calendar, arrivalVA, servicesVA);

        ProgressBar bar = new ProgressBar(STOP);
        while (s.getCurrent() < STOP || s.activeJobExists()) {
            /* Compute the next event time */
            Event nextEvent = calendar.nextEvent();
            bar.update(nextEvent.getTimestamp());

            nextEvent.process(s, visitor);
        }

        s.printStats();
    }

}
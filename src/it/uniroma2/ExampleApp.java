package it.uniroma2;

import it.uniroma2.exceptions.JobCompletionException;
import it.uniroma2.libs.Rngs;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.utils.ProgressBar;

import static it.uniroma2.models.Config.*;

public class ExampleApp {

    public static void main(String[] args) throws JobCompletionException {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA  = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(r, 1, SERVICES_Z);

        long completedJobs  = 0;                  /* used to count departed jobs         */

        /* Compute first arrival time */
        double nextArrival = arrivalVA.gen();
        double nextCompletion = INFINITY;
        Event firstArrival = new ArrivalEvent(nextArrival);
        Event firstCompletion = new CompletionEvent(nextCompletion);

        /* Setup event schedule */
        EventCalendar calendar = new EventCalendar();
        calendar.addEvent(firstArrival);
        calendar.addEvent(firstCompletion);

        /* Setup state of the system */
        SystemState s = new SystemState(calendar, arrivalVA, servicesVA);
        //21070.448522268824
        ProgressBar bar = new ProgressBar(STOP);
        while (s.getCurrent() < STOP || s.jobActiveExist()) {
            /* Compute the next event time */
            Event nextEvent = calendar.nextEvent();
            bar.update(nextEvent.getTimestamp());
            if(s.getCurrent() > 21070) {
                System.out.println("mannaggetta");
            }

            nextEvent.process(s, visitor);
        }

//        /* Print results */
//        DecimalFormat f = new DecimalFormat("###0.00");
//
//        System.out.println("\nfor " + completedJobs + " jobs");
//        System.out.println("   average interarrival time =   " + f.format(s.getLast() / completedJobs));
//        System.out.println("   average wait ............ =   " + f.format(sum.node / completedJobs));
//        System.out.println("   average delay ........... =   " + f.format(sum.queue / completedJobs));
//        System.out.println("   average service time .... =   " + f.format(sum.service / completedJobs));
//        System.out.println("   average # in the node ... =   " + f.format(sum.node / s.getCurrent()));
//        System.out.println("   average # in the queue .. =   " + f.format(sum.queue / s.getCurrent()));
//        System.out.println("   utilization ............. =   " + f.format(sum.service / s.getCurrent()));
    }

}
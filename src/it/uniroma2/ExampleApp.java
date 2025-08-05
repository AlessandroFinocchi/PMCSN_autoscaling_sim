package it.uniroma2;

import it.uniroma2.libs.Rngs;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.events.*;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.models.sys.SystemStats;
import it.uniroma2.utils.ProgressBar;

import java.text.DecimalFormat;

import static it.uniroma2.models.Config.*;

public class ExampleApp {

    public static void main(String[] args)  {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        EventVisitor visitor = new EventProcessor();

        Distribution arrivalVA  = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Exponential(r, 1, SERVICES_Z);

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
        SystemStats stats = new SystemStats();

        ProgressBar bar = new ProgressBar(STOP);
        while (s.getCurrent() < STOP || s.jobActiveExist()) {
            /* Compute the next event time */
            Event nextEvent = calendar.nextEvent();
            bar.update(nextEvent.getTimestamp());

            nextEvent.process(s, stats, visitor);
        }

        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00000000");

        System.out.println("\nfor " + stats.getCompletedJobs() + " jobs");
        System.out.println("   average interarrival time =   " + f.format(s.getCurrent() / stats.getCompletedJobs()));
        System.out.println("   average response time ... =   " + f.format(stats.getNodeSum() / stats.getCompletedJobs()));
        System.out.println("   average waiting time .... =   " + f.format(stats.getQueueSum() / stats.getCompletedJobs()));
        System.out.println("   average service time .... =   " + f.format(stats.getServiceSum() / stats.getCompletedJobs()));
        System.out.println("   average # in the node ... =   " + f.format(stats.getNodeSum() / s.getCurrent()));
        System.out.println("   average # in the queue .. =   " + f.format(stats.getQueueSum() / s.getCurrent()));
        System.out.println("   utilization ............. =   " + f.format(stats.getServiceSum() / s.getCurrent()));
    }

}
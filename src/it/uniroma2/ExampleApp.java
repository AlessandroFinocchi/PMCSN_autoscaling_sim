package it.uniroma2;

import it.uniroma2.libs.Rngs;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.distr.Exponential;
import it.uniroma2.models.distr.Uniform;
import it.uniroma2.models.sys.SystemState;
import it.uniroma2.models.sys.SystemStatsSum;
import it.uniroma2.utils.ProgressBar;

import java.text.DecimalFormat;

import static it.uniroma2.models.Config.*;

/**
 * Riprendi da:
 * - LIBRO      p.208   paragrafo inizio
 */


public class ExampleApp {

    public static void main(String[] args) {

        long completedJobs  = 0;                  /* used to count departed jobs         */
        long activeJobs = 0;                  /* number in the node                  */
        double nextArrival = START;
        double nextCompletion = INFINITY;

        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        Distribution arrivalVA  = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Uniform(r, 1, SERVICES_MIN, SERVICES_MAX);

        /* Compute first arrival time */
        nextArrival += arrivalVA.gen();

        SystemStatsSum sum = new SystemStatsSum();
        SystemState s = new SystemState(
                nextArrival,
                nextCompletion,
                START
        );

        ProgressBar bar = new ProgressBar(STOP);

        while (s.getArrival() < STOP || activeJobs > 0) {
            bar.update(s.getCurrent());

            /* Compute the next event time */
            double nextEvent = Math.min(s.getArrival(), s.getCompletion());
            s.setNext(nextEvent);

            /* Update statistics */
            if (activeJobs > 0) {
                double duration = (s.getNext() - s.getCurrent());
                sum.node    += duration * activeJobs;
                sum.queue   += duration * (activeJobs - 1);
                sum.service += duration;
            }

            /* Handle the next event */
            s.setCurrent(s.getNext());

            if (s.getCurrent() == s.getArrival()) {
                activeJobs++;
                /* Compute the next arrival */
                nextArrival += arrivalVA.gen();
                s.setArrival(nextArrival);
                /* If the next arrival is after then end of the simulation, then register it */
                if (s.getArrival() > STOP) {
                    s.setLast(s.getCurrent());
                    s.setArrival(INFINITY);
                }
                /* If is the first active job, then compute its completion time */
                if (activeJobs == 1){
                    nextCompletion = s.getCurrent() + servicesVA.gen();
                    s.setCompletion(nextCompletion);
                }
            }
            else if (s.getCurrent() == s.getCompletion()) {
                completedJobs++;
                activeJobs--;
                /* Compute the completion time of the next job */
                if (activeJobs == 0) {
                    nextCompletion = INFINITY;
                } else {
                    nextCompletion = s.getCurrent() + servicesVA.gen();
                }
                s.setCompletion(nextCompletion);
            }
        }

        /* Print results */
        DecimalFormat f = new DecimalFormat("###0.00");

        System.out.println("\nfor " + completedJobs + " jobs");
        System.out.println("   average interarrival time =   " + f.format(s.getLast() / completedJobs));
        System.out.println("   average wait ............ =   " + f.format(sum.node / completedJobs));
        System.out.println("   average delay ........... =   " + f.format(sum.queue / completedJobs));
        System.out.println("   average service time .... =   " + f.format(sum.service / completedJobs));
        System.out.println("   average # in the node ... =   " + f.format(sum.node / s.getCurrent()));
        System.out.println("   average # in the queue .. =   " + f.format(sum.queue / s.getCurrent()));
        System.out.println("   utilization ............. =   " + f.format(sum.service / s.getCurrent()));

    }

}
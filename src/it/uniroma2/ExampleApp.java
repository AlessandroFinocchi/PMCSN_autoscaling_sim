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

/***
 * Riprendi da:
 * - LIBRO      p.182   paragrafo inizio
 * - APPUNTI    p.32    paragrafo inizio
 */


public class ExampleApp {

    public static void main(String[] args) {

        long index  = 0;                  /* used to count departed jobs         */
        long number = 0;                  /* number in the node                  */
        double sarrival = START;

        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        Distribution arrivalVA  = new Exponential(r, 0, ARRIVALS_MU);
        Distribution servicesVA = new Uniform(r, 1, SERVICES_MIN, SERVICES_MAX);

        sarrival += arrivalVA.gen();

        SystemStatsSum sum = new SystemStatsSum();
        SystemState s = new SystemState(
                sarrival,
                INFINITY,
                START
        );

        ProgressBar bar = new ProgressBar(STOP);

        while (s.getArrival() < STOP || number > 0) {
            bar.update(s.getCurrent());
            s.setNext(Math.min(s.getArrival(), s.getCompletion()));
            if (number > 0) {
                sum.node    += (s.getNext() - s.getCurrent()) * number;
                sum.queue   += (s.getNext() - s.getCurrent()) * (number - 1);
                sum.service += (s.getNext() - s.getCurrent());
            }
            s.setCurrent(s.getNext());

            if (s.getCurrent() == s.getArrival()) {
                number++;
                sarrival += arrivalVA.gen();
                s.setArrival(sarrival);
                if (s.getArrival() > STOP) {
                    s.setLast(s.getCurrent());
                    s.setArrival(INFINITY);
                }
                if (number == 1)
                    s.setCompletion(s.getCurrent() + servicesVA.gen());
            }
            else {
                index++;
                number--;
                s.setCompletion(number > 0 ? s.getCurrent() + servicesVA.gen() : INFINITY);
            }
        }
        DecimalFormat f = new DecimalFormat("###0.00");

        System.out.println("\nfor " + index + " jobs");
        System.out.println("   average interarrival time =   " + f.format(s.getLast() / index));
        System.out.println("   average wait ............ =   " + f.format(sum.node / index));
        System.out.println("   average delay ........... =   " + f.format(sum.queue / index));
        System.out.println("   average service time .... =   " + f.format(sum.service / index));
        System.out.println("   average # in the node ... =   " + f.format(sum.node / s.getCurrent()));
        System.out.println("   average # in the queue .. =   " + f.format(sum.queue / s.getCurrent()));
        System.out.println("   utilization ............. =   " + f.format(sum.service / s.getCurrent()));

    }

}
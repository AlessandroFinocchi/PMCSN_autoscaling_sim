package it.uniroma2;

import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Request;

import java.text.DecimalFormat;

import static it.uniroma2.models.Config.*;

/***
 * Riprendi da:
 * - LIBRO      p.117   paragrafo 3.2
 * - APPUNTI    p.28    paragrafo 3.1.3
 */

class ExampleSum {                                 /* sum of ...           */
    double delay;                                  /*   delay times        */
    double wait;                                   /*   wait times         */
    double service;                                /*   service times      */
    double interarrival;                           /*   interarrival times */

    ExampleSum() {
        this.delay = 0.0;
        this.wait = 0.0;
        this.service = 0.0;
        this.interarrival = 0.0;
    }
}

public class ExampleApp {

    public static void main( String[] args )
    {
        long   index     = 0;                         /* job index            */
        double arrival   = Request.getSTART();        /* time of arrival      */
        double delay;                                 /* delay in queue       */
        double wait;                                  /* delay + service      */
        double departure = Request.getSTART();        /* time of departure    */

        ExampleSum sum = new ExampleSum();
        Rngs r = new Rngs();
        r.plantSeeds(SEED);


        while (index < Request.getLAST()) {
            index++;
            Request request = new Request(r);
            if (arrival < departure)
                delay      = departure - arrival;         /* delay in queue    */
            else
                delay      = 0.0;                         /* no delay          */
            wait         = delay + request.getServiceTime();
            departure    = arrival + wait;              /* time of departure */
            sum.delay   += delay;
            sum.wait    += wait;
            sum.service += request.getServiceTime();
        }
        sum.interarrival = arrival - Request.getSTART();

        DecimalFormat f = new DecimalFormat("###0.00");
        System.out.println("\nfor " + index + " jobs");
        System.out.println("   average interarrival time =   " + f.format(sum.interarrival / index));
        System.out.println("   average wait ............ =   " + f.format(sum.wait / index));
        System.out.println("   average delay ........... =   " + f.format(sum.delay / index));
        System.out.println("   average service time .... =   " + f.format(sum.service / index));
        System.out.println("   average # in the node ... =   " + f.format(sum.wait / departure));
        System.out.println("   average # in the queue .. =   " + f.format(sum.delay / departure));
        System.out.println("   utilization ............. =   " + f.format(sum.service / departure));

    }

}

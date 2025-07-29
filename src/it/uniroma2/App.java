package it.uniroma2;

import it.uniroma2.libs.Rngs;
import it.uniroma2.models.Request;

import static it.uniroma2.models.Config.*;

public class App 
{
    public static void main( String[] args )
    {
        Rngs rngs = new Rngs();
        rngs.plantSeeds(SEED);

        System.out.println( "Hello World! Seed is number " + SEED );

        Request r = new Request(1.0f, 2.2f);
        System.out.println(r);

    }
}

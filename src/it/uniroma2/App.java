package it.uniroma2;

import libs.Rngs;

import static it.uniroma2.models.Config.*;

public class App 
{
    public static void main( String[] args )
    {
        Rngs rngs = new Rngs();
        rngs.plantSeeds(SEED);

        System.out.println( "Hello World! Seed is number " + SEED );

    }
}

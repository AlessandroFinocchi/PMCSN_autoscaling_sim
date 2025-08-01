package it.uniroma2.models;

import it.uniroma2.ExampleApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static final int SEED;

    public static final double ARRIVALS_MU;
    public static final double SERVICES_MIN;
    public static final double SERVICES_MAX;

    public static final double START;
    public static final double STOP;
    public static final double INFINITY;

    static {
        try(InputStream in = ExampleApp.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)){
            Properties props = new Properties();
            props.load(in);

            SEED            = Integer.parseInt(props.getProperty("random.seed"));
            ARRIVALS_MU = Double.parseDouble(props.getProperty("distribution.arrivals.mu"));
            SERVICES_MIN    = Double.parseDouble(props.getProperty("distribution.services.min"));
            SERVICES_MAX    = Double.parseDouble(props.getProperty("distribution.services.max"));
            START           = Double.parseDouble(props.getProperty("system.start"));
            STOP            = Double.parseDouble(props.getProperty("system.stop"));
            INFINITY        = Double.POSITIVE_INFINITY;
//            INFINITY = 100.0 * STOP;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

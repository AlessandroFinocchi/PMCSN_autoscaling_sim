package it.uniroma2.models;

import it.uniroma2.ExampleApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static final int SEED;

    public static final double ARRIVALS_MU;
    public static final double SERVICES_Z;
    public static final double WEBSERVER_CAPACITY;

    public static final double START;
    public static final double STOP;
    public static final double INFINITY;
    public static final double RESPONSE_TIME_OUT_THRESHOLD;
    public static final double RESPONSE_TIME_IN_THRESHOLD;

    static {
        try(InputStream in = ExampleApp.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)){
            Properties props = new Properties();
            props.load(in);

            SEED            = Integer.parseInt(props.getProperty("random.seed"));
            ARRIVALS_MU    = Double.parseDouble(props.getProperty("distribution.arrivals.mu"));
            SERVICES_Z    = Double.parseDouble(props.getProperty("distribution.services.z"));
            WEBSERVER_CAPACITY = Double.parseDouble(props.getProperty("webserver.capacity"));
            START           = Double.parseDouble(props.getProperty("system.start"));
            STOP            = Double.parseDouble(props.getProperty("system.stop"));
            INFINITY        = Double.POSITIVE_INFINITY;
            RESPONSE_TIME_OUT_THRESHOLD  = Double.parseDouble(props.getProperty("webserver.response_time.out_thr"));
            RESPONSE_TIME_IN_THRESHOLD  = Double.parseDouble(props.getProperty("webserver.response_time.in_thr"));

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

package it.uniroma2.models;

import it.uniroma2.ExampleApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static final int SEED;
    public static final double ARRIVALS_AVG;
    public static final double ARRIVALS_CV;
    public static final double SERVICES_AVG;
    public static final double SERVICES_CV;

    static {
        try(InputStream in = ExampleApp.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)){
            Properties props = new Properties();
            props.load(in);

            SEED = Integer.parseInt(props.getProperty("random.seed"));
            ARRIVALS_AVG = Double.parseDouble(props.getProperty("distribution.arrivals.avg"));
            ARRIVALS_CV = Double.parseDouble(props.getProperty("distribution.arrivals.cv"));
            SERVICES_AVG = Double.parseDouble(props.getProperty("distribution.services.avg"));
            SERVICES_CV = Double.parseDouble(props.getProperty("distribution.services.cv"));

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

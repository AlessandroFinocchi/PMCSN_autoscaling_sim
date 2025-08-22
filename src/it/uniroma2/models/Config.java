package it.uniroma2.models;

import it.uniroma2.SimulateRunApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static it.uniroma2.utils.DataCSVWriter.*;

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
    public static final int MAX_NUM_SERVERS;
    public static final int START_NUM_SERVERS;
    public static final double ALPHA;
    public static final int SI_MAX;
    public static final double SPIKE_CAPACITY;
    public static final boolean SPIKESERVER_ACTIVE;
    public static final String SCHEDULER_TYPE;



    static {
        try(InputStream in = SimulateRunApp.class
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
            RESPONSE_TIME_OUT_THRESHOLD  = (Objects.equals(props.getProperty("webserver.response_time.out_thr"), "INFINITY")) ?
                            Double.POSITIVE_INFINITY : Double.parseDouble(props.getProperty("webserver.response_time.out_thr"));
            RESPONSE_TIME_IN_THRESHOLD  = Double.parseDouble(props.getProperty("webserver.response_time.in_thr"));
            MAX_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.max_num_server"));
            START_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.start_num_server"));
            ALPHA = Double.parseDouble(props.getProperty("stats.alpha"));
            SI_MAX = Integer.parseInt(props.getProperty("infrastructure.si_max"));
            SPIKE_CAPACITY = Double.parseDouble(props.getProperty("spikeserver.capacity"));
            SPIKESERVER_ACTIVE = Boolean.parseBoolean(props.getProperty("infrastructure.spikeserver.active"));
            SCHEDULER_TYPE =  props.getProperty("infrastructure.scheduler");

            /* Add properties to CSV logging */
            List<String> sortedKeys = new ArrayList<>(props.stringPropertyNames());
            sortedKeys.sort(String::compareTo);
            for (String key: sortedKeys) {
                INTER_RUN_DATA_HEADERS.add(key);
                INTER_RUN_DATA.addField(RUN_FINISHED_KEY, key, props.getProperty(key));
            }

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

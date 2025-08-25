package it.uniroma2.models;

import it.uniroma2.SimulateRunApp;
import it.uniroma2.models.configurations.RunConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static it.uniroma2.utils.DataCSVWriter.*;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static int SEED;
    public static int TOTAL_STREAMS;

    public static double ARRIVALS_MU;
    public static double ARRIVALS_CV;
    public static double SERVICES_Z;
    public static double SERVICES_CV;
    public static double WEBSERVER_CAPACITY;

    public static double START;
    public static double STOP;
    public static double INFINITY;
    public static double RESPONSE_TIME_OUT_THRESHOLD;
    public static double RESPONSE_TIME_IN_THRESHOLD;
    public static int MAX_NUM_SERVERS;
    public static int START_NUM_SERVERS;
    public static double ALPHA;
    public static int SI_MAX;
    public static double SPIKE_CAPACITY;
    public static boolean SPIKESERVER_ACTIVE;
    public static String SCHEDULER_TYPE;
    public static double TURN_ON_MU;
    public static double TURN_ON_STD;

    static {
        loadHeaders();
        load(null);
    }

    public static void loadHeaders() {
        try (InputStream in = SimulateRunApp.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.load(in);

            /* Add properties to CSV logging */
            List<String> sortedKeys = new ArrayList<>(props.stringPropertyNames());
            sortedKeys.sort(String::compareTo);
            for (String key : sortedKeys) {
                INTER_RUN_DATA_HEADERS.add(key);
            }

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public static void load(RunConfiguration c) {
        try (InputStream in = SimulateRunApp.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            Properties props = new Properties();

            /* Load default properties from configuration file */
            props.load(in);

            /* Overwrite default properties */
            if (c != null) {
                for (Map.Entry<String, String> entry : c.getParams().entrySet()) {
                    props.setProperty(entry.getKey(), entry.getValue());
                }
            }

            SEED = Integer.parseInt(props.getProperty("random.seed"));
            TOTAL_STREAMS = Integer.parseInt(props.getProperty("random.total_streams"));
            ARRIVALS_MU = Double.parseDouble(props.getProperty("distribution.arrivals.mu"));
            ARRIVALS_CV = Double.parseDouble(props.getProperty("distribution.arrivals.cv"));
            SERVICES_Z = Double.parseDouble(props.getProperty("distribution.services.z"));
            SERVICES_CV = Double.parseDouble(props.getProperty("distribution.services.cv"));
            WEBSERVER_CAPACITY = Double.parseDouble(props.getProperty("webserver.capacity"));
            START = Double.parseDouble(props.getProperty("system.start"));
            STOP = Double.parseDouble(props.getProperty("system.stop"));
            INFINITY = Double.POSITIVE_INFINITY;
            RESPONSE_TIME_OUT_THRESHOLD = (Objects.equals(props.getProperty("webserver.response_time.out_thr"), "INFINITY")) ?
                    Double.POSITIVE_INFINITY : Double.parseDouble(props.getProperty("webserver.response_time.out_thr"));
            RESPONSE_TIME_IN_THRESHOLD = Double.parseDouble(props.getProperty("webserver.response_time.in_thr"));
            MAX_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.max_num_server"));
            START_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.start_num_server"));
            ALPHA = Double.parseDouble(props.getProperty("stats.alpha"));
            SI_MAX = Integer.parseInt(props.getProperty("infrastructure.si_max"));
            SPIKE_CAPACITY = Double.parseDouble(props.getProperty("spikeserver.capacity"));
            SPIKESERVER_ACTIVE = Boolean.parseBoolean(props.getProperty("infrastructure.spikeserver.active"));
            SCHEDULER_TYPE = props.getProperty("infrastructure.scheduler");
            TURN_ON_MU = Double.parseDouble(props.getProperty("turn.on.mu"));
            TURN_ON_STD = Double.parseDouble(props.getProperty("turn.on.std"));

            /* Add properties values for CSV logging */
            INTER_RUN_DATA.clear();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                INTER_RUN_DATA.addField(INTER_RUN_KEY, (String) entry.getKey(), entry.getValue());
            }

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

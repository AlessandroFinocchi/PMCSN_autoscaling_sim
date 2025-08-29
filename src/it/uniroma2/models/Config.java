package it.uniroma2.models;

import it.uniroma2.SimulateRunApp;
import it.uniroma2.models.configurations.RunConfiguration;
import it.uniroma2.models.configurations.experiments.Experiment;
import it.uniroma2.models.configurations.experiments.ExperimentBaseTransient;
import it.uniroma2.models.configurations.experiments.ExperimentSimpleBM;
import it.uniroma2.utils.DataCSVWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static it.uniroma2.utils.DataCSVWriter.*;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static boolean LOG_INTRA_RUN;
    public static boolean LOG_BM;

    public static int SEED;
    public static int TOTAL_STREAMS;
    public static int REPEAT_CONFIGURATION;

    public static double ARRIVALS_MU;
    public static double ARRIVALS_CV;
    public static double SERVICES_Z;
    public static double SERVICES_CV;
    public static double WEBSERVER_CAPACITY;

    public static double START;
    public static double STOP;
    public static boolean EMPTY_JOBS;
    public static double INFINITY;
    public static double RESPONSE_TIME_OUT_THRESHOLD;
    public static double RESPONSE_TIME_IN_THRESHOLD;
    public static double RESPONSE_TIME_SLO;
    public static int MAX_NUM_SERVERS;
    public static int START_NUM_SERVERS;
    public static int SI_MAX;
    public static double SPIKE_CAPACITY;
    public static boolean SPIKESERVER_ACTIVE;
    public static String SCHEDULER_TYPE;
    public static double TURN_ON_MU;
    public static double TURN_ON_STD;

    public static int ALL_MAX_NUM_SERVERS;
    public static boolean ALL_SPIKESERVER_ACTIVE;

    public static double ALPHA;
    public static int STATS_BATCH_SIZE;
    public static int STATS_BATCH_NUM;
    public static double STATS_CONFIDENCE_ALPHA;

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
                if (key.startsWith("log.")) continue; // skip log properties
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

            LOG_INTRA_RUN = Boolean.parseBoolean(props.getProperty("log.intra_run"));
            LOG_BM = Boolean.parseBoolean(props.getProperty("log.bm"));
            SEED = Integer.parseInt(props.getProperty("random.seed"));
            TOTAL_STREAMS = Integer.parseInt(props.getProperty("random.total_streams"));
            REPEAT_CONFIGURATION = Integer.parseInt(props.getProperty("random.repeat_config"));
            ARRIVALS_MU = Double.parseDouble(props.getProperty("distribution.arrivals.mu"));
            ARRIVALS_CV = Double.parseDouble(props.getProperty("distribution.arrivals.cv"));
            SERVICES_Z = Double.parseDouble(props.getProperty("distribution.services.z"));
            SERVICES_CV = Double.parseDouble(props.getProperty("distribution.services.cv"));
            WEBSERVER_CAPACITY = Double.parseDouble(props.getProperty("webserver.capacity"));
            START = Double.parseDouble(props.getProperty("system.start"));
            STOP = (Objects.equals(props.getProperty("system.stop"), "INFINITY")) ?
                    Double.POSITIVE_INFINITY : Double.parseDouble(props.getProperty("system.stop"));
            EMPTY_JOBS =  Boolean.parseBoolean(props.getProperty("system.empty_jobs"));
            INFINITY = Double.POSITIVE_INFINITY;
            RESPONSE_TIME_OUT_THRESHOLD = (Objects.equals(props.getProperty("webserver.response_time.out_thr"), "INFINITY")) ?
                    Double.POSITIVE_INFINITY : Double.parseDouble(props.getProperty("webserver.response_time.out_thr"));
            RESPONSE_TIME_IN_THRESHOLD = Double.parseDouble(props.getProperty("webserver.response_time.in_thr"));
            RESPONSE_TIME_SLO = Double.parseDouble(props.getProperty("infrastructure.response_time_slo"));
            MAX_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.max_num_server"));
            START_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.start_num_server"));
            SI_MAX = Integer.parseInt(props.getProperty("infrastructure.si_max"));
            SPIKE_CAPACITY = Double.parseDouble(props.getProperty("spikeserver.capacity"));
            SPIKESERVER_ACTIVE = Boolean.parseBoolean(props.getProperty("infrastructure.spikeserver.active"));
            SCHEDULER_TYPE = props.getProperty("infrastructure.scheduler");
            TURN_ON_MU = Double.parseDouble(props.getProperty("turn.on.mu"));
            TURN_ON_STD = Double.parseDouble(props.getProperty("turn.on.std"));

            ALPHA = Double.parseDouble(props.getProperty("stats.alpha"));
            STATS_BATCH_SIZE = (Objects.equals(props.getProperty("stats.batch.size"), "INFINITY")) ?
                    Integer.MAX_VALUE : Integer.parseInt(props.getProperty("stats.batch.size"));
            STATS_BATCH_NUM = Integer.parseInt(props.getProperty("stats.batch.num"));
            STATS_CONFIDENCE_ALPHA = Double.parseDouble(props.getProperty("stats.confidence.alpha"));

            /* Add property values for CSV logging */
            INTER_RUN_DATA.clear();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                INTER_RUN_DATA.addField(INTER_RUN_KEY, (String) entry.getKey(), entry.getValue());
            }

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public static List<RunConfiguration> createConfigurations() {
        List<RunConfiguration> configurations = new ArrayList<>();

        Experiment experiment = new ExperimentSimpleBM();
        configurations.addAll(experiment.getRunConfigurations());

        // Set variables used to CSV log
        ALL_MAX_NUM_SERVERS = MAX_NUM_SERVERS;
        ALL_SPIKESERVER_ACTIVE = SPIKESERVER_ACTIVE;
        for (RunConfiguration c : configurations) {
            if (c.get("infrastructure.start_num_server") != null) {
                ALL_MAX_NUM_SERVERS = Math.max(ALL_MAX_NUM_SERVERS, Integer.parseInt(c.get("infrastructure.start_num_server")));
            }
            if (c.get("infrastructure.spikeserver.capacity") != null) {
                ALL_SPIKESERVER_ACTIVE = ALL_SPIKESERVER_ACTIVE || Boolean.parseBoolean(c.get("infrastructure.spikeserver.active"));
            }
        }

        System.out.printf("Created %d configurations\n\n", configurations.size());
        for (RunConfiguration c : configurations) {
            System.out.println(c.toString());
            System.out.println();
        }

        return configurations;
    }

}

package it.uniroma2.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import it.uniroma2.models.configurations.RunConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataField.*;

public class DataCSVWriter {

    private static final LocalDateTime START_DATE_TIME = LocalDateTime.now();

    // Data table for completion and scaling events
    public static final DataTimeTable INTRA_RUN_DATA = new DataTimeTable();
    public static final DataTimeTable INTRA_RUN_BM_DATA = new DataTimeTable();

    public static final DataHeaders INTER_RUN_DATA_HEADERS = new DataHeaders();
    public static final DataTimeTable INTER_RUN_DATA = new DataTimeTable();
    public static final Double INTER_RUN_KEY = -1.0;

    private static final String OUT_DIR_PATH = "out_data";
    private static final String OUT_DIR_PATH_WITH_SUFFIX = OUT_DIR_PATH + "/" + OUT_DIR_PATH_SUFFIX();

    private static String OUT_DIR_PATH_SUFFIX() {
        return START_DATE_TIME.getYear() + "-" + START_DATE_TIME.getMonthValue() + "-" + START_DATE_TIME.getDayOfMonth() + "_" + START_DATE_TIME.getHour() + "-" + START_DATE_TIME.getMinute() + "-" + START_DATE_TIME.getSecond();
    }

    private static boolean hasHeaders(String filePath, String[] fields) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers = reader.readNext();
            return headers != null && Arrays.equals(headers, fields);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Flushes a DataTimeTable to a file in .csv format.
     *
     * @param timeTable The DataTimeTable containing the data.
     * @param fileName  The name of the file in the OUT_DIR_PATH.
     * @param fields    The lists of the fields that will be flushed on file.
     */
    public static void flushList(DataTimeTable timeTable, String outPath, String fileName, String[] fields, boolean append) {
        String filePath = outPath + "/" + fileName + ".csv";

        // Create the directory if not exists
        File directory = new File(OUT_DIR_PATH_WITH_SUFFIX);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create output directory");
        }

        // Check if the file is writable
        File file = new File(filePath);
        if (file.exists() && !file.canWrite()) {
            throw new RuntimeException("File exists but is not writable: " + filePath);
        }

        // Write on file
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, append), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            // Write headers only if not already written
            if (!append || !hasHeaders(filePath, fields)) {
                writer.writeNext(fields);
            }

            // Write data
            for (String[] row : timeTable.getDataFromHeaders(fields)) {
                writer.writeNext(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void flushAllIntra(RunConfiguration c, int repetition) {
        String fileNameSuffix = "-" + c.getName() + "_" + repetition;

        /* Log data about scaling events */
        if (repetition == 0 && SCALING_OUT_THRESHOLD != INFINITY) {
            DataHeaders scalingHeaders = new DataHeaders(
                    TIMESTAMP,
                    R_0, SCALING_INDICATOR, COMPLETING_SERVER_INDEX,
                    EVENT_TYPE_SCALING,
                    TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED
            );
            scalingHeaders.add(
                    JOBS_IN_SERVER + "_1", JOBS_IN_SERVER + "_2", JOBS_IN_SERVER + "_3"
            );
            DataTimeTable filteredScalingData = INTRA_RUN_DATA
                    .filter(EVENT_TYPE_SCALING, false, "");
            flushList(filteredScalingData,
                    OUT_DIR_PATH_WITH_SUFFIX, "scaling" + fileNameSuffix,
                    scalingHeaders.get(), false);
        }

        /* Log data about jobs in each server */
        if (repetition == 0) {
            DataHeaders jobsHeaders = new DataHeaders();
            jobsHeaders.add(
                    TIMESTAMP,
                    EVENT_TYPE_JOB,
                    PER_JOB_RESPONSE_TIME,
                    COMPLETING_SERVER_INDEX, JOBS_IN_SYSTEM,
                    EVENT_TYPE_SCALING, SCALING_INDICATOR,
                    JOB_SIZE, NEXT_INTERARRIVAL_TIME
            );
            jobsHeaders.add(TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED);
            if (SPIKESERVER_ACTIVE) {
                jobsHeaders.add(SPIKE_CURRENT_CAPACITY);
                jobsHeaders.add("JOBS_IN_SERVER_0");
            }
            for (int i = 1; i <= MAX_NUM_SERVERS; i++) {
                jobsHeaders.add("STATUS_OF_SERVER_" + i);
                jobsHeaders.add("JOBS_IN_SERVER_" + i);
            }
            jobsHeaders.add(AGG_SYSTEM_RESPONSE_TIME, AGG_SYSTEM_JOB_NUMBER, AGG_SYSTEM_UTILIZATION, AGG_SYSTEM_ALLOCATED_CAPACITY_PER_SEC);
            for (int serverIndex = (SPIKESERVER_ACTIVE) ? 0 : 1; serverIndex <= MAX_NUM_SERVERS; serverIndex++) {
                jobsHeaders.add(
                        AGG_SERVER_RESPONSE_TIME + "_" + serverIndex,
                        AGG_SERVER_JOB_NUMBER + "_" + serverIndex,
                        AGG_SERVER_UTILIZATION + "_" + serverIndex,
                        AGG_SERVER_ALLOCATED_CAPACITY_PER_SEC + "_" + serverIndex
                );
            }
            // DataTimeTable filteredJobsData = INTRA_RUN_DATA.filter(EVENT_TYPE_SCALING, false, "ACTIVE");
            DataTimeTable filteredJobsData = INTRA_RUN_DATA;
            flushList(filteredJobsData,
                    OUT_DIR_PATH_WITH_SUFFIX, "jobs" + fileNameSuffix,
                    jobsHeaders.get(), false);
        }

        /* Log data about jobs in each server */
        DataHeaders allJobsHeaders = new DataHeaders();
        allJobsHeaders.add(
                TIMESTAMP,
                AGG_SYSTEM_RESPONSE_TIME,
                AGG_SYSTEM_JOB_NUMBER,
                AGG_SYSTEM_UTILIZATION,
                AGG_SYSTEM_ALLOCATED_CAPACITY_PER_SEC
        );
        for (int i = SPIKESERVER_ACTIVE ? 0 : 1; i <= MAX_NUM_SERVERS; i++) {
            allJobsHeaders.add(
                    AGG_SERVER_RESPONSE_TIME + "_" + i,
                    AGG_SERVER_JOB_NUMBER + "_" + i,
                    AGG_SERVER_UTILIZATION + "_" + i,
                    AGG_SERVER_ALLOCATED_CAPACITY_PER_SEC + "_" + i
            );
        }
        allJobsHeaders.add(REPETITION_ID);
        DataTimeTable combinedJobsData = INTRA_RUN_DATA
                .filter(EVENT_TYPE_SCALING, false, "ACTIVE")
                .setEach(REPETITION_ID, repetition);
        flushList(combinedJobsData,
                OUT_DIR_PATH_WITH_SUFFIX, "jobs" + "-" + c.getName() + "_all",
                allJobsHeaders.get(), true);

        INTRA_RUN_DATA.clear();
    }

    public static void flushAllIntraBM(RunConfiguration c, int repetition) {
        String fileNameSuffix = "-" + c.getName() + "_" + repetition;

        /* Log data about jobs in each server */
        DataHeaders bmHeaders = new DataHeaders();
        bmHeaders.add(TIMESTAMP, EVENT_TYPE_JOB, EVENT_TYPE_SCALING, COMPLETING_SERVER_INDEX, PER_JOB_RESPONSE_TIME);
        bmHeaders.add(
                BM_SYSTEM_RESPONSE_TIME,
                BM_SYSTEM_JOB_NUMBER,
                BM_SYSTEM_UTILIZATION,
                BM_SYSTEM_ALLOCATED_CAPACITY_PER_SEC,
                BM_SYSTEM_95PERC_SLO_VIOLATIONS_PERC,
                BM_SYSTEM_99PERC_SLO_VIOLATIONS_PERC
        );
        for (int i = 1; i <= MAX_NUM_SERVERS; i++) {
            bmHeaders.add(
                    BM_SERVER_RESPONSE_TIME + "_" + i,
                    BM_SERVER_JOB_NUMBER + "_" + i,
                    BM_SERVER_UTILIZATION + "_" + i,
                    BM_SERVER_ALLOCATED_CAPACITY_PER_SEC + "_" + i,
                    BM_SERVER_95PERC_SLO_VIOLATIONS_PERC + "_" + i,
                    BM_SERVER_99PERC_SLO_VIOLATIONS_PERC + "_" + i
            );
        }
        DataTimeTable fitleredBmData = INTRA_RUN_BM_DATA;
        flushList(fitleredBmData,
                OUT_DIR_PATH_WITH_SUFFIX, "bm" + fileNameSuffix,
                bmHeaders.get(), false);
    }

    public static void flushAllInter() {
        /* Log data about configuration and final results of a run */
        for (int stream = 0; stream < TOTAL_STREAMS; stream++) {
            INTER_RUN_DATA_HEADERS.add(STREAM_SEED + "_" + stream);
        }
        INTER_RUN_DATA_HEADERS.add(
                RUN_DATETIME, CONFIGURATION_DESCRIPTION, CONFIGURATION_ID, REPETITION_ID,
                FINAL_TS,
                TOTAL_ALLOCATED_CAPACITY, WEB_SERVER_ALLOCATED_CAPACITY, SPIKE_VIRTUAL_ALLOCATED_CAPACITY,
                ALLOCATED_CAPACITY_PER_SEC,
                SYSTEM_UTILIZATION,
                MEAN_SYSTEM_RESPONSE_TIME, TOTAL_JOBS_COMPLETED, TOTAL_SPIKE_JOBS_COMPLETED,
                TOTAL_SLO_95_VIOLATIONS, TOTAL_SLO_99_VIOLATIONS,
                SPIKE_SLO_95_VIOLATIONS, SPIKE_SLO_99_VIOLATIONS,
                SLO_95PERC_VIOLATIONS_PERCENTAGE, SLO_99PERC_VIOLATIONS_PERCENTAGE
        );
        flushList(INTER_RUN_DATA,
                OUT_DIR_PATH, "final_all",
                INTER_RUN_DATA_HEADERS.get(), true);
        flushList(INTER_RUN_DATA,
                OUT_DIR_PATH_WITH_SUFFIX, "final",
                INTER_RUN_DATA_HEADERS.get(), true);
    }
}

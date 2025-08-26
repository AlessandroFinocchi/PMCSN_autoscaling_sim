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

    public static final DataHeaders INTER_RUN_DATA_HEADERS = new DataHeaders();
    public static final DataTimeTable INTER_RUN_DATA = new DataTimeTable();
    public static final Double INTER_RUN_KEY = -1.0;

    private static final String OUT_DIR_PATH = "out_data/" + OUT_DIR_PATH_SUFFIX();

    private static String OUT_DIR_PATH_SUFFIX() {
        // return "";
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
    public static void flushList(DataTimeTable timeTable, String fileName, String[] fields, boolean append) {
        String filePath = OUT_DIR_PATH + "/" + fileName + ".csv";

        // Create the directory if not exists
        File directory = new File(OUT_DIR_PATH);
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

    public static void flushList(DataTimeTable timeTable, String fileName, DataField[] fields, boolean append) throws IOException {
        String[] strFields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            strFields[i] = fields[i].toString();
        }
        flushList(timeTable, fileName, strFields, append);
    }

    public static void flushAllIntra(RunConfiguration c, int repetition) {
        String fileNameSuffix = "-" + c.getName() + "_" + repetition;

        /* Log data about scaling events */
        DataHeaders scalingHeaders = new DataHeaders(TIMESTAMP, R_0, MOVING_R_O, EVENT_TYPE, TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED);
        DataTimeTable filteredScalingData = INTRA_RUN_DATA.filter(EVENT_TYPE, false, "ARRIVAL").filter(EVENT_TYPE, false, "COMPLETION");
        flushList(filteredScalingData, "scaling" + fileNameSuffix, scalingHeaders.get(), false);

        /* Log data about jobs in each server */
        DataHeaders jobsHeaders = new DataHeaders();
        jobsHeaders.add(TIMESTAMP, EVENT_TYPE, COMPLETING_SERVER_INDEX, PER_JOB_RESPONSE_TIME);
        if (SPIKESERVER_ACTIVE) {
            jobsHeaders.add(SPIKE_CURRENT_CAPACITY);
            jobsHeaders.add("JOBS_IN_SERVER_0");
        }
        for (int i = 1; i <= MAX_NUM_SERVERS; i++) {
            jobsHeaders.add("STATUS_OF_SERVER_" + i);
            jobsHeaders.add("JOBS_IN_SERVER_" + i);
        }
        DataTimeTable filteredJobsData = INTRA_RUN_DATA.filter(EVENT_TYPE, false, "ACTIVE");
        flushList(filteredJobsData, "jobs" + fileNameSuffix, jobsHeaders.get(), false);

        /* Log data about jobs in each server */
        DataHeaders allJobsHeaders = new DataHeaders();
        allJobsHeaders.add(TIMESTAMP, EVENT_TYPE, COMPLETING_SERVER_INDEX, PER_JOB_RESPONSE_TIME);
        if (ALL_SPIKESERVER_ACTIVE) {
            allJobsHeaders.add(SPIKE_CURRENT_CAPACITY);
            allJobsHeaders.add("JOBS_IN_SERVER_0");
        }
        for (int i = 1; i <= ALL_MAX_NUM_SERVERS; i++) {
            allJobsHeaders.add("STATUS_OF_SERVER_" + i);
            allJobsHeaders.add("JOBS_IN_SERVER_" + i);
        }
        allJobsHeaders.add(CONFIGURATION_ID, REPETITION_ID);
        DataTimeTable combinedJobsData = filteredJobsData
                .setEach(CONFIGURATION_ID, c.getName())
                .setEach(REPETITION_ID, repetition);
        flushList(combinedJobsData, "jobs" + "-all", allJobsHeaders.get(), true);

        INTRA_RUN_DATA.clear();
    }

    public static void flushAllInter() {
        /* Log data about configuration and final results of a run */
        for (int stream = 0; stream < TOTAL_STREAMS; stream++) {
            INTER_RUN_DATA_HEADERS.add(STREAM_SEED + "_" + stream);
        }
        INTER_RUN_DATA_HEADERS.add(RUN_DATETIME, CONFIGURATION_ID, REPETITION_ID, FINAL_TS, TOTAL_ALLOCATED_CAPACITY, TOTAL_ALLOCATED_CAPACITY_PER_SEC, SYSTEM_UTILIZATION, MEAN_SYSTEM_RESPONSE_TIME, TOTAL_JOBS_COMPLETED, TOTAL_SPIKE_JOBS_COMPLETED, TOTAL_SLO_VIOLATIONS, SLO_VIOLATIONS_PERCENTAGE);
        flushList(INTER_RUN_DATA, "final", INTER_RUN_DATA_HEADERS.get(), true);
    }
}

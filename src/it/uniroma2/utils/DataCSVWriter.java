package it.uniroma2.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static it.uniroma2.utils.DataField.*;

public class DataCSVWriter {

    // Data table for completion and scaling events
    public static final DataTimeTable INTRA_RUN_DATA = new DataTimeTable();

    public static final DataHeaders INTER_RUN_DATA_HEADERS = new DataHeaders();
    public static final DataTimeTable INTER_RUN_DATA = new DataTimeTable();

    public static final Double INTER_RUN_KEY = -1.0;

    private static final String OUT_DIR_PATH = "out_data";

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
     * @param timeTable The DataTimeTable containing the data.
     * @param fileName The name of the file in the OUT_DIR_PATH.
     * @param fields The lists of the fields that will be flushed on file.
     * @throws IOException
     */
    public static void flushList(DataTimeTable timeTable, String fileName, String[] fields, boolean append) throws IOException {
        String filePath = OUT_DIR_PATH + "/" + fileName + ".csv";

        // Create the directory if not exists
        File directory = new File(OUT_DIR_PATH);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create output directory");
        }

        // Check if the file is writable
        File file = new File(filePath);
        if (file.exists() && !file.canWrite()) {
            throw new IOException("File exists but is not writable: " + filePath);
        }

        // Write on file
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, append),
                                              CSVWriter.DEFAULT_SEPARATOR,
                                              CSVWriter.NO_QUOTE_CHARACTER,
                                              CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                              CSVWriter.DEFAULT_LINE_END)) {

            // Write headers only if not already written
            if (!append || !hasHeaders(filePath, fields)) {
                writer.writeNext(fields);
            }

            // Write data
            for (String[] row : timeTable.getDataFromHeaders(fields)) {
                writer.writeNext(row);
            }
        }
    }

    public static void flushList(DataTimeTable timeTable, String fileName, DataField[] fields, boolean append) throws IOException {
        String[] strFields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            strFields[i] = fields[i].toString();
        }
        flushList(timeTable, fileName, strFields, append);
    }

    public static void flushAll() throws IOException {
        /* Log data about scaling events */
        DataHeaders scalingHeaders = new DataHeaders(TIMESTAMP, R_0, MOVING_R_O, EVENT_TYPE, TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED);
        DataTimeTable filteredScalingData = INTRA_RUN_DATA
                .filter(EVENT_TYPE, false, "ARRIVAL")
                .filter(EVENT_TYPE, false, "COMPLETION");
        flushList(filteredScalingData, "scaling", scalingHeaders.get(), false);

        /* Log data about jobs in each server */
        DataHeaders jobsHeaders = new DataHeaders();
        jobsHeaders.add(TIMESTAMP, EVENT_TYPE, SPIKE_CURRENT_CAPACITY);
        jobsHeaders.add("JOBS_IN_SERVER_0");
        for (int i = 1; i <= 5; i++) {
            jobsHeaders.add("STATUS_OF_SERVER_" + i);
            jobsHeaders.add("JOBS_IN_SERVER_" + i);
        }
        DataTimeTable filteredJobsData = INTRA_RUN_DATA.filter(EVENT_TYPE, false, "ACTIVE");
        flushList(filteredJobsData, "jobs", jobsHeaders.get(), false);

        /* Log data about configuration and final results of a run */
        INTER_RUN_DATA_HEADERS.add(CONFIGURATION_ID, RUN_ID, RUN_SEED, FINAL_TS, TOTAL_ALLOCATED_CAPACITY, TOTAL_ALLOCATED_CAPACITY_PER_SEC, MEAN_SYSTEM_RESPONSE_TIME);
        flushList(INTER_RUN_DATA, "final", INTER_RUN_DATA_HEADERS.get(), true);
    }
}

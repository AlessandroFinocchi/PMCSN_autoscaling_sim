package it.uniroma2.utils;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static it.uniroma2.utils.DataField.*;

public class DataCSVWriter {

    // Data table for completion and scaling events
    public static DataTimeTable CSV_DATA = new DataTimeTable();

    private static String OUT_DIR_PATH = "out";

    /**
     * Flushes a DataTimeTable to a file in .csv format.
     * @param timeTable The DataTimeTable containing the data.
     * @param fileName The name of the file in the OUT_DIR_PATH.
     * @param fields The lists of the fields that will be flushed on file.
     * @throws IOException
     */
    public static void flushList(DataTimeTable timeTable, String fileName, String[] fields) throws IOException {
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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                                              CSVWriter.DEFAULT_SEPARATOR,
                                              CSVWriter.NO_QUOTE_CHARACTER,
                                              CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                              CSVWriter.DEFAULT_LINE_END)) {

            // Write headers
            writer.writeNext(fields);

            // Write data
            for (String[] row : timeTable.getDataFromHeaders(fields)) {
                writer.writeNext(row);
            }
        }
    }

    public static void flushList(DataTimeTable timeTable, String fileName, DataField[] fields) throws IOException {
        String[] strFields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            strFields[i] = fields[i].toString();
        }
        flushList(timeTable, fileName, strFields);
    }

    public static void flushAll() throws IOException {
        DataHeaders scalingHeaders = new DataHeaders(TIMESTAMP, R_0, MOVING_R_O, EVENT_TYPE, TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED);
        DataTimeTable filteredScalingData = CSV_DATA.filter(EVENT_TYPE, false, "ARRIVAL");
        flushList(filteredScalingData, "scaling", scalingHeaders.get());

        DataHeaders jobsHeaders = new DataHeaders();
        jobsHeaders.add(TIMESTAMP, EVENT_TYPE, SPIKE_CURRENT_CAPACITY);
        jobsHeaders.add("JOBS_IN_SERVER_0");
        for (int i = 1; i <= 5; i++) {
            jobsHeaders.add("STATUS_OF_SERVER_" + i);
            jobsHeaders.add("JOBS_IN_SERVER_" + i);
        }
        flushList(CSV_DATA, "jobs", jobsHeaders.get());
    }
}

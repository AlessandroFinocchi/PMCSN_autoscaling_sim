package it.uniroma2.utils;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static it.uniroma2.utils.DataField.*;

public class DataCSVWriter {

    // Data table for completion and scaling events
    public static DataTimeTable SCALING_DATA = new DataTimeTable();
    public static DataTimeTable JOBS_DATA = new DataTimeTable();

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
            String[] strFields = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                strFields[i] = fields[i];
            }
            writer.writeNext(strFields);

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
        flushList(SCALING_DATA, "scaling", new DataField[]{
                TIMESTAMP, R_0, MOVING_R_O, EVENT_TYPE, TO_BE_ACTIVE, ACTIVE, TO_BE_REMOVED, REMOVED
        });

        String[] jobsFields = new String[2 + 5 * 2];
        jobsFields[0] = "TIMESTAMP";
        jobsFields[1] = "EVENT_TYPE";
        for (int i = 0; i < 5; i++) {
            jobsFields[2 + i * 2] = "STATUS_OF_SERVER_" + (i + 1);
            jobsFields[2 + i * 2 + 1] = "JOBS_IN_SERVER_" + (i + 1);
        }
        flushList(JOBS_DATA, "jobs", jobsFields);
    }
}

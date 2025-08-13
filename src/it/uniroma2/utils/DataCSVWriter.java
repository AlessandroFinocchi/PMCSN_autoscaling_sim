package it.uniroma2.utils;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataCSVWriter {

    public static DataCSVList MOVING_R0 = new DataCSVList("moving-R_0", "timestamp", "moving-R_0");
    public static DataCSVList R0 = new DataCSVList("R_0", "timestamp", "R_0");
    public static DataCSVList SERVERS = new DataCSVList("servers", "timestamp", "active", "to-be-removed", "removed", "event");

    public static void flushList(DataCSVList list) throws IOException {
        String dirPath = "out";
        String filePath = dirPath + "/" + list.getName() + ".csv";

        File directory = new File(dirPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create output directory");
        }

        File file = new File(filePath);
        if (file.exists() && !file.canWrite()) {
            throw new IOException("File exists but is not writable: " + filePath);
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                                              CSVWriter.DEFAULT_SEPARATOR,
                                              CSVWriter.NO_QUOTE_CHARACTER,
                                              CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                              CSVWriter.DEFAULT_LINE_END)) {
            writer.writeNext(list.getHeader());

            if (list.getData() != null) {
                for (String[] row : list.getData()) {
                    writer.writeNext(row);
                }
            }
        }
    }

    public static void flushAll() throws IOException {
        flushList(MOVING_R0);
        flushList(R0);
        flushList(SERVERS);
    }
}

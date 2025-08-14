package it.uniroma2.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DataTimeTable {
    @Getter
    Map<Double, Map<DataField, String>> table;


    /**
     * Create a DataTimeTable where the row key is a double indicating a timestamp. The records are ordered by the row key.
     * The column key is a DataField object, so the possible fields are already enumerated.
     * The values are represented as String.
     */
    public DataTimeTable() {
        // TreeMap allows keeping the map ordered by the row key
        this.table = new TreeMap<>();
    }

    /**
     * Add a field given the row key, column key and its value.
     */
    public void addField(Double rowKey, DataField columnKey, Object value) {
        if (!table.containsKey(rowKey)) {
            table.put(rowKey, new HashMap<>());
            table.get(rowKey).put(DataField.TIMESTAMP, String.valueOf(rowKey));
        }
        table.get(rowKey).put(columnKey, String.valueOf(value));
    }

    /**
     * Given a list of DataField produce a table of String used for the CSV export.
     */
    public String[][] getDataFromHeaders(DataField... fields) {
        String[][] data = new String[table.size()][fields.length];
        int i = 0;
        for (Map.Entry<Double, Map<DataField, String>> entry : table.entrySet()) {
            int j = 0;
            for (DataField field : fields) {
                data[i][j] = entry.getValue().get(field);
                j++;
            }
            i++;
        }
        return data;
    }
}

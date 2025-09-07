package it.uniroma2.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static it.uniroma2.utils.DataField.TIMESTAMP;

public class DataTimeTable {
    @Getter
    Map<Double, Map<String, String>> table;
    @Getter
    @Setter
    private boolean writable = true;


    /**
     * Create a DataTimeTable where the row key is a double indicating a timestamp. The records are ordered by the row key.
     * The column key is a DataField object, so the possible fields are already enumerated.
     * The values are represented as String.
     */
    public DataTimeTable() {
        // TreeMap allows keeping the map ordered by the row key
        this.table = new TreeMap<>();
    }

    private DataTimeTable(Map<Double, Map<String, String>> table) {
        this.table = table;
    }

    /**
     * Add a field given the row key, column key and its value.
     */
    public void addField(Double rowKey, String columnKey, Object value) {
        if (!writable) return;

        if (!table.containsKey(rowKey)) {
            table.put(rowKey, new HashMap<>());
            table.get(rowKey).put(TIMESTAMP.toString(), String.valueOf(rowKey));
        }
        table.get(rowKey).put(columnKey, String.valueOf(value));
    }

    public void addField(Double rowKey, DataField columnKey, Object value) {
        this.addField(rowKey, columnKey.toString(), value);
    }

    public void addFieldWithSuffix(Double rowKey, DataField columnKey, String suffix, Object value) {
        if (!writable) return;

        if (!table.containsKey(rowKey)) {
            table.put(rowKey, new HashMap<>());
            table.get(rowKey).put(TIMESTAMP.toString(), String.valueOf(rowKey));
        }
        String columnKeyString = columnKey.toString() + "_" + suffix;
        table.get(rowKey).put(columnKeyString, String.valueOf(value));
    }

    public void deleteField(Double rowKey, DataField columnKey) {
        if (table.containsKey(rowKey)) {
            table.get(rowKey).remove(columnKey.toString());
        }
    }

    /**
     * Given a list of DataField produce a table of String used for the CSV export.
     */
    public String[][] getDataFromHeaders(String... fields) {
        String[][] data = new String[table.size()][fields.length];
        int i = 0;
        for (Map.Entry<Double, Map<String, String>> entry : table.entrySet()) {
            int j = 0;
            for (String field : fields) {
                data[i][j] = entry.getValue().get(field);
                j++;
            }
            i++;
        }
        return data;
    }

    /**
     * Return a new DataTimeTable containing only the entries that respect the filter.
     *
     * @param field  The field on which to apply the filter.
     * @param equals If equals is true, the value of the field has to be equals to the target.
     * @param target The target value.
     * @return A new filtered DataTimeTable.
     */
    public DataTimeTable filter(String field, boolean equals, String target) {
        Map<Double, Map<String, String>> newTable = new TreeMap<>();

        for (Map.Entry<Double, Map<String, String>> entry : this.table.entrySet()) {
            String value = entry.getValue().get(field);

            boolean acceptCondition = (Objects.equals(value, target)) == equals;
            if (acceptCondition) {
                newTable.put(entry.getKey(), entry.getValue());
            }
        }

        return new DataTimeTable(newTable);
    }

    public DataTimeTable filterPeriod(double period) {
        Map<Double, Map<String, String>> newTable = new TreeMap<>();

        double nextKey = 0.0;
        for (Map.Entry<Double, Map<String, String>> entry : this.table.entrySet()) {
            double value = Double.parseDouble(entry.getValue().get(TIMESTAMP.toString()));
            boolean acceptCondition = false;

            if (value > nextKey) {
                acceptCondition = true;
                nextKey = Math.floor(value / period) * period + period;
            }

            if (acceptCondition) {
                newTable.put(entry.getKey(), entry.getValue());
            }
        }

        return new DataTimeTable(newTable);
    }

    public DataTimeTable filter(DataField field, boolean equals, String target) {
        return this.filter(field.toString(), equals, target);
    }

    public void clear() {
        this.table.clear();
    }

    public DataTimeTable setEach(DataField field, Object value) {
        return this.setEach(field.toString(), String.valueOf(value));
    }

    public DataTimeTable setEach(String field, Object value) {
        Map<Double, Map<String, String>> newTable = new TreeMap<>();

        for (Map.Entry<Double, Map<String, String>> entry : this.table.entrySet()) {
            newTable.put(entry.getKey(), entry.getValue());
            newTable.get(entry.getKey()).put(field, String.valueOf(value));
        }

        return new DataTimeTable(newTable);
    }
}

package it.uniroma2.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DataCSVList {
    @Getter
    String name;
    @Getter
    String[] header;
    @Getter
    List<String[]> data;

    public DataCSVList(String name, String... fields) {
        this.name = name;
        this.header = fields;
        this.data = new ArrayList<>();
    }

    public void addArrayData(String[] values) {
        if (values.length != header.length) {
            throw new IllegalArgumentException("Wrong number of columns");
        }
        data.add(values);
    }

    public void addData(Object... values) {
        if (values.length != header.length) {
            throw new IllegalArgumentException("Wrong number of columns");
        }

        String[] stringData = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            stringData[i] = String.valueOf(values[i]);
        }

        data.add(stringData);
    }

}

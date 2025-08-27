package it.uniroma2.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DataHeaders {

    @Getter
    List<String> headers;

    public DataHeaders() {
        this.headers = new ArrayList<>();
    }

    public DataHeaders(List<String> headers) {
        this.headers = new ArrayList<>();
        this.headers.addAll(headers);
    }

    public DataHeaders(DataField... headers) {
        this.headers = new ArrayList<>();
        this.add(headers);
    }

    public void add(DataField... newHeaders) {
        for (DataField field : newHeaders) {
            this.add(field.toString());
        }
    }

    public void add(String... newHeaders) {
        for (String field : newHeaders) {
            if (!this.headers.contains(field)) {
                this.headers.add(field);
            }
        }
    }

    public String[] get() {
        return headers.toArray(new String[0]);
    }

    public void clear() {
        this.headers.clear();
    }

}

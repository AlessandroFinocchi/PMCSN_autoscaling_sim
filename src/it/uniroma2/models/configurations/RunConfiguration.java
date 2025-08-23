package it.uniroma2.models.configurations;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RunConfiguration {
    @Getter
    String name;
    @Getter
    Map<String, String> params;

    public RunConfiguration(String name) {
        this.name = name;
        this.params = new HashMap<>();
    }

    public void put(String key, String value) {
        this.params.put(key, value);
    }

    public String get(String key) {
        return this.params.get(key);
    }
}

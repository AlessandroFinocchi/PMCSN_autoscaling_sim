package it.uniroma2.models.configurations;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parameter {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    List<String> values;

    public Parameter(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public void addValues(String... newValues) {
        Collections.addAll(this.values, newValues);
    }
}

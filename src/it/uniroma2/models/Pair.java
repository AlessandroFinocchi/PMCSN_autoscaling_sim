package it.uniroma2.models;

import lombok.Getter;

public class Pair<T1, T2> {
    @Getter private T1 first;
    @Getter private T2 second;
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

}

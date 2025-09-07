package it.uniroma2.models;

import lombok.Getter;

public class Tuple<T1, T2, T3> {
    @Getter private T1 first;
    @Getter private T2 second;
    @Getter private T3 third;
    public Tuple(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}

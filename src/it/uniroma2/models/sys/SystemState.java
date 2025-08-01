package it.uniroma2.models.sys;

import lombok.Getter;
import lombok.Setter;

/**
 * System clock abstraction
 */
public class SystemState {
    @Getter @Setter private double arrival;                 /* next arrival time                   */
    @Getter @Setter private double completion;              /* next completion time                */
    @Getter @Setter private double current;                 /* current time                        */
    @Getter @Setter private double next;                    /* next (most imminent) event time     */
    @Getter @Setter private double last;                    /* last arrival time                   */

    public SystemState(double arrival, double completion, double current) {
        this.arrival = arrival;
        this.completion = completion;
        this.current = current;
    }
}
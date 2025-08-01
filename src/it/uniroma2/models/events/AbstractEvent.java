package it.uniroma2.models.events;

import it.uniroma2.models.distr.Distribution;

public class AbstractEvent {
    protected double timestamp;
    protected Distribution dtb;

    protected AbstractEvent(double timestamp) {
        this.timestamp = timestamp;
    }

}

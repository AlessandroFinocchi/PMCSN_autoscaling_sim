package it.uniroma2.models.events;

import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.sys.SystemState;
import lombok.Getter;

public abstract class Event {
    @Getter protected double timestamp;

    protected Event(double timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void process(SystemState s, EventVisitor visitor);

}

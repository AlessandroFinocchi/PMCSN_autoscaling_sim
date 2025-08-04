package it.uniroma2.models.events;

import it.uniroma2.exceptions.JobCompletionException;
import it.uniroma2.models.sys.SystemState;
import lombok.Getter;

public abstract class Event {
    @Getter private double timestamp;
    @Getter private EventType eventType;

    protected Event(double timestamp, EventType eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public abstract void process(SystemState s, EventVisitor visitor) throws JobCompletionException;

}

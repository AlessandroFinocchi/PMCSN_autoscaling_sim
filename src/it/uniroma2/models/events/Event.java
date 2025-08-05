package it.uniroma2.models.events;

import it.uniroma2.models.sys.SystemState;
import it.uniroma2.models.sys.SystemStats;
import lombok.Getter;

public abstract class Event {
    @Getter private double timestamp;
    @Getter private EventType eventType;

    protected Event(double timestamp, EventType eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public abstract void process(SystemState s, SystemStats stats, EventVisitor visitor);

}

package it.uniroma2.models.events;

import java.util.*;

public class EventCalendar {
    private final PriorityQueue<Event> events;

    public EventCalendar() {
        this.events = new PriorityQueue<>(
                (e1, e2) -> Double.compare(e1.getTimestamp(), e2.getTimestamp())
        );
    }

    /**
     * Adds event in the list, keeping it in order
     * @param e The event to add
     */
    public void addEvent(Event e) {
        events.add(e);
    }

    /**
     * Get event with minimum timestamp and removes it
     */
    public Event nextEvent() {
        return events.poll();
    }
}

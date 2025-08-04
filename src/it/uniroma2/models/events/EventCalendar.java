package it.uniroma2.models.events;

import java.util.*;

public class EventCalendar {
    private final Map<EventType, Event> events = new HashMap<>();

    /**
     * Adds event in the map
     * @param e The event to add
     */
    public void addEvent(Event e) {
        events.put(e.getEventType(), e);
    }

    /**
     * Get event with minimum timestamp
     */
    public Event nextEvent() {
        return events.values()
                .stream()
                .min(Comparator.comparing(Event::getTimestamp))
                .orElse(null);
    }
}

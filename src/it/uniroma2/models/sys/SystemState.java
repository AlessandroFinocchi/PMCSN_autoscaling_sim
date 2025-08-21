package it.uniroma2.models.sys;

import it.uniroma2.controllers.infrastructure.IServerInfrastructure;
import it.uniroma2.controllers.infrastructure.InfrastructureFactory;
import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.events.Event;
import it.uniroma2.models.events.EventCalendar;
import lombok.Getter;
import lombok.Setter;

import static it.uniroma2.models.Config.*;

public class SystemState {
    @Getter private final double capacity;
    @Getter @Setter private double current;                 /* current time                        */
    private final EventCalendar calendar;
    @Getter private final IServerInfrastructure servers;
    @Getter private final Distribution arrivalVA;
    @Getter private final Distribution servicesVA;

    public SystemState(EventCalendar calendar, Distribution arrivalVA, Distribution servicesVA) {
        this.capacity = WEBSERVER_CAPACITY;
        this.current = START;
        this.calendar = calendar;
        this.servers = InfrastructureFactory.create();
        this.arrivalVA = arrivalVA;
        this.servicesVA = servicesVA;
    }

    public void addEvent(Event event) {
        this.calendar.addEvent(event);
    }

    public boolean activeJobExists() {
        return servers.activeJobExists();
    }

    public void printStats() {
        servers.printStats(this.getCurrent());
    }
}
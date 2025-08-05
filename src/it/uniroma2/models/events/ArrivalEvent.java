package it.uniroma2.models.events;

import it.uniroma2.models.sys.SystemState;
import it.uniroma2.models.sys.SystemStats;

public class ArrivalEvent extends Event {
    public ArrivalEvent(double timestamp) {
        super(timestamp, EventType.ARRIVAL);
    }

    @Override
    public void process(SystemState s, SystemStats stats, EventVisitor visitor)  {
        visitor.visit(s,stats, this);
    }

}

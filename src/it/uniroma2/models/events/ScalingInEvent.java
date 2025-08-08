package it.uniroma2.models.events;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.sys.SystemState;

public class ScalingInEvent extends Event {
    public ScalingInEvent(double timestamp) {
        super(timestamp, EventType.ARRIVAL);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }

}
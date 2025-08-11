package it.uniroma2.models.events;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.sys.SystemState;

public class ScalingOutEvent extends Event {
    public ScalingOutEvent(double timestamp) {
        super(timestamp, EventType.SCALING_OUT);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }

}
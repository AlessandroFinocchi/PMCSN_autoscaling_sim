package it.uniroma2.models.events;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.sys.SystemState;

public class ScalingOutReqEvent extends Event {
    public ScalingOutReqEvent(double timestamp) {
        super(timestamp, EventType.SCALING_OUT_REQ);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }
}
package it.uniroma2.models.events;

import it.uniroma2.controllers.WebServer;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.sys.SystemState;
import lombok.Getter;
import lombok.Setter;

public class ScalingOutEvent extends Event {
    @Getter @Setter
    private WebServer target;

    public ScalingOutEvent(double timestamp, WebServer target) {
        super(timestamp, EventType.SCALING_OUT);
        this.target = target;
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }
}
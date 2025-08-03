package it.uniroma2.models.events;

import it.uniroma2.models.distr.Distribution;
import it.uniroma2.models.sys.SystemState;

public class CompletionEvent extends Event {
    public CompletionEvent(double timestamp) {
        super(timestamp);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) {
        visitor.visit(s, this);
    }
}

package it.uniroma2.models.events;

import it.uniroma2.exceptions.JobCompletionException;
import it.uniroma2.models.sys.SystemState;

public class CompletionEvent extends Event {
    public CompletionEvent(double timestamp) {
        super(timestamp, EventType.COMPLETION);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws JobCompletionException {
        visitor.visit(s, this);
    }
}

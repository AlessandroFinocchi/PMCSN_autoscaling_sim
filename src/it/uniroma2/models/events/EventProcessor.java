package it.uniroma2.models.events;

import it.uniroma2.models.sys.SystemState;

public class EventProcessor implements EventVisitor {
    @Override
    public void visit(SystemState s, ArrivalEvent event) {

    }

    @Override
    public void visit(SystemState s, CompletionEvent event) {

    }
}

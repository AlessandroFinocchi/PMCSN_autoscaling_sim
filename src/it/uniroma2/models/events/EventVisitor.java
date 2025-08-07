package it.uniroma2.models.events;

import it.uniroma2.models.sys.SystemState;

public interface EventVisitor {
    void visit(SystemState s, ArrivalEvent event) ;
    void visit(SystemState s, CompletionEvent event);
}

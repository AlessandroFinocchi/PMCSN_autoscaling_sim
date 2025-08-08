package it.uniroma2.models.events;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.sys.SystemState;

public interface EventVisitor {
    void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException;
    void visit(SystemState s, CompletionEvent event) throws IllegalLifeException;
}

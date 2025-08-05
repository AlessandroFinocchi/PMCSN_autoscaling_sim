package it.uniroma2.models.events;

import it.uniroma2.models.sys.SystemState;
import it.uniroma2.models.sys.SystemStats;

public interface EventVisitor {
    void visit(SystemState s, SystemStats stats, ArrivalEvent event) ;
    void visit(SystemState s, SystemStats stats, CompletionEvent event);
}

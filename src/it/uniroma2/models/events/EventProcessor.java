package it.uniroma2.models.events;

import it.uniroma2.controllers.ServerInfrastructure;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemState;

import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.models.Config.STOP;

public class EventProcessor implements EventVisitor {

    @Override
    public void visit(SystemState s, ArrivalEvent event)  {
        ServerInfrastructure servers = s.getServers();
        
        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        servers.computeJobsAdvancement(startTs, endTs, 0);

        /* Add the next job to the list */
        double nextServiceLife = s.getServicesVA().gen();
        Job newJob = new Job(endTs, nextServiceLife);
        servers.assignJob(newJob);

        /* Generate next completion */
        double nextCompletionTs = servers.computeNextCompletionTs(endTs);
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Generate next arrival if simulation is not finished*/
        if(endTs < STOP) {
            double nextArrivalTs = endTs + s.getArrivalVA().gen();
            Event nextArrival = new ArrivalEvent(nextArrivalTs);
            s.addEvent(nextArrival);
        } else s.addEvent(new ArrivalEvent(INFINITY));

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, CompletionEvent event) {
        ServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        servers.computeJobsAdvancement(startTs, endTs, 1);

        /* Generate next completion */
        double nextCompletionTs = servers.activeJobExists() ? servers.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }
}

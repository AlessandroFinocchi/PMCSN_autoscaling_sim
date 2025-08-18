package it.uniroma2.models.events;

import it.uniroma2.controllers.ServerInfrastructure;
import it.uniroma2.controllers.ServerState;
import it.uniroma2.controllers.WebServer;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemState;

import static it.uniroma2.models.Config.*;

public class EventProcessor implements EventVisitor {

    @Override
    public void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException {
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

        /* Generate next completion 0.08246927943190602*/
        double nextCompletionTs = servers.computeNextCompletionTs(endTs);
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Generate next arrival if simulation is not finished*/
        if (endTs < STOP) {
            double nextArrivalTs = endTs + s.getArrivalVA().gen();
            Event nextArrival = new ArrivalEvent(nextArrivalTs);
            s.addEvent(nextArrival);
        } else s.addEvent(new ArrivalEvent(INFINITY));

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, CompletionEvent event) throws IllegalLifeException {
        ServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        double movingMeanResponseTime = servers.computeJobsAdvancement(startTs, endTs, 1);

        /* Check scaling */
        if (movingMeanResponseTime > RESPONSE_TIME_OUT_THRESHOLD &&
                servers.getNumServersByState(ServerState.ACTIVE) + servers.getNumServersByState(ServerState.TO_BE_ACTIVE) < MAX_NUM_SERVERS)
            s.addEvent(new ScalingOutReqEvent(endTs));
        else if (movingMeanResponseTime < RESPONSE_TIME_IN_THRESHOLD &&
                servers.getNumServersByState(ServerState.ACTIVE) > 1)
            s.addEvent(new ScalingInEvent(endTs));


        /* Generate next completion */
        double nextCompletionTs = servers.activeJobExists() ? servers.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingOutReqEvent event) {
        ServerInfrastructure servers = s.getServers();
        double endTs = event.getTimestamp();

        // From request to effective scale out
        var serverTarget = servers.requestScaleOut(endTs);

        /* Find the earliest WS to make active: it needs to be done
           in case no scaling out event is already scheduled */
        WebServer nextScaleOut = servers.findNextScaleOut();
        s.addEvent(new ScalingOutEvent(nextScaleOut.getActivationTimestamp(), nextScaleOut));

        // Schedule to INFINITY the next request
        s.addEvent(new ScalingOutReqEvent(INFINITY));

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingOutEvent event) throws IllegalLifeException {
        ServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        servers.computeJobsAdvancement(startTs, endTs, 0);

        /* Set server to be effectively active */
        servers.scaleOut(endTs, event.getTarget());

        /* Find the earliest WS to make active */
        WebServer nextScaleOut = servers.findNextScaleOut();
        double nextActivationTS = nextScaleOut == null ? INFINITY : nextScaleOut.getActivationTimestamp();
        s.addEvent(new ScalingOutEvent(nextActivationTS, nextScaleOut));

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingInEvent event) {
        ServerInfrastructure servers = s.getServers();
        double endTs = event.getTimestamp();
        servers.scaleIn(endTs);
        s.addEvent(new ScalingInEvent(INFINITY));
        s.setCurrent(endTs);
    }
}

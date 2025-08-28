package it.uniroma2.models.events;

import it.uniroma2.controllers.infrastructure.IServerInfrastructure;
import it.uniroma2.controllers.servers.ServerState;
import it.uniroma2.controllers.servers.WebServer;
import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemState;

import static it.uniroma2.models.Config.*;

public class EventProcessor implements EventVisitor {

    @Override
    public void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException {
        IServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        servers.computeJobsAdvancement(startTs, endTs, false);

        /* Add the next job to the list */
        double nextServiceLife = s.getServicesVA().gen();
        Job newJob = new Job(endTs, nextServiceLife);
        servers.assignJob(newJob);

        servers.addJobsData(endTs, "ARRIVAL");

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
        IServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        double movingMeanResponseTime = servers.computeJobsAdvancement(startTs, endTs, true);

        servers.addJobsData(endTs, "COMPLETION");

        /* Check scaling */
        if (movingMeanResponseTime > RESPONSE_TIME_OUT_THRESHOLD &&
                servers.getNumWebServersByState(ServerState.ACTIVE) + servers.getNumWebServersByState(ServerState.TO_BE_ACTIVE) < MAX_NUM_SERVERS)
            s.addEvent(new ScalingOutReqEvent(endTs));
        else if (movingMeanResponseTime < RESPONSE_TIME_IN_THRESHOLD &&
                servers.getNumWebServersByState(ServerState.ACTIVE) > 1)
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
        IServerInfrastructure servers = s.getServers();
        double endTs = event.getTimestamp();

        /* Generate time for turn on a web sever */
        double turnOnTime = s.getTurnOnVA().gen();

        // From request to effective scale out
        var serverTarget = servers.requestScaleOut(endTs, turnOnTime);

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
        IServerInfrastructure servers = s.getServers();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        servers.computeJobsAdvancement(startTs, endTs, false);

        /* Set server to be effectively active */
        servers.scaleOut(endTs, event.getTarget());

        /* Find the earliest WS to make active */
        WebServer nextScaleOut = servers.findNextScaleOut();
        double nextActivationTS = nextScaleOut == null ? INFINITY : nextScaleOut.getActivationTimestamp();
        s.addEvent(new ScalingOutEvent(nextActivationTS, nextScaleOut));

        /* Generate next completion */
        double nextCompletionTs = servers.activeJobExists() ? servers.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingInEvent event) {
        IServerInfrastructure servers = s.getServers();
        double endTs = event.getTimestamp();

        servers.scaleIn(endTs);

        s.addEvent(new ScalingInEvent(INFINITY));

        /* Generate next completion */
        double nextCompletionTs = servers.activeJobExists() ? servers.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        s.setCurrent(endTs);
    }
}

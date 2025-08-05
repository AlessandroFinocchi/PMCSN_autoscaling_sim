package it.uniroma2.models.events;

import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemState;

import static it.uniroma2.models.Config.INFINITY;
import static it.uniroma2.models.Config.STOP;

public class EventProcessor implements EventVisitor {

    @Override
    public void visit(SystemState s, ArrivalEvent event)  {
        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Compute the advancement of each job */
        double quantum = (s.getCapacity() / s.getJobs().size()) * (endTs - startTs);
        for(Job job: s.getJobs()) {
            job.decreaseRemainingLife(quantum);
        }

        /* Add the next job to the list */
        double nextServiceLife = s.getServicesVA().gen();
        Job newJob = new Job(endTs, nextServiceLife);
        s.getJobs().add(newJob);

        /* Generate next completion */
        double nextCompletionTs = endTs + s.minRemainingLife() / (s.getCapacity() / s.getJobs().size());
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
        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Compute the advancement of each job */
        double quantum = (s.getCapacity() / s.getJobs().size()) * (endTs - startTs);
        s.removeMinRemainingLifeJob();
        for (Job job : s.getJobs()) {
            job.decreaseRemainingLife(quantum);
        }

        /* Generate next completion */
        double nextCompletionTs;
        if(s.jobActiveExist())
            nextCompletionTs = endTs + s.minRemainingLife() / (s.getCapacity() / s.getJobs().size());
        else
            nextCompletionTs = INFINITY;

        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }
}

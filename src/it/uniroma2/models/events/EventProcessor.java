package it.uniroma2.models.events;

import it.uniroma2.exceptions.JobCompletionException;
import it.uniroma2.models.Job;
import it.uniroma2.models.sys.SystemState;

import static it.uniroma2.models.Config.INFINITY;

public class EventProcessor implements EventVisitor {

    @Override
    public void visit(SystemState s, ArrivalEvent event) throws JobCompletionException {
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

        /* Generate next arrival */
        double nextArrivalTs = endTs + s.getArrivalVA().gen();
        Event nextArrival = new ArrivalEvent(nextArrivalTs);
        s.addEvent(nextArrival);

    }

    @Override
    public void visit(SystemState s, CompletionEvent event) {
        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Compute the advancement of each job */
        double quantum = (s.getCapacity() / s.getJobs().size()) * (endTs - startTs);
        for(Job job: s.getJobs()) {
            try {
                job.decreaseRemainingLife(quantum);
            } catch (JobCompletionException e) {
                s.removeJob(job);
            }
        }

        /* Generate next completion */
        double nextCompletionTs;
        if(s.jobActiveExist())
            nextCompletionTs = endTs + s.minRemainingLife() / (s.getCapacity() / s.getJobs().size());
        else
            nextCompletionTs = INFINITY;

        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);
    }
}

package it.uniroma2.controllers.servers;

import it.uniroma2.exceptions.IllegalLifeException;
import it.uniroma2.models.jobs.Job;
import it.uniroma2.models.jobs.JobList;
import it.uniroma2.models.sys.ServerStats;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import static it.uniroma2.models.Config.SLIDING_WINDOW_SIZE;
import static it.uniroma2.models.Config.STOP;

public abstract class AbstractServer implements IServer {
    private final DecimalFormat f;
    @Getter @Setter private ServerState serverState;
    @Getter protected double capacity;
    protected JobList jobs;
    @Getter protected ServerStats stats;
    @Getter private Deque<Double> movingWindowResponseTime;

    public AbstractServer(double capacity, ServerState serverState, int index) {
        this.serverState = serverState;
        this.capacity = capacity;
        this.jobs = new JobList();
        this.stats = new ServerStats(index);
        this.movingWindowResponseTime = new ArrayDeque<>();

        this.f = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        this.f.applyPattern("###0.00000000");
    }

    /**
     * Computes the advancement of all jobs using a Processor Sharing scheduling
     * @param startTs the interval start time
     * @param endTs the interval end time
     * @param completedJobResponseTime null if no job was completed, otherwise the response time of the completed job
     */
    @Override
    public void computeJobsAdvancement(double startTs, double endTs, Double completedJobResponseTime) throws IllegalLifeException {
        /*  At this point the job has already been removed, so if this is the server
         *   where completion happened, it must be taken into consideration */
        int completedJob = completedJobResponseTime == null ? 0 : 1;
        int jobAdvanced = jobs.size() + completedJob;

        stats.updateServerStats(startTs, endTs, jobAdvanced, completedJobResponseTime, this.serverState, this.capacity);

        if(completedJob == 1) {
            if(this.movingWindowResponseTime.size() == SLIDING_WINDOW_SIZE) {
                this.movingWindowResponseTime.removeFirst();
            }
            this.movingWindowResponseTime.addLast(completedJobResponseTime);
        }

        /* Compute the advancement of each job */
        double quantum = (this.capacity / jobAdvanced) * (endTs - startTs);
        try{
            for (Job job : this.jobs.getJobs()) {
                job.decreaseRemainingLife(quantum);
            }
        } catch (IllegalLifeException e) {
            System.out.printf("endTs: %f", endTs);
            throw e;
        }
    }

    @Override
    public void addJob(Job job) {
        jobs.add(job);
    }
    
    @Override
    public boolean activeJobExists(){
        return jobs.activeJobExists();
    }

    @Override
    public int size() {
        return jobs.size();
    }

    @Override
    public double getMinRemainingLife() {
        return jobs.minRemainingLife();
    }

    @Override
    public Job getMinRemainingLifeJob() {
        return jobs.getMinRemainingLifeJob();
    }

    @Override
    public void printServerStats(double currentTs) {
        System.out.println("for " + stats.getCompletedJobs() + " jobs");
        // STOP instead of currentTs, because the arrival process ends at STOP and the simulation ends when all the servers are empty
        System.out.println("   average interarrival time =   " + f.format(STOP / stats.getCompletedJobs()));
        System.out.println("   average response time ... =   " + f.format(stats.getNodeSum() / stats.getCompletedJobs()));
        System.out.println("   average service time .... =   " + f.format(stats.getServiceSum() / stats.getCompletedJobs()));
        System.out.println("   average # in the node ... =   " + f.format(stats.getNodeSum() / currentTs));
        System.out.println("   utilization ............. =   " + f.format(stats.getServiceSum() / currentTs));
        System.out.println("   jobs within 95-perc SLO . =   " + f.format(stats.getJobRespecting95percSLO()));
        System.out.println("   jobs within 99-perc SLO . =   " + f.format(stats.getJobRespecting99percSLO()));
        stats.getStationaryStats().printIntervalEstimation();
    }

    public void resetMovingExpMeanResponseTime() {
        this.movingWindowResponseTime = new ArrayDeque<>();
    }

    public double getWindowedMeanResponseTime() {
        double sum = 0.0;
        for(Double d : movingWindowResponseTime) {
            sum += d / movingWindowResponseTime.size();
        }
        return sum;
    }
}
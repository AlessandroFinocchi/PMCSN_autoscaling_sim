package it.uniroma2.models.sys;

import it.uniroma2.controllers.servers.ServerState;
import lombok.Getter;

import static it.uniroma2.models.Config.RESPONSE_TIME_SLO;

/**
 * Used for computing statistics
 */
public class ServerStats {
    @Getter private double nodeSum;                    /* mean population in system  intg(l(s))             */
    @Getter private double serviceSum;                 /* mean population in service intg(x(s))             */
    @Getter private int    completedJobs;              /* number of completed jobs                          */
    @Getter private double allocatedCapacity;          /* total allocated capacity per time                 */
    @Getter private int    completedJobsInTime;        /* number of jobs that completed withing the SLO     */
    @Getter private double currMeanResponseTime;       /* current mean response time                        */

    @Getter private StationaryStats stationaryStats;

    public ServerStats(int serverIndex){
        this.nodeSum           = 0.0;
        this.serviceSum        = 0.0;
        this.completedJobs     =   0;
        this.allocatedCapacity = 0.0;

        this.stationaryStats   = new StationaryStats(serverIndex);
    }

    /**
     * @param jobNum the number of jobs in the server from startTs to endTs
     * @param completedJobResponseTime null if no completion, otherwise the response time of the completed job an endTs
     */
    public void updateServerStats(double startTs, double endTs, double jobNum, Double completedJobResponseTime,
                                  ServerState serverState, double currentCapacity) {

        assert startTs >= 0 && endTs >= startTs && jobNum >= 0;

        /* Check if there has been a completion */
        boolean isCompletion = completedJobResponseTime != null;

        /* Update statistics */
        if(jobNum > 0) {
            this.nodeSum       += (endTs - startTs) * jobNum;
            this.serviceSum    += (endTs - startTs);
        }

        if(serverState == ServerState.ACTIVE || serverState == ServerState.TO_BE_REMOVED)
            this.allocatedCapacity += (endTs - startTs) * currentCapacity;

        if (isCompletion){
            this.completedJobs++;

            /* Update SLO indicator */
            if (completedJobResponseTime <= RESPONSE_TIME_SLO)
                completedJobsInTime++;

            /* Update mean response time */
            currMeanResponseTime += (completedJobResponseTime - currMeanResponseTime) / completedJobs;

            if (stationaryStats.advanceCounterReady())
                updateStationaryStats(endTs);
        }
    }

    private void updateStationaryStats(double endTs) {
        double currMeanJobNumber = this.nodeSum / endTs;
        double currMeanUtilization = this.getServiceSum() / endTs;
        double currMeanCapacityPerSec = this.getAllocatedCapacity() / endTs;
        double currMeanViolationPercentage = 1.0f - this.getCompletedJobsInTime() / (double) this.getCompletedJobs();
        stationaryStats.updateStats(
                endTs,
                this.currMeanResponseTime,
                currMeanJobNumber,
                currMeanUtilization,
                currMeanCapacityPerSec,
                currMeanViolationPercentage
        );
    }
}

package it.uniroma2.models.sys;

import it.uniroma2.controllers.servers.ServerState;
import lombok.Getter;

import static it.uniroma2.models.Config.RESPONSE_TIME_95PERC_SLO;
import static it.uniroma2.models.Config.RESPONSE_TIME_99PERC_SLO;

/**
 * Used for computing statistics
 */
public class ServerStats {
    @Getter private final int serverIndex;             /* server index                                      */
    @Getter private double nodeSum;                    /* mean population in system  intg(l(s))             */
    @Getter private double serviceSum;                 /* mean population in service intg(x(s))             */
    @Getter private int    completedJobs;              /* number of completed jobs                          */
    @Getter private double allocatedCapacity;          /* total allocated capacity per time                 */
    @Getter private int    jobRespecting95percSLO;     /* number of jobs that completed withing the SLO     */
    @Getter private int    jobRespecting99percSLO;     /* number of jobs that completed withing the SLO     */
    @Getter private double currMeanResponseTime;       /* current mean response time                        */

    @Getter private final StationaryStats stationaryStats;

    public ServerStats(int serverIndex){
        this.serverIndex            = serverIndex;
        this.nodeSum                = 0.0;
        this.serviceSum             = 0.0;
        this.completedJobs          =   0;
        this.allocatedCapacity      = 0.0;
        this.jobRespecting95percSLO =   0;
        this.jobRespecting99percSLO =   0;

        this.stationaryStats = new StationaryStats(serverIndex);
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
            this.nodeSum    += (endTs - startTs) * jobNum;
            this.serviceSum += (endTs - startTs);
        }

        if(serverState == ServerState.ACTIVE || serverState == ServerState.TO_BE_REMOVED)
            this.allocatedCapacity += (endTs - startTs) * currentCapacity;

        if (isCompletion){
            this.completedJobs++;

            /* Update SLO indicator */
            if (completedJobResponseTime <= RESPONSE_TIME_95PERC_SLO)
                this.jobRespecting95percSLO++;

            if(completedJobResponseTime <= RESPONSE_TIME_99PERC_SLO)
                this.jobRespecting99percSLO++;

            /* Update mean response time */
            currMeanResponseTime += (completedJobResponseTime - currMeanResponseTime) / completedJobs;

            if (stationaryStats.advanceCounterReady())
                updateStationaryStats(endTs);
        }
    }

    private void updateStationaryStats(double endTs) {
        double currMeanJobNumber                 = this.getNodeSum() / endTs;
        double currMeanUtilization               = this.getServiceSum() / endTs;
        double currMeanCapacityPerSec            = this.getAllocatedCapacity() / endTs;
        double currMean95percViolationPercentage = 1.0f - this.getJobRespecting95percSLO() / (double) this.getCompletedJobs();
        double currMean99percViolationPercentage = 1.0f - this.getJobRespecting99percSLO() / (double) this.getCompletedJobs();
        stationaryStats.updateStats(
                endTs,
                this.currMeanResponseTime,
                currMeanJobNumber,
                currMeanUtilization,
                currMeanCapacityPerSec,
                currMean95percViolationPercentage,
                currMean99percViolationPercentage
        );
    }
}

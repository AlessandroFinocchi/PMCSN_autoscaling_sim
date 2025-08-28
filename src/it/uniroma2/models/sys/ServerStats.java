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

    private StationaryStats stationaryStats;

    public ServerStats(int serverIndex){
        this.nodeSum           = 0.0;
        this.serviceSum        = 0.0;
        this.completedJobs     =   0;
        this.allocatedCapacity = 0.0;

        this.stationaryStats   = new StationaryStats(serverIndex);
    }

    public void updateServerStats(double startTs, double endTs, double jobNum, int completed,
                                  ServerState serverState, double currentCapacity) {
        assert startTs >= 0 && endTs >= startTs && jobNum >= 0;
        if(jobNum > 0) {
            this.nodeSum       += (endTs - startTs) * jobNum;
            this.serviceSum    += (endTs - startTs);
            this.completedJobs += completed;
        }

        if(serverState == ServerState.ACTIVE || serverState == ServerState.TO_BE_REMOVED)
            this.allocatedCapacity += (endTs - startTs) * currentCapacity;

        if (stationaryStats.advanceCounterReady())
            updateStationaryStats(endTs);
    }

    public void updateSLO(double responseTime) {
        if(responseTime <= RESPONSE_TIME_SLO)
            completedJobsInTime++;
    }

    private void updateStationaryStats(double endTs) {
        double currResponseTime = this.getNodeSum() / this.getCompletedJobs();
        stationaryStats.updateStats(endTs, currResponseTime);
    }
}

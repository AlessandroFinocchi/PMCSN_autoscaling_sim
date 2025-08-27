package it.uniroma2.models.sys;

import java.util.Arrays;
import java.util.List;

import static it.uniroma2.models.Config.SPIKESERVER_ACTIVE;
import static it.uniroma2.utils.DataCSVWriter.INTRA_RUN_DATA;
import static it.uniroma2.utils.DataField.*;

public class TransientStats {
    private List<ServerStats> stats;
    private double aggSystemResponseTime;
    private final Double[] aggServerResponseTimes;
    private double aggSystemJobNumber;
    private final Double[] aggServerJobNumber;
    private double aggSystemUtilization;
    private final Double[] aggServerUtilization;
    private double aggSystemAllocatedCapacityPerSec;
    private final Double[] aggServerAllocatedCapacityPerSec;

    public TransientStats(List<ServerStats> stats) {
        int numServers = stats.size();

        this.stats = stats;
        this.aggSystemResponseTime = 0.0;
        this.aggServerResponseTimes = new Double[numServers];
        this.aggSystemJobNumber = 0.0;
        this.aggServerJobNumber = new Double[numServers];
        this.aggSystemUtilization = 0.0;
        this.aggServerUtilization = new Double[numServers];
        this.aggSystemAllocatedCapacityPerSec = 0.0;
        this.aggServerAllocatedCapacityPerSec = new Double[numServers];

        // Init to 0.0 for avoiding NPE on Double[]
        Arrays.fill(this.aggServerResponseTimes, 0.0);
        Arrays.fill(this.aggServerJobNumber, 0.0);
        Arrays.fill(this.aggServerUtilization, 0.0);
        Arrays.fill(this.aggServerAllocatedCapacityPerSec, 0.0);
    }

    public void updateStats(int completionServerIndex, double startTs, double endTs, Double currJobResponseTime) {
        assert (completionServerIndex == -1 && currJobResponseTime == null) ||
               (completionServerIndex >=  0 && currJobResponseTime != null);

        /* Update transient statistics for completion events */
        if (completionServerIndex != -1) {
            ServerStats currServerStats = stats.get(completionServerIndex);
            double currSystemCompletedJobs = stats.stream().mapToInt(ServerStats::getCompletedJobs).sum();
            double currServerCompletedJobs = currServerStats.getCompletedJobs();
            this.aggSystemResponseTime =
                    (this.aggSystemResponseTime * (currSystemCompletedJobs-1) + currJobResponseTime)
                    / currSystemCompletedJobs;

            this.aggServerResponseTimes[completionServerIndex] =
                    (this.aggServerResponseTimes[completionServerIndex] * (currServerCompletedJobs-1) + currJobResponseTime)
                    / currServerCompletedJobs;
        }

        /* Update transient statistics for all events */
        // double meanCurrSystemJobNumber = stats.stream().mapToDouble(ServerStats::getNodeSum).sum() / endTs;
        double sum = 0.0;
        for (ServerStats serverStats : stats) {
            double nodeSum = serverStats.getNodeSum();
            sum += nodeSum;
        }
        double meanCurrSystemJobNumber = sum / endTs;
        double meanCurrSystemAllocatedCapacityPerSec = stats.stream().mapToDouble(ServerStats::getAllocatedCapacity).sum() / endTs;
        double meanCurrSystemUtilization =  stats.stream().mapToDouble(ServerStats::getServiceSum).sum() / endTs;

        // this.aggSystemJobNumber =
        //         (this.aggSystemJobNumber * startTs + (meanCurrSystemJobNumber) * (endTs - startTs))
        //         / endTs;

        this.aggSystemJobNumber = meanCurrSystemJobNumber;

        this.aggSystemUtilization =
                (this.aggSystemUtilization * startTs + meanCurrSystemUtilization * (endTs - startTs))
                        / endTs;

        this.aggSystemAllocatedCapacityPerSec =
                (this.aggSystemAllocatedCapacityPerSec * startTs + meanCurrSystemAllocatedCapacityPerSec * (endTs - startTs))
                        / endTs;

        for (ServerStats stat : stats){
            int serverIndex = stats.indexOf(stat);
            double meanCurrServerJobNumber = stat.getNodeSum() / endTs;
            double meanCurrServerUtilization = stat.getServiceSum() / endTs;
            double meanCurrServerAllocatedCapacityPerSec = stat.getAllocatedCapacity() / endTs;

            this.aggServerJobNumber[serverIndex] =
                    (this.aggServerJobNumber[serverIndex] * startTs + meanCurrServerJobNumber * (endTs - startTs))
                    / endTs;

            this.aggServerUtilization[serverIndex] =
                    (this.aggServerUtilization[serverIndex] * startTs + meanCurrServerUtilization * (endTs - startTs))
                    / endTs;

            this.aggServerAllocatedCapacityPerSec[serverIndex] =
                    (this.aggServerAllocatedCapacityPerSec[serverIndex] * startTs + meanCurrServerAllocatedCapacityPerSec * (endTs - startTs))
                    / endTs;
        }

        INTRA_RUN_DATA.addField(endTs, AGG_SYSTEM_RESPONSE_TIME, this.aggSystemResponseTime);
        INTRA_RUN_DATA.addField(endTs, AGG_SYSTEM_JOB_NUMBER, this.aggSystemJobNumber);
        INTRA_RUN_DATA.addField(endTs, AGG_SYSTEM_UTILIZATION, this.aggSystemUtilization);
        INTRA_RUN_DATA.addField(endTs, AGG_SYSTEM_ALLOCATED_CAPACITY_PER_SEC, this.aggSystemAllocatedCapacityPerSec);
        int start = SPIKESERVER_ACTIVE ? 0 : 1;
        for (int serverIndex = 0; serverIndex < stats.size(); serverIndex++) {
            INTRA_RUN_DATA.addFieldWithSuffix(
                    endTs, AGG_SERVER_RESPONSE_TIME, String.valueOf(serverIndex+start),
                    this.aggServerResponseTimes[serverIndex]
            );
            INTRA_RUN_DATA.addFieldWithSuffix(
                    endTs, AGG_SERVER_JOB_NUMBER, String.valueOf(serverIndex+start),
                    this.aggServerJobNumber[serverIndex]
            );
            INTRA_RUN_DATA.addFieldWithSuffix(
                    endTs, AGG_SERVER_UTILIZATION, String.valueOf(serverIndex+start),
                    this.aggServerUtilization[serverIndex]
            );
            INTRA_RUN_DATA.addFieldWithSuffix(
                    endTs, AGG_SERVER_ALLOCATED_CAPACITY_PER_SEC, String.valueOf(serverIndex+start),
                    this.aggServerAllocatedCapacityPerSec[serverIndex]
            );
        }
    }
}

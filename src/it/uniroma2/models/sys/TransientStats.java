package it.uniroma2.models.sys;

import java.util.Arrays;
import java.util.List;

import static it.uniroma2.utils.DataCSVWriter.INTRA_RUN_DATA;
import static it.uniroma2.utils.DataField.AGG_RESPONSE_TIME;

public class TransientStats {
    private List<ServerStats> stats;
    private double aggSystemResponseTime;
    private Double[] aggServerResponseTimes;
    private double aggSystemJobNumber;
    private Double[] aggServerJobNumber;
    private double aggSystemAllocatedCapacityPerSec;
    private double aggSystemUtilization;
    private Double[] aggServerUtilization;

    public TransientStats(List<ServerStats> stats) {
        int numServers = stats.size();

        this.stats = stats;
        this.aggSystemResponseTime = 0.0;
        this.aggServerResponseTimes = new Double[numServers];
        this.aggSystemJobNumber = 0.0;
        this.aggServerJobNumber = new Double[numServers];
        this.aggSystemAllocatedCapacityPerSec = 0.0;
        this.aggSystemUtilization = 0.0;
        this.aggServerUtilization = new Double[numServers];

        // Init to 0.0 for avoiding NPE on Double[]
        Arrays.fill(this.aggServerResponseTimes, 0.0);
        Arrays.fill(this.aggServerJobNumber, 0.0);
        Arrays.fill(this.aggServerUtilization, 0.0);

    }

    public void updateStats(int completionServerIndex, double startTs, double endTs, double currJobResponseTime) {
        ServerStats currServerStats = stats.get(completionServerIndex);

        /* get current values to update transient statistics */
        double currSystemCompletedJobs = stats.stream().mapToInt(ServerStats::getCompletedJobs).sum();
        double currServerCompletedJobs = currServerStats.getCompletedJobs();
        double meanCurrSystemJobNumber = stats.stream().mapToDouble(ServerStats::getNodeSum).sum() / endTs;
        double meanCurrServerJobNumber = currServerStats.getNodeSum() / endTs;
        double meanCurrSystemAllocatedCapacityPerSec = stats.stream().mapToDouble(ServerStats::getAllocatedCapacity).sum() / endTs;
        double meanCurrSystemUtilization =  stats.stream().mapToDouble(ServerStats::getServiceSum).sum() / endTs;
        double meanCurrServerUtilization = currServerStats.getServiceSum() / endTs;

        /* Update transient statistics */
        this.aggSystemResponseTime =
                (this.aggSystemResponseTime * (currSystemCompletedJobs-1) + currJobResponseTime)
                / currSystemCompletedJobs;

        this.aggServerResponseTimes[completionServerIndex] =
                (this.aggServerResponseTimes[completionServerIndex] * (currServerCompletedJobs-1) + currJobResponseTime)
                / currServerCompletedJobs;

        this.aggSystemJobNumber =
                (this.aggSystemJobNumber * startTs + (meanCurrSystemJobNumber) * (endTs - startTs))
                / endTs;

        this.aggServerJobNumber[completionServerIndex] =
                (this.aggServerJobNumber[completionServerIndex] * startTs + meanCurrServerJobNumber * (endTs - startTs))
                / endTs;

        this.aggSystemAllocatedCapacityPerSec =
                (this.aggSystemAllocatedCapacityPerSec * startTs + meanCurrSystemAllocatedCapacityPerSec * (endTs - startTs))
                / endTs;

        this.aggSystemUtilization =
                (this.aggSystemUtilization * startTs + meanCurrSystemUtilization * (endTs - startTs))
                / endTs;

        this.aggServerUtilization[completionServerIndex] =
                (this.aggServerUtilization[completionServerIndex] * startTs + meanCurrServerUtilization * (endTs - startTs))
                / endTs;

        // todo: result is obviously wrong
        INTRA_RUN_DATA.addField(endTs, AGG_RESPONSE_TIME, this.aggSystemResponseTime);

    }
}

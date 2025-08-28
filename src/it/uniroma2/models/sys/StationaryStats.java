package it.uniroma2.models.sys;

import static it.uniroma2.models.Config.STATS_BATCH_NUM;
import static it.uniroma2.models.Config.STATS_BATCH_SIZE;
import static it.uniroma2.utils.DataCSVWriter.INTRA_RUN_BM_DATA;
import static it.uniroma2.utils.DataField.*;

public class StationaryStats {
    private int counter;
    private int currBatch;
    private double sumBatchesResponseTime;
    private double sumBatchesJobNumber;
    private double sumBatchesUtilization;
    private double sumBatchesCapacityPerSec;
    private double sumBatchesViolationPercentage;
    private Integer serverIndex;

    /**
     * @param serverIndex null if system stats, not null if server stats
     */
    public StationaryStats(Integer serverIndex) {
        this.counter = 1;
        this.currBatch = 0;
        this.sumBatchesResponseTime = 0.0f;
        this.serverIndex = serverIndex;
    }

    private boolean isBatchEnd(){
        return this.counter % STATS_BATCH_SIZE == 0;
    }

    public boolean advanceCounterReady() {
        this.counter++;
        return isBatchEnd();
    }

    public void updateStats(double endTs, double currMeanResponseTime, double currMeanJobNumber,
                            double currMeanUtilization, double currMeanCapacityPerSec, double currMeanViolationPercentage) {
        /* Return if all batches were computed or the batch is not ready */
//        if (isCompleted()) return;

        /* Compute batch statistics */
        double currBatchResponseTime = (this.currBatch + 1) * currMeanResponseTime - this.sumBatchesResponseTime;
        double currBatchJobNumber = (this.currBatch + 1) * currMeanJobNumber - this.sumBatchesJobNumber;
        double currBatchUtilization = (this.currBatch + 1) * currMeanUtilization - this.sumBatchesUtilization;
        double currBatchCapacityPerSec = (this.currBatch + 1) * currMeanCapacityPerSec - this.sumBatchesCapacityPerSec;
        double currBatchViolationPercentage = (this.currBatch + 1) * currMeanViolationPercentage - this.sumBatchesViolationPercentage;

        /* Update statistic sums */
        this.sumBatchesResponseTime += currBatchResponseTime;
        this.sumBatchesJobNumber += currBatchJobNumber;
        this.sumBatchesUtilization += currBatchUtilization;
        this.sumBatchesCapacityPerSec += currBatchCapacityPerSec;
        this.sumBatchesViolationPercentage += currBatchViolationPercentage;

        /* System stationary stats */
        if (this.serverIndex == null){
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_RESPONSE_TIME, currBatchResponseTime);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_JOB_NUMBER, currBatchJobNumber);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_UTILIZATION, currBatchUtilization);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_ALLOCATED_CAPACITY_PER_SEC, currBatchCapacityPerSec);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_SLO_VIOLATIONS_PERC, currBatchViolationPercentage);
        }
        /* Server stationary stats */
        else {
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_RESPONSE_TIME, String.valueOf(serverIndex), currBatchResponseTime);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_JOB_NUMBER, String.valueOf(serverIndex), currBatchJobNumber);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_UTILIZATION, String.valueOf(serverIndex), currBatchUtilization);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_ALLOCATED_CAPACITY_PER_SEC, String.valueOf(serverIndex), currBatchCapacityPerSec);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_SLO_VIOLATIONS_PERC, String.valueOf(serverIndex), currBatchViolationPercentage);
        }

        this.currBatch++;
    }

    public boolean isCompleted(){
        return this.currBatch >= STATS_BATCH_NUM;
    }
}

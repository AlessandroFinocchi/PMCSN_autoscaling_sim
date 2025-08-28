package it.uniroma2.models.sys;

import java.util.Arrays;

import static it.uniroma2.models.Config.STATS_BATCH_NUM;
import static it.uniroma2.models.Config.STATS_BATCH_SIZE;
import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_DATA;
import static it.uniroma2.utils.DataCSVWriter.INTRA_RUN_DATA;
import static it.uniroma2.utils.DataField.*;

public class StationaryStats {
    private int counter;
    private int currBatch;
    private double sumBatchesResponseTime;
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

    public void updateStats(double endTs, double currMeanResponseTime) {
        /* Return if all batches were computed or the batch is not ready */
//        if (isCompleted()) return;

        /* Compute batch statistics */
        double currBatchResponseTime = (this.currBatch + 1) * currMeanResponseTime - this.sumBatchesResponseTime;

        /* Update statistic sums */
        this.sumBatchesResponseTime += currBatchResponseTime;

        /* System stationary stats */
        if (this.serverIndex == null){
            INTRA_RUN_DATA.addField(endTs, BM_SYSTEM_RESPONSE_TIME, currBatchResponseTime);
        }
        /* Server stationary stats */
        else {
            INTRA_RUN_DATA.addFieldWithSuffix(
                    endTs, BM_SERVER_RESPONSE_TIME, String.valueOf(serverIndex), currBatchResponseTime);
        }

        this.currBatch++;
    }

    public boolean isCompleted(){
        return this.currBatch >= STATS_BATCH_NUM;
    }
}

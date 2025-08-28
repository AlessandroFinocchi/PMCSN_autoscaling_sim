package it.uniroma2.models.sys;

import java.util.Arrays;

import static it.uniroma2.models.Config.STATS_BATCH_NUM;
import static it.uniroma2.models.Config.STATS_BATCH_SIZE;
import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_DATA;
import static it.uniroma2.utils.DataField.*;

public class StationaryStats {
    private int counter;
    private int currBatch;
    private final Double[] batchesResponseTimes;
    private Integer serverIndex;

    /**
     * @param serverIndex null if system stats, not null if server stats
     */
    public StationaryStats(Integer serverIndex) {
        this.counter = 1;
        this.currBatch = 0;
        this.batchesResponseTimes = new Double[STATS_BATCH_NUM];
        this.serverIndex = serverIndex;

        Arrays.fill(batchesResponseTimes, 0.0);
    }

    private boolean isBatchEnd(){
        return this.counter % STATS_BATCH_SIZE == 0;
    }

    public boolean advanceCounterReady() {
        this.counter++;
        return isBatchEnd();
    }

    public void updateStats(double endTs, double currMeanResponseTime) {
        /* Return if all batches were computed or the batch is not ready*/
        if (this.currBatch == STATS_BATCH_NUM || !isBatchEnd()) return;

        double sumResponseTimes = Arrays.stream(this.batchesResponseTimes).reduce(0.0, Double::sum);
        this.batchesResponseTimes[this.currBatch] =
                (this.currBatch + 1) * currMeanResponseTime - sumResponseTimes;

        /* System stationary stats */
        if (this.serverIndex == null){
            INTER_RUN_DATA.addField(
                    endTs, BM_SYSTEM_RESPONSE_TIME, this.batchesResponseTimes[this.currBatch]
            );
        }
        /* Server stationary stats */
        else {
            INTER_RUN_DATA.addFieldWithSuffix(
                    endTs, BM_SERVER_RESPONSE_TIME, String.valueOf(serverIndex), this.batchesResponseTimes[this.currBatch]
            );
        }

        this.currBatch++;
    }
}

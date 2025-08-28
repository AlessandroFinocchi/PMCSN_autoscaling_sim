package it.uniroma2.models.sys;

import java.util.Arrays;

import static it.uniroma2.models.Config.STATS_BATCH_NUM;
import static it.uniroma2.models.Config.STATS_BATCH_SIZE;

public class StationaryStats {
    private int counter;
    private int currBatch;
    private final Double[] batchesResponseTimes;

    public StationaryStats() {
        this.counter = 1;
        this.currBatch = 0;
        batchesResponseTimes = new Double[STATS_BATCH_NUM];

        Arrays.fill(batchesResponseTimes, 0.0);
    }

    private boolean isBatchEnd(){
        return this.counter % STATS_BATCH_SIZE == 0;
    }

    public boolean advanceCounterReady() {
        this.counter++;
        return isBatchEnd();
    }

    public void updateStats(double currMeanResponseTime) {
        /* Return if all batches were computed or the batch is not ready*/
        if (this.currBatch == STATS_BATCH_NUM || !isBatchEnd()) return;

        double sumResponseTimes = Arrays.stream(this.batchesResponseTimes).reduce(0.0, Double::sum);
        this.batchesResponseTimes[this.currBatch] =
                (this.currBatch + 1) * currMeanResponseTime - sumResponseTimes;

        this.currBatch++;
    }
}

package it.uniroma2.models.sys;

import it.uniroma2.libs.Rvms;
import it.uniroma2.models.Pair;

import java.text.DecimalFormat;
import java.util.*;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class StationaryStats {
    private final DecimalFormat f;
    private int counter;                /* For tracking when to trigger update*/
    private int currBatch;              /* For tracking the last processed batch */
    private final Integer serverIndex;  /* Null if System stats, not null if server stats */

    /* Metrics sums */
    private double sumBatchesResponseTime;
    private double sumBatchesJobNumber;
    private double sumBatchesUtilization;
    private double sumBatchesCapacityPerSec;
    private double sumBatches95percViolationPercentage;
    private double sumBatches99percViolationPercentage;

    /* Welford algorithm variables*/
    private double responseTimeX;
    private double responseTimeV;
    private double jobNumberX;
    private double jobNumberV;
    private double utilizationX;
    private double utilizationV;
    private double capacityPerSecX;
    private double capacityPerSecV;
    private double slo95percViolationPercentageX;
    private double slo95percViolationPercentageV;
    private double slo99percViolationPercentageX;
    private double slo99percViolationPercentageV;

    /**
     * @param serverIndex null if system stats, not null if server stats
     */
    public StationaryStats(Integer serverIndex) {
        this.counter = 1;
        this.currBatch = 0;

        this.serverIndex = serverIndex;
        this.sumBatchesResponseTime = 0.0f;
        this.sumBatchesJobNumber = 0.0f;
        this.sumBatchesUtilization = 0.0f;
        this.sumBatchesCapacityPerSec = 0.0f;
        this.sumBatches95percViolationPercentage = 0.0f;
        this.sumBatches99percViolationPercentage = 0.0f;

        /* Values to compute Welford's algorithm */
        this.responseTimeX = 0.0f;
        this.responseTimeV = 0.0f;
        this.jobNumberX = 0.0f;
        this.jobNumberV = 0.0f;
        this.utilizationX = 0.0f;
        this.utilizationV = 0.0f;
        this.capacityPerSecX = 0.0f;
        this.capacityPerSecV = 0.0f;
        this.slo95percViolationPercentageX = 0.0f;
        this.slo95percViolationPercentageV = 0.0f;
        this.slo99percViolationPercentageX = 0.0f;
        this.slo99percViolationPercentageV = 0.0f;

        this.f = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        this.f.applyPattern("###0.00000000");
    }

    private boolean isBatchEnd(){
        return this.counter % STATS_BATCH_SIZE == 0;
    }

    public boolean advanceCounterReady() {
        this.counter++;
        return isBatchEnd();
    }

    /**
     * This method computes for each metric, give their current means as parameters,
     * since the class keeps the sum all the previous batch means, the means of the current batch
     */
    public void updateStats(double endTs, double currMeanResponseTime, double currMeanJobNumber,
                            double currMeanUtilization, double currMeanCapacityPerSec,
                            double currMean95percViolationPercentage, double currMean99percViolationPercentage) {
        /* Return if all batches were computed or the batch is not ready */
//        if (isCompleted()) return;

        /* Compute batch statistics */
        double currBatchResponseTime = (this.currBatch + 1) * currMeanResponseTime - this.sumBatchesResponseTime;
        double currBatchJobNumber = (this.currBatch + 1) * currMeanJobNumber - this.sumBatchesJobNumber;
        double currBatchUtilization = (this.currBatch + 1) * currMeanUtilization - this.sumBatchesUtilization;
        double currBatchCapacityPerSec = (this.currBatch + 1) * currMeanCapacityPerSec - this.sumBatchesCapacityPerSec;
        double currBatch95percViolationPercentage = (this.currBatch + 1) * currMean95percViolationPercentage - this.sumBatches95percViolationPercentage;
        double currBatch99percViolationPercentage = (this.currBatch + 1) * currMean99percViolationPercentage - this.sumBatches99percViolationPercentage;

        /* Update statistic sums */
        this.sumBatchesResponseTime += currBatchResponseTime;
        this.sumBatchesJobNumber += currBatchJobNumber;
        this.sumBatchesUtilization += currBatchUtilization;
        this.sumBatchesCapacityPerSec += currBatchCapacityPerSec;
        this.sumBatches95percViolationPercentage += currBatch95percViolationPercentage;
        this.sumBatches99percViolationPercentage += currBatch99percViolationPercentage;

        /* System stationary stats */
        if (this.serverIndex == null){
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_RESPONSE_TIME, currBatchResponseTime);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_JOB_NUMBER, currBatchJobNumber);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_UTILIZATION, currBatchUtilization);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_ALLOCATED_CAPACITY_PER_SEC, currBatchCapacityPerSec);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_95PERC_SLO_VIOLATIONS_PERC, currBatch95percViolationPercentage);
            INTRA_RUN_BM_DATA.addField(endTs, BM_SYSTEM_99PERC_SLO_VIOLATIONS_PERC, currBatch99percViolationPercentage);
        }
        /* Server stationary stats */
        else {
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_RESPONSE_TIME, String.valueOf(serverIndex), currBatchResponseTime);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_JOB_NUMBER, String.valueOf(serverIndex), currBatchJobNumber);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_UTILIZATION, String.valueOf(serverIndex), currBatchUtilization);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_ALLOCATED_CAPACITY_PER_SEC, String.valueOf(serverIndex), currBatchCapacityPerSec);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_95PERC_SLO_VIOLATIONS_PERC, String.valueOf(serverIndex), currBatch95percViolationPercentage);
            INTRA_RUN_BM_DATA.addFieldWithSuffix(endTs, BM_SERVER_99PERC_SLO_VIOLATIONS_PERC, String.valueOf(serverIndex), currBatch99percViolationPercentage);
        }

        this.currBatch++;

        /* Welford pass */
        double responseTimeD = currBatchResponseTime - this.responseTimeX;
        this.responseTimeV += Math.pow(responseTimeD, 2) * (this.currBatch - 1) / this.currBatch;
        this.responseTimeX += responseTimeD / this.currBatch;

        double jobNumberD = currBatchJobNumber - this.jobNumberX;
        this.jobNumberV += Math.pow(jobNumberD, 2) * (this.currBatch - 1) / this.currBatch;
        this.jobNumberX += jobNumberD / this.currBatch;

        double utilizationD = currBatchUtilization - this.utilizationX;
        this.utilizationV += Math.pow(utilizationD, 2) * (this.currBatch - 1) / this.currBatch;
        this.utilizationX += utilizationD / this.currBatch;

        double capacityPerSecD = currBatchCapacityPerSec - this.capacityPerSecX;
        this.capacityPerSecV += Math.pow(capacityPerSecD, 2) * (this.currBatch - 1) / this.currBatch;
        this.capacityPerSecX += capacityPerSecD / this.currBatch;

        double slo95percViolationPercentageD = currBatch95percViolationPercentage - this.slo95percViolationPercentageX;
        this.slo95percViolationPercentageV += Math.pow(slo95percViolationPercentageD, 2) * (this.currBatch - 1) / this.currBatch;
        this.slo95percViolationPercentageX += slo95percViolationPercentageD / this.currBatch;

        double slo99percViolationPercentageD = currBatch99percViolationPercentage - this.slo99percViolationPercentageX;
        this.slo99percViolationPercentageV += Math.pow(slo99percViolationPercentageD, 2) * (this.currBatch - 1) / this.currBatch;
        this.slo99percViolationPercentageX += slo99percViolationPercentageD / this.currBatch;
    }

    public boolean isCompleted(){
        return this.currBatch >= STATS_BATCH_NUM;
    }

    public void printIntervalEstimation() {
        if (!LOG_BM || !this.isCompleted()) return;

        double x, s, u, t, w;
        double responseTimeS       = Math.sqrt(this.responseTimeV / this.currBatch);
        double jobNumberS          = Math.sqrt(this.jobNumberV / this.currBatch);
        double utilizationS        = Math.sqrt(this.utilizationV / this.currBatch);
        double capacityPerSecS     = Math.sqrt(this.capacityPerSecV / this.currBatch);
        double slo95percViolationS = Math.sqrt(this.slo95percViolationPercentageV / this.currBatch);
        double slo99percViolationS = Math.sqrt(this.slo99percViolationPercentageV / this.currBatch);

        double confidence = 1 - STATS_CONFIDENCE_ALPHA;
        Rvms rvms = new Rvms();
        u = 1.0 - 0.5 * STATS_CONFIDENCE_ALPHA;          /* interval parameter  */
        t = rvms.idfStudent(this.currBatch - 1, u); /* critical value of t */

        Map<String, Pair<Double, Double>> metrics = new LinkedHashMap<>();
        metrics.put("Response Time .....................", new Pair<>(this.responseTimeX, responseTimeS));
        metrics.put("Job Number ........................", new Pair<>(this.jobNumberX, jobNumberS));
        metrics.put("Utilization .......................", new Pair<>(this.utilizationX, utilizationS));
        metrics.put("Capacity per sec ..................", new Pair<>(this.capacityPerSecX, capacityPerSecS));
        metrics.put("95-perc SLO Violation Percentage ..", new Pair<>(this.slo95percViolationPercentageX, slo95percViolationS));
        metrics.put("99-perc SLO Violation Percentage ..", new Pair<>(this.slo99percViolationPercentageX, slo99percViolationS));

        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_RESPONSE_TIME, this.responseTimeX);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_JOB_NUMBER, this.jobNumberX);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_UTILIZATION, this.utilizationX);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_ALLOCATED_CAPACITY_PER_SEC, this.capacityPerSecX);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_95PERC_SLO_VIOLATIONS_PERC, this.slo95percViolationPercentageX);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, BM_SYSTEM_99PERC_SLO_VIOLATIONS_PERC, this.slo99percViolationPercentageX);

        System.out.println("Interval estimations based upon "+ this.currBatch + " data points " +
                "and with " + (int) (100.0 * confidence + 0.5) + "% confidence");
        for (Map.Entry<String, Pair<Double, Double>> entry : metrics.entrySet()) {
            String metric = entry.getKey();
            x = entry.getValue().getFirst();
            s = entry.getValue().getSecond();

            w = t * s / Math.sqrt(this.currBatch - 1);  /* interval half width */

            System.out.print(metric + " the expected value is in the interval " + f.format(x) + " +/- " + f.format(w) + "\n");
        }
    }
}

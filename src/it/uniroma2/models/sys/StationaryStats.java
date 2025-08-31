package it.uniroma2.models.sys;

import it.uniroma2.libs.Rvms;
import it.uniroma2.models.Pair;

import java.text.DecimalFormat;
import java.util.*;

import static it.uniroma2.models.Config.*;
import static it.uniroma2.utils.DataCSVWriter.INTRA_RUN_BM_DATA;
import static it.uniroma2.utils.DataField.*;

public class StationaryStats {
    private final DecimalFormat f;
    private int counter;
    private int currBatch;
    private final Integer serverIndex;

    /* Metrics sums */
    private double sumBatchesResponseTime;
    private double sumBatchesJobNumber;
    private double sumBatchesUtilization;
    private double sumBatchesCapacityPerSec;
    private double sumBatchesViolationPercentage;

    /* Welford algorithm variables*/
    private double responseTimeX;
    private double responseTimeV;
    private double jobNumberX;
    private double jobNumberV;
    private double utilizationX;
    private double utilizationV;
    private double capacityPerSecX;
    private double capacityPerSecV;
    private double violationPercentageX;
    private double violationPercentageV;

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
        this.sumBatchesViolationPercentage = 0.0f;

        this.responseTimeX = 0.0f;
        this.responseTimeV = 0.0f;
        this.jobNumberX = 0.0f;
        this.jobNumberV = 0.0f;
        this.utilizationX = 0.0f;
        this.utilizationV = 0.0f;
        this.capacityPerSecX = 0.0f;
        this.capacityPerSecV = 0.0f;
        this.violationPercentageX = 0.0f;
        this.violationPercentageV = 0.0f;

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

        double violationPercentageD = currBatchViolationPercentage - this.violationPercentageX;
        this.violationPercentageV += Math.pow(violationPercentageD, 2) * (this.currBatch - 1) / this.currBatch;
        this.violationPercentageX += violationPercentageD / this.currBatch;
    }

    public boolean isCompleted(){
        return this.currBatch >= STATS_BATCH_NUM;
    }

    public void printIntervalEstimation() {
        if (!LOG_BM) return;

        double x,s, u, t, w;
        double responseTimeS    = Math.sqrt(this.responseTimeV / this.currBatch);
        double jobNumberS       = Math.sqrt(this.jobNumberV / this.currBatch);
        double utilizationS     = Math.sqrt(this.utilizationV / this.currBatch);
        double capacityPerSecS  = Math.sqrt(this.capacityPerSecV / this.currBatch);
        double violationS       = Math.sqrt(this.violationPercentageV / this.currBatch);

        double confidence = 1 - STATS_CONFIDENCE_ALPHA;
        Rvms rvms = new Rvms();
        u = 1.0 - 0.5 * STATS_CONFIDENCE_ALPHA;                 /* interval parameter  */
        t = rvms.idfStudent(this.currBatch - 1, u);             /* critical value of t */

        Map<String, Pair<Double, Double>> metrics = new LinkedHashMap<>();
        metrics.put("Response Time ..........", new Pair<>(this.responseTimeX, responseTimeS));
        metrics.put("Job Number .............", new Pair<>(this.jobNumberX, jobNumberS));
        metrics.put("Utilization ............", new Pair<>(this.utilizationX, utilizationS));
        metrics.put("Capacity per sec .......", new Pair<>(this.capacityPerSecX, capacityPerSecS));
        metrics.put("Violation Percentage ...", new Pair<>(this.violationPercentageX, violationS));

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

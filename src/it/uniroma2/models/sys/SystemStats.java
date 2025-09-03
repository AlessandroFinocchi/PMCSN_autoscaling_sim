package it.uniroma2.models.sys;

import lombok.Getter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import static it.uniroma2.models.Config.SPIKE_CAPACITY;
import static it.uniroma2.utils.DataCSVWriter.*;
import static it.uniroma2.utils.DataField.*;

public class SystemStats {
    private final DecimalFormat f;
    List<ServerStats> stats;
    @Getter StationaryStats stationaryStats;

    public SystemStats(List<ServerStats> stats) {
        this.stats = stats;
        this.stationaryStats = new StationaryStats(null);

        this.f = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        this.f.applyPattern("###0.00000000");
    }

    public void updateStationaryStats(double currentTs) {
        if (!stationaryStats.advanceCounterReady()) return;

        double meanSystemResponseTime = 0.0f;
        double meanJobNumber = 0.0f;
        double systemUtilization = 0.0f;
        double totalAllocatedCapacityPerSec = 0.0f;
        double slo95percViolationsPercentage = 0.0f;
        double slo99percViolationsPercentage = 0.0f;
        int completedJobs = 0;

        for (ServerStats stat : stats) {
            meanSystemResponseTime += stat.getCurrMeanResponseTime() * stat.getCompletedJobs();
            meanJobNumber += stat.getNodeSum() / currentTs;
            systemUtilization += stat.getServiceSum() / currentTs;
            totalAllocatedCapacityPerSec += stat.getAllocatedCapacity();
            slo95percViolationsPercentage += stat.getCompletedJobs() - stat.getJobRespecting95percSLO();
            slo99percViolationsPercentage += stat.getCompletedJobs() - stat.getJobRespecting99percSLO();
            completedJobs += stat.getCompletedJobs();
        }

        meanSystemResponseTime /= completedJobs;
        totalAllocatedCapacityPerSec /= currentTs;
        slo95percViolationsPercentage /= completedJobs;
        slo99percViolationsPercentage /= completedJobs;

        stationaryStats.updateStats(currentTs, meanSystemResponseTime, meanJobNumber, systemUtilization,
                totalAllocatedCapacityPerSec, slo95percViolationsPercentage, slo99percViolationsPercentage);
    }

    public void processStats(double currentTs) {
        double systemUtilization = 0.0f;
        double totalAllocatedCapacity = 0.0f; // considering spike server always allocated (if spike server is active)
        double wsAllocatedCapacity = 0.0f;
        double spikeVirtualAllocatedCapacity = 0.0f; // considering spike server allocated only when non-empty (if spike server is active)
        double meanSystemResponseTime = 0.0f;
        int completedJobs = 0;
        int total95percSLOViolations = 0;
        int total99percSLOViolations = 0;

        for (ServerStats stat : stats) {
            systemUtilization += stat.getServiceSum() / currentTs;
            totalAllocatedCapacity += stat.getAllocatedCapacity();
            if (stat.getServerIndex() == 0){
                // Spike server
                spikeVirtualAllocatedCapacity += stat.getServiceSum() * SPIKE_CAPACITY;
            } else {
                // Web servers
                wsAllocatedCapacity += stat.getAllocatedCapacity();
            }
            meanSystemResponseTime += stat.getNodeSum();
            completedJobs += stat.getCompletedJobs();
            total95percSLOViolations += stat.getCompletedJobs() - stat.getJobRespecting95percSLO();
            total99percSLOViolations += stat.getCompletedJobs() - stat.getJobRespecting99percSLO();
        }

        meanSystemResponseTime /= completedJobs;

        INTER_RUN_DATA.addField(INTER_RUN_KEY, FINAL_TS, f.format(currentTs));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_ALLOCATED_CAPACITY, f.format(totalAllocatedCapacity));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, WEB_SERVER_ALLOCATED_CAPACITY, f.format((wsAllocatedCapacity)));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SPIKE_VIRTUAL_ALLOCATED_CAPACITY, f.format(spikeVirtualAllocatedCapacity));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, ALLOCATED_CAPACITY_PER_SEC, f.format(totalAllocatedCapacity / currentTs));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SYSTEM_UTILIZATION, f.format(systemUtilization));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, MEAN_SYSTEM_RESPONSE_TIME, f.format(meanSystemResponseTime));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_JOBS_COMPLETED, completedJobs);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SLO_95_VIOLATIONS, total95percSLOViolations);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SLO_99_VIOLATIONS, total99percSLOViolations);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SLO_95PERC_VIOLATIONS_PERCENTAGE, f.format((total95percSLOViolations * 1.0f) / completedJobs));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SLO_99PERC_VIOLATIONS_PERCENTAGE, f.format((total99percSLOViolations * 1.0f) / completedJobs));

        System.out.println();
        System.out.println("System utilization .................... = " + f.format(systemUtilization));
        System.out.println("Total Allocated Capacity per second ... = " + f.format(totalAllocatedCapacity / currentTs));
        System.out.println("Mean System Response .................. = " + f.format(meanSystemResponseTime));
        System.out.println("Total jobs 95-perc SLO violation ...... = " + f.format(total95percSLOViolations));
        System.out.println("Total jobs 99-perc SLO violation ...... = " + f.format(total99percSLOViolations));
        System.out.println("Perc  jobs 95-perc SLO violation ...... = " + f.format((total95percSLOViolations * 1.0f) / completedJobs));
        System.out.println("Perc  jobs 99-perc SLO violation ...... = " + f.format((total99percSLOViolations * 1.0f) / completedJobs));
    }
}

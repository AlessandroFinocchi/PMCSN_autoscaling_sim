package it.uniroma2.models.sys;

import lombok.Getter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

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
        double SLOViolationsPercentage = 0.0f;
        int completedJobs = 0;

        for (ServerStats stat : stats) {
            meanSystemResponseTime += stat.getCurrMeanResponseTime() * stat.getCompletedJobs();
            meanJobNumber += stat.getNodeSum() / currentTs;
            systemUtilization += stat.getServiceSum() / currentTs;
            totalAllocatedCapacityPerSec += stat.getAllocatedCapacity();
            SLOViolationsPercentage += stat.getCompletedJobs() - stat.getCompletedJobsInTime();
            completedJobs += stat.getCompletedJobs();
        }

        meanSystemResponseTime /= completedJobs;
        totalAllocatedCapacityPerSec /= currentTs;
        SLOViolationsPercentage /= completedJobs;

        stationaryStats.updateStats(currentTs, meanSystemResponseTime, meanJobNumber, systemUtilization,
                totalAllocatedCapacityPerSec, SLOViolationsPercentage);
    }

    public void processStats(double currentTs) {
        double systemUtilization = 0.0f;
        double totalAllocatedCapacity = 0.0f;
        double meanSystemResponseTime = 0.0f;
        int completedJobs = 0;
        int totalSLOViolations = 0;

        for (ServerStats stat : stats) {
            systemUtilization += stat.getServiceSum() / currentTs;
            totalAllocatedCapacity += stat.getAllocatedCapacity();
            meanSystemResponseTime += stat.getNodeSum();
            completedJobs += stat.getCompletedJobs();
            totalSLOViolations += stat.getCompletedJobs() - stat.getCompletedJobsInTime();
        }

        meanSystemResponseTime /= completedJobs;

        INTER_RUN_DATA.addField(INTER_RUN_KEY, FINAL_TS, f.format(currentTs));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_ALLOCATED_CAPACITY, f.format(totalAllocatedCapacity));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, ALLOCATED_CAPACITY_PER_SEC, f.format(totalAllocatedCapacity / currentTs));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SYSTEM_UTILIZATION, f.format(systemUtilization));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, MEAN_SYSTEM_RESPONSE_TIME, f.format(meanSystemResponseTime));
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_JOBS_COMPLETED, completedJobs);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, TOTAL_SLO_VIOLATIONS, totalSLOViolations);
        INTER_RUN_DATA.addField(INTER_RUN_KEY, SLO_VIOLATIONS_PERCENTAGE, f.format((totalSLOViolations * 1.0f) / completedJobs));

        System.out.println();
        System.out.println("System utilization .................... = " + f.format(systemUtilization));
        System.out.println("Total Allocated Capacity per second ... = " + f.format(totalAllocatedCapacity / currentTs));
        System.out.println("Mean System Response .................. = " + f.format(meanSystemResponseTime));
        System.out.println("Total jobs SLO violation .............. = " + f.format(totalSLOViolations));
        System.out.println("Perc jobs SLO violation ............... = " + f.format((totalSLOViolations * 1.0f) / completedJobs));
    }
}

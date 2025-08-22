package it.uniroma2.models.sys;

import java.text.DecimalFormat;
import java.util.List;

import static it.uniroma2.utils.DataCSVWriter.INTER_RUN_DATA;
import static it.uniroma2.utils.DataCSVWriter.RUN_FINISHED_KEY;
import static it.uniroma2.utils.DataField.*;

public class SystemStats {
    List<ServerStats> stats;

    public SystemStats(List<ServerStats> stats) {
        this.stats = stats;
    }

    public void processStats(DecimalFormat f, double currentTs) {
        double totalAllocatedCapacity = 0.0f;
        double meanSystemResponseTime = 0.0f;
        int completedJobs = 0;

        for (ServerStats stat : stats) {
            totalAllocatedCapacity += stat.getAllocatedCapacity();
            meanSystemResponseTime += stat.getNodeSum();
            completedJobs += stat.getCompletedJobs();
        }

        meanSystemResponseTime /= completedJobs;

        INTER_RUN_DATA.addField(RUN_FINISHED_KEY, FINAL_TS, f.format(currentTs));
        INTER_RUN_DATA.addField(RUN_FINISHED_KEY, TOTAL_ALLOCATED_CAPACITY, f.format(totalAllocatedCapacity));
        INTER_RUN_DATA.addField(RUN_FINISHED_KEY, TOTAL_ALLOCATED_CAPACITY_PER_SEC, f.format(totalAllocatedCapacity / currentTs));
        INTER_RUN_DATA.addField(RUN_FINISHED_KEY, MEAN_SYSTEM_RESPONSE_TIME, f.format(meanSystemResponseTime));


        System.out.println();
        System.out.println("Total Allocated Capacity per second ... = " + f.format(totalAllocatedCapacity / currentTs));
        System.out.println("Mean System Response .................. = " + f.format(meanSystemResponseTime));
    }
}

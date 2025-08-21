package it.uniroma2.models.sys;

import java.text.DecimalFormat;
import java.util.List;

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

        System.out.println();
        System.out.println("Total Allocated Capacity per second ... = " + f.format(totalAllocatedCapacity/currentTs));
        System.out.println("Mean System Response .................. = " + f.format(meanSystemResponseTime));
    }
}

package it.uniroma2.controllers.scheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static it.uniroma2.models.Config.SCHEDULER_TYPE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchedulerFactory {

    public static IScheduler create() {
        System.out.println("Using scheduler: " +  SCHEDULER_TYPE);
        return switch (SCHEDULER_TYPE) {
            case "leastUsed", "round_robin", "rr" -> new LeastUsedScheduler();
            case "roundRobin", "least_used", "lu", "" -> new RoundRobinScheduler();
            default -> throw new IllegalArgumentException("Invalid scheduler type: " + SCHEDULER_TYPE);
        };
    }
}

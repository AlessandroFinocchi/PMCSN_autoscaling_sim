package it.uniroma2.controllers.scheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static it.uniroma2.models.Config.SCHEDULER_TYPE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchedulerFactory {

    public static IScheduler create() {
        return switch (SCHEDULER_TYPE) {
            case "leastUsed", "round_robin", "rr" -> new RoundRobinScheduler();
            case "roundRobin", "least_used", "lu", "" -> new LeastUsedScheduler(); // default
            default -> throw new IllegalArgumentException("Invalid scheduler type: " + SCHEDULER_TYPE);
        };
    }
}

package it.uniroma2.utils;

public enum DataField {
    /* Intra run data fields */
    TIMESTAMP,
    R_0,
    MOVING_R_O,
    TO_BE_ACTIVE,
    ACTIVE,
    TO_BE_REMOVED,
    REMOVED,
    EVENT_TYPE,
    JOBS_IN_SERVER,
    STATUS_OF_SERVER,
    SPIKE_CURRENT_CAPACITY,
    /* Inter run data fields */
    FINAL_TS,
    TOTAL_ALLOCATED_CAPACITY,
    TOTAL_ALLOCATED_CAPACITY_PER_SEC,
    MEAN_SYSTEM_RESPONSE_TIME,
}

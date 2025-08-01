package it.uniroma2.models.sys;

import lombok.Getter;

/**
 * Used for computing statistics
 */
public class SystemStatsSum {
    public double node;                    /* time integrated number in the node  */
    public double queue;                   /* time integrated number in the queue */
    public double service;                 /* time integrated number in service   */

    public SystemStatsSum(){
        this.node    = 0.0;
        this.queue   = 0.0;
        this.service = 0.0;
    }
}

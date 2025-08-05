package it.uniroma2.models.sys;

import lombok.Getter;

/**
 * Used for computing statistics
 */
public class SystemStats {
    @Getter private double nodeSum;                    /* mean population in system  intg(l(s)) */
    @Getter private double queueSum;                   /* mean population in queue   intg(q(s)) */
    @Getter private double serviceSum;                 /* mean population in service intg(x(s)) */
    @Getter private int    completedJobs;              /* number of completed jobs              */

    public SystemStats(){
        this.nodeSum       = 0.0;
        this.queueSum      = 0.0;
        this.serviceSum    = 0.0;
        this.completedJobs =   0;
    }

    public void updateSystemStats(double startTs, double endTs, double jobNum, int completed) {
        assert startTs >= 0 && endTs >= startTs && jobNum >= 0;
        if(jobNum > 0) {
            this.nodeSum        = (endTs - startTs) * jobNum;
            this.queueSum       = (endTs - startTs) * (jobNum - 1);
            this.serviceSum     = (endTs - startTs);
            this.completedJobs += completed;
        }
    }
}

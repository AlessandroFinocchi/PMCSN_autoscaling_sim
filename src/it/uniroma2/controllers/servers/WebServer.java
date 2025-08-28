package it.uniroma2.controllers.servers;

import it.uniroma2.models.Job;
import lombok.Getter;
import lombok.Setter;

public class WebServer extends AbstractServer {
    @Getter @Setter private Double activationTimestamp;
    private double timeActive;

    public WebServer(double capacity, ServerState serverState, int index) {
        super(capacity, serverState, index);
    }
    
    public double getRemainingServerLife() {
        return jobs.getSumRemainingLife();
    }

    @Override
    public boolean removeJob(Job job) {
        jobs.removeJob(job);
        if (!this.activeJobExists() && this.getServerState() == ServerState.TO_BE_REMOVED) {
            this.setServerState(ServerState.REMOVED);
            return true;
        }
        return false;
    }
}

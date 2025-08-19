package it.uniroma2.models.sys;

import java.util.List;

public class SystemStats {
    List<ServerStats> stats;

    public SystemStats(List<ServerStats> stats) {
        this.stats = stats;
    }
    // todo Statistiche:
    //     * Tempo di risposta medio globale (vedi analisi op.)
    //     * Capacità computazionale totale allocata
}

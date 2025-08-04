package it.uniroma2.utils;


public class ProgressBar {
    private final double len;
    private double counter;
    private final long initialTime;

    public ProgressBar(double len) {
        this.len = len;
        this.counter = 0;
        this.initialTime = System.nanoTime();
    }

    public void update(double current) {
//        try { Thread.sleep(1); } catch (InterruptedException ignored) {}
        if (counter++ % 1000 == 0) {
            double percent =(current * 100) / this.len;
            long currentTime = System.nanoTime();
            long diffTime = currentTime - initialTime;
            long remainingTime = (long) (diffTime * 100 / percent) - diffTime;
            System.out.print(String.format(
                    "\rProgress: %.2f%% (%.2f/%.2f) | Elapsed %s | Remaining %s",
                    percent, current, this.len,
                    formatTime(diffTime),
                    formatTime(remainingTime)
            ));
            System.out.flush();
        }
    }

    private String formatTime(long nanos) {
        long seconds = (long) (nanos / 1e9);
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d",
                hours,
                minutes % 60,
                seconds % 60);
    }
}

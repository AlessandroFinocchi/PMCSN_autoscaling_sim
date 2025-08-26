package it.uniroma2.models.distr;
import static org.junit.Assert.assertTrue;

import it.uniroma2.libs.Rngs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static it.uniroma2.models.Config.SEED;

@RunWith(Parameterized.class)
public class TestCHyperExponential {

    private final double mean;
    private final double cv;
    private final double alpha;
    private final int n;
    private final int stream1;
    private final int stream2;
    private final int stream3;

    @Parameterized.Parameters(name = "mean={0}, cv={1}, alpha={2}, n={3}, stream1={4}, stream2={5}, stream3={6}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // mean, cv, alpha, n
                {0.05, 1.5, 0.05, 10000, 0, 1, 2},
                {0.10, 2.0, 0.01, 20000, 1, 2, 3},
                {0.15, 4.0, 0.05, 50000, 1, 3, 4},
                {0.20, 3.0, 0.10, 30000, 0, 2, 1},
                {0.25, 5.0, 0.05, 100000, 4, 2, 1},
                {0.30, 2.5, 0.01, 80000, 3, 2, 0},
                {0.40, 1.2, 0.05, 25000, 0, 2, 1},
                {0.50, 3.5, 0.05, 60000, 2, 3, 4},
                {0.60, 4.0, 0.10, 40000, 3, 4, 5},
                {0.75, 2.0, 0.05, 50000, 5, 6, 7}
        });
    }

    public TestCHyperExponential(double mean, double cv, double alpha, int n, int stream1, int stream2, int stream3) {
        this.mean = mean;
        this.cv = cv;
        this.alpha = alpha;
        this.n = n;
        this.stream1 = stream1;
        this.stream2 = stream2;
        this.stream3 = stream3;
    }

    private double computeSampleMean(CHyperExponential ch2, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += ch2.gen();
        }
        return sum / n;
    }

    @Test
    public void testH2() {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        // Hyperexponential creation
        CHyperExponential ch2 = new CHyperExponential(r, cv, mean, stream1, stream2, stream3);

        // Width of CI such that P[|X - E[X] > eps] <= alpha
        double eps = Math.sqrt((Math.pow(mean, 2) * cv) / (n * alpha));

        // Sample Mean
        double sampleMean = computeSampleMean(ch2, n);

        String message = String.format("After %d samples: %f < %f < %f", n, mean - eps, sampleMean, mean + eps);

        // With probability (1 - alpha) this test pass
        assertTrue(message, sampleMean >= mean - eps && sampleMean <= mean + eps);

        System.out.println(message);
    }
}

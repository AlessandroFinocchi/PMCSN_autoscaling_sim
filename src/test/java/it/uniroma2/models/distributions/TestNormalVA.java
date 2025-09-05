package it.uniroma2.models.distributions;

import it.uniroma2.libs.Rngs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static it.uniroma2.models.Config.SEED;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestNormalVA {
    private final double mean;
    private final double s2;
    private final int n;
    private final double alpha;
    private final int stream1;
    private final int stream2;

    public TestNormalVA(double mean, double s2, int n, double alpha, int stream1, int stream2) {
        this.mean = mean;
        this.s2 = s2;
        this.n = n;
        this.alpha = alpha;
        this.stream1 = stream1;
        this.stream2 = stream2;
    }


    @Parameterized.Parameters(name = "{index}: mean={0}, var={1}, n={2}, alpha={3}, stream1={4}, stream2={5}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0.0, 1.0, 100, 0.05, 0, 1},
                {5.0, 2.5, 1000, 0.1, 2, 3},
                {-3.0, 4.0, 10000, 0.01, 4, 5},
                {10.0, 0.1, 100000, 0.1, 6, 7},
                {2.5, 3.5, 500, 0.05, 8, 9},
                {-1.0, 0.5, 50, 0.2, 10, 11}
        });
    }

    private double computeSampleMean(Normal x, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += x.gen();
        }
        return sum / n;
    }

    @Test
    public void testNormalVA() {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        Normal normal = new Normal(r, stream1, stream2, mean, s2);

        // Width of CI such that P[|X - E[X] > eps] <= alpha
        double eps = Math.sqrt(s2 / (n * alpha));

        // Sample Mean
        double sampleMean = computeSampleMean(normal, n);

        String message = String.format("After %d samples: %f < %f < %f", n, sampleMean - eps, mean, sampleMean + eps);

        // With probability (1 - alpha) this test pass
        assertTrue(message, mean >= sampleMean - eps && mean <= sampleMean + eps);

        System.out.println(message);
    }

}

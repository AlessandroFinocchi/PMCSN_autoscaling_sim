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
public class TestBernoulliVA {
    private final double p;
    private final int stream;
    private final int n;
    private final double alpha;

    public TestBernoulliVA(double p, int n, double alpha, int stream) {
        this.p = p;
        this.n = n;
        this.alpha = alpha;
        this.stream = stream;
    }

    @Parameterized.Parameters(name = "{index}: p={0}, n={1}, alpha={2}, stream={3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0.1, 100, 0.05, 0},
                {0.5, 1000, 0.1, 1},
                {0.9, 5000, 0.01, 2},
                {0.25, 50, 0.2, 3},
                {0.75, 200, 0.05, 4},
                {0.33, 10000, 0.01, 5}
        });
    }

    private double computeSampleMean(Bernoulli bernoulli, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += bernoulli.gen();
        }
        return sum / n;
    }

    @Test
    public void testBernoulliVA() {
        Rngs r = new Rngs();
        r.plantSeeds(SEED);

        Bernoulli bernoulli = new Bernoulli(r, stream, p);

        // Width of CI such that P[|X - E[X] > eps] <= alpha
        double eps = Math.sqrt((p * (1 - p)) / (n * alpha));

        double sampleMean = computeSampleMean(bernoulli, n);

        String message = String.format("After %d samples: %f < %f < %f", n, p - eps, sampleMean, p + eps);

        // With probability (1 - alpha) this test pass
        assertTrue(message, sampleMean >= p - eps && sampleMean <= p + eps);

        System.out.println(message);

    }
}

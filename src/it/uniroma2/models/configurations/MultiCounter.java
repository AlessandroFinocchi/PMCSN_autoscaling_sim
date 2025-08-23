package it.uniroma2.models.configurations;

import lombok.Getter;
import lombok.Setter;

/**
 * This class allows keeping track of multiple subindexes to access all the possible combinations in a list of arrays.
 * It uses the concept of remainders, for example, given maxCounter=[2, 3]:
 *      [0, 0] -- .increment() --> [1, 0] --> [0, 1] --> [1, 1] --> [0, 2] --> [1, 2]
 */
public class MultiCounter {
    int length;
    @Getter
    @Setter
    int[] maxCounter;
    @Getter
    @Setter
    int[] counter;

    public MultiCounter(int length) {
        this.length = length;
        this.maxCounter = new int[length];
        this.counter = new int[length];
    }

    public void increment() {
        for (int i = 0; i < this.length; i++) {
            this.counter[i]++;
            if (this.counter[i] < this.maxCounter[i]) {
                break;
            } else {
                this.counter[i] = 0;
            }
        }
    }
}

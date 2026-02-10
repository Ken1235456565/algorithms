package com.phasmidsoftware.dsaipg.util.benchmark;

import com.phasmidsoftware.dsaipg.sort.elementary.InsertionSort;
import com.phasmidsoftware.dsaipg.util.config.Config;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InsertionSortBenchmarkTest {

    @Test
    public void testInsertionSortPerformance() throws IOException {
        Config config = Config.load(getClass());
        InsertionSort<Integer> sorter = new InsertionSort<>(config);

        int[] sizes = {1000, 2000, 4000, 8000, 16000};
        int nRuns = 100;

        System.out.println("=".repeat(70));
        System.out.printf("%-10s %-12s %-12s %-12s %-12s%n",
                "Size", "Random", "Ordered", "Partial", "Reverse");
        System.out.println("=".repeat(70));

        for (int n : sizes) {
            double[] times = new double[4];

            // 1. Random
            times[0] = runBenchmark(sorter, () -> generateRandom(n),
                    n, nRuns, config);

            // 2. Ordered
            times[1] = runBenchmark(sorter, () -> generateOrdered(n),
                    n, nRuns, config);

            // 3. Partially Ordered
            times[2] = runBenchmark(sorter, () -> generatePartial(n),
                    n, nRuns, config);

            // 4. Reverse
            times[3] = runBenchmark(sorter, () -> generateReverse(n),
                    n, nRuns, config);

            System.out.printf("%-10d %-12.2f %-12.2f %-12.2f %-12.2f%n",
                    n, times[0], times[1], times[2], times[3]);
        }

        System.out.println("=".repeat(70));
    }

    private double runBenchmark(InsertionSort<Integer> sorter,
                                Supplier<Integer[]> supplier,
                                int n, int nRuns, Config config) {
        Consumer<Integer[]> runner = arr -> sorter.sort(arr, 0, arr.length);

        Benchmark_Timer<Integer[]> benchmark = new Benchmark_Timer<>(
                "test-" + n, config, runner
        );

        return benchmark.runFromSupplier(supplier, nRuns);
    }

    private Integer[] generateRandom(int n) {
        Random random = new Random();
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = random.nextInt(n * 10);
        }
        return arr;
    }

    private Integer[] generateOrdered(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        return arr;
    }

    private Integer[] generatePartial(int n) {
        Random random = new Random();
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n / 2; i++) {
            arr[i] = i * 2;
        }
        for (int i = n / 2; i < n; i++) {
            arr[i] = random.nextInt(n * 10);
        }
        return arr;
    }

    private Integer[] generateReverse(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = n - i - 1;
        }
        return arr;
    }
}
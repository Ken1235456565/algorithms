package com.phasmidsoftware.dsaipg.sort.par;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class Main {

    static final int RUNS = 5;               // repetitions per config
    static final int[] ARRAY_SIZES = {
            500_000, 1_000_000, 2_000_000, 4_000_000
    };

    public static void main(String[] args) throws IOException {
        System.out.println("Available processors : " + Runtime.getRuntime().availableProcessors());
        System.out.println("Common pool parallelism: " + ForkJoinPool.getCommonPoolParallelism());

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("./src/result.csv")))) {

            // CSV header
            bw.write("scheme,arraySize,cutoff,depth,avgTimeMs\n");

            // ---- Scheme 1: vary cutoff ----------------------------------------
            int[] cutoffs = {5000, 10000, 50000, 100000, 250000, 500000};
            for (int size : ARRAY_SIZES) {
                for (int cutoff : cutoffs) {
                    ParSort.cutoff = cutoff;
                    double avg = benchmark(size, () -> {
                        int[] arr = makeArray(size);
                        ParSort.sort(arr, 0, arr.length);
                    });
                    String line = String.format("cutoff,%d,%d,N/A,%.1f%n", size, cutoff, avg);
                    System.out.print(line);
                    bw.write(line);
                }
            }

            // ---- Scheme 2: vary depth (threads = 2^depth) ---------------------
            int[] depths = {1, 2, 3, 4};   // 2, 4, 8, 16 partitions
            for (int size : ARRAY_SIZES) {
                for (int depth : depths) {
                    ParSort.maxDepth = depth;
                    double avg = benchmark(size, () -> {
                        int[] arr = makeArray(size);
                        ParSort.sortByDepth(arr, 0, arr.length, depth);
                    });
                    String line = String.format("depth,%d,N/A,%d,%.1f%n", size, depth, avg);
                    System.out.print(line);
                    bw.write(line);
                }
            }

            // ---- Scheme 3: combined (cutoff=50000, vary depth) ----------------
            ParSort.cutoff = 50_000;
            for (int size : ARRAY_SIZES) {
                for (int depth : depths) {
                    double avg = benchmark(size, () -> {
                        int[] arr = makeArray(size);
                        ParSort.sortCombined(arr, 0, arr.length, depth);
                    });
                    String line = String.format("combined,%d,50000,%d,%.1f%n", size, depth, avg);
                    System.out.print(line);
                    bw.write(line);
                }
            }

            // ---- Baseline: Arrays.sort (single-threaded) ----------------------
            for (int size : ARRAY_SIZES) {
                double avg = benchmark(size, () -> {
                    int[] arr = makeArray(size);
                    Arrays.sort(arr);
                });
                String line = String.format("baseline,%d,N/A,N/A,%.1f%n", size, avg);
                System.out.print(line);
                bw.write(line);
            }
        }
        System.out.println("Done. Results written to ./src/result.csv");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    static final Random RNG = new Random(42);

    static int[] makeArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = RNG.nextInt(10_000_000);
        return arr;
    }

    /** Runs task RUNS times, discards first run (warm-up), returns average ms. */
    static double benchmark(int size, Runnable task) {
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long t0 = System.currentTimeMillis();
            task.run();
            long t1 = System.currentTimeMillis();
            if (i > 0) total += (t1 - t0);   // skip warm-up
        }
        return (double) total / (RUNS - 1);
    }
}
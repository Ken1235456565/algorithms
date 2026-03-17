/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package com.phasmidsoftware.dsaipg.util.benchmark;

import com.phasmidsoftware.dsaipg.adt.pq.FibonacciHeap;
import com.phasmidsoftware.dsaipg.adt.pq.PQException;
import com.phasmidsoftware.dsaipg.adt.pq.PriorityQueue;
import com.phasmidsoftware.dsaipg.adt.pq.PriorityQueue_BinaryHeap;
import com.phasmidsoftware.dsaipg.util.config.Config;
import com.phasmidsoftware.dsaipg.util.general.Utilities;
import com.phasmidsoftware.dsaipg.util.logging.LazyLogger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.phasmidsoftware.dsaipg.util.benchmark.SortBenchmarkHelper.getWords;

/**
 * The {@code PQBenchmark} class is designed to benchmark operations performed with priority queues.
 * It includes methods to evaluate insertion and deletion performance using different configurations
 * and algorithms. This class uses external configuration for its settings and employs benchmarking
 * utilities to measure execution times of the operations.
 */
public class PQBenchmark {

    /**
     * For mergesort, the number of array accesses is actually six times the number of comparisons.
     * That's because, in addition to each comparison, there will be approximately two copy operations.
     * Thus, in the case where comparisons are based on primitives,
     * the normalized time per run should approximate the time for one array access.
     */

    static final int M       = 4095;   // 堆最大容量
    static final int INSERTS = 16_000; // 总插入次数
    static final int REMOVES = 4_000;  // 总删除次数
    static final int RUNS    = 2000;    // 基准测试重复次数

    final static LazyLogger logger = new LazyLogger(PQBenchmark.class);

    public final static TimeLogger[] timeLoggersLinearithmic = {
            new TimeLogger("Raw time per run (mSec): ", null),
            new TimeLogger("Normalized time per run (n log n): ", SortBenchmark::minComparisons)
    };

    /**
     * A static, final instance of {@link LazyLogger} used for logging in the {@code PQBenchmark} class.
     * This logger provides a flexible and efficient mechanism for generating log messages relevant
     * to the application's operations, such as benchmarking or configuration-related tasks.
     */
//    final static LazyLogger logger = new LazyLogger(PQBenchmark.class);

    /**
     * The main method serves as the entry point for the PQBenchmark application. It initializes
     * the configuration, logs application information, performs benchmarking for insertion and
     * deletion operations with and without Floyd's method, and outputs the results.
     *
     * @param args command-line arguments, expected to specify word counts for benchmarking;
     *             may be empty if no counts are provided.
     * @throws IOException if an error occurs during configuration loading.
     */
    public static void main(String[] args) throws IOException {
        Config config = Config.load(PQBenchmark.class);
        PQBenchmark bm = new PQBenchmark(config);

        System.out.println("M=" + M + ", inserts=" + INSERTS +
                ", removes=" + REMOVES + ", runs=" + RUNS);
        System.out.println("─".repeat(55));

        // 预生成随机数组（每次测试复用，保证公平性）
        int[] data = bm.generateRandom(INSERTS);

        double t1 = bm.runBenchmark("1. Binary Heap (no Floyd)", data, false, 2);
        double t2 = bm.runBenchmark("2. Binary Heap + Floyd",    data, true,  2);
        double t3 = bm.runBenchmark("3. 4-ary  Heap (no Floyd)", data, false, 4);
        double t4 = bm.runBenchmark("4. 4-ary  Heap + Floyd",    data, true,  4);
        double t5 = bm.runFibBenchmark("5. Fibonacci Heap",      data);      // ← 移到这里

        System.out.println("─".repeat(55));
        System.out.printf("%-30s %.4f ms%n", "1. Binary Heap (no Floyd):", t1);
        System.out.printf("%-30s %.4f ms%n", "2. Binary Heap + Floyd:",    t2);
        System.out.printf("%-30s %.4f ms%n", "3. 4-ary  Heap (no Floyd):", t3);
        System.out.printf("%-30s %.4f ms%n", "4. 4-ary  Heap + Floyd:",    t4);
        System.out.printf("%-30s %.4f ms%n", "5. Fibonacci Heap:",         t5);
    }

    private double runFibBenchmark(String label, int[] data) {
        Benchmark<Boolean> bm = new Benchmark_Timer<>(
                label, config, null,
                ignored -> doFibInsertDelete(data),
                null);
        double ms = bm.run(true, RUNS);
        // 报告溢出最高优先级
        FibonacciHeap<Integer> fh = new FibonacciHeap<>(M, true, Integer::compare);
        for (int v : data) fh.give(v);
        for (int i = 0; i < REMOVES; i++) if (!fh.isEmpty()) fh.take();
        System.out.printf("%-30s => %.4f ms  |  highestSpilled = %d%n",
                label, ms, fh.getHighestSpilled());
        return ms;
    }

    private void doFibInsertDelete(int[] data) {
        FibonacciHeap<Integer> fh = new FibonacciHeap<>(M, true, Integer::compare);
        for (int v : data) fh.give(v);
        for (int i = 0; i < REMOVES; i++) if (!fh.isEmpty()) fh.take();
    }

    private double runBenchmark(String label, int[] data,
                                boolean floyd, int d) {
        // fRun：插入 INSERTS 个元素 + 删除 REMOVES 个元素
        Benchmark<Boolean> benchmark = new Benchmark_Timer<>(
                label,
                config,
                null,
                ignored -> doInsertDelete(data, floyd, d),
                null
        );
        double ms = benchmark.run(true, RUNS);
        // 额外：报告溢出中最高优先级元素
        Integer spilled = getHighestSpilled(data, floyd, d);
        System.out.printf("%-30s => %.4f ms  |  highestSpilled = %d%n",
                label, ms, spilled);
        return ms;
    }

    private Integer getHighestSpilled(int[] data, boolean floyd, int d) {
        PriorityQueue_BinaryHeap<Integer> pq =
                new PriorityQueue_BinaryHeap<>(M, true, Integer::compare, floyd, d);
        for (int v : data) pq.give(v);
        for (int i = 0; i < REMOVES; i++) {
            try { pq.take(); } catch (PQException e) { break; }
        }
        return pq.getHighestSpilled();
    }

    private void doInsertDelete(int[] data, boolean floyd, int d) {
        PriorityQueue_BinaryHeap<Integer> pq =
                new PriorityQueue_BinaryHeap<>(M, true, Integer::compare, floyd, d);
        // 插入阶段（堆满后 give() 内部自动处理溢出）
        for (int v : data) pq.give(v);
        // 删除阶段
        for (int i = 0; i < REMOVES; i++) {
            try { pq.take(); } catch (PQException e) { break; }
        }
    }

    private int[] generateRandom(int n) {
        Random rng = new Random(42); // 固定种子，保证可复现
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rng.nextInt(1_000_000);
        return arr;
    }

    /**
     * This is the mean number of inversions in a randomly ordered set of n elements.
     * For insertion sort, each (low-level) swap fixes one inversion, so on average, this number of swaps is required.
     * The minimum number of comparisons is slightly higher.
     *
     * @param n the number of elements
     * @return one quarter n-squared more or less.
     */
    static double meanInversions(int n) {
        return 0.25 * n * (n - 1);
    }

    /**
     * Determines whether the specified configuration option in the given section is set to a boolean value.
     * TESTME
     *
     * @param section the name of the configuration section to check
     * @param option  the name of the configuration option within the specified section
     * @return true if the configuration option exists and is a boolean, false otherwise
     */
    boolean isConfigBoolean(String section, String option) {
        return config.getBoolean(section, option);
    }

    /**
     * Constructs a new instance of PQBenchmark with the specified configuration.
     *
     * @param config the configuration object used to set up the benchmark
     */
    public PQBenchmark(Config config) {
        this.config = config;
    }

    /**
     * Inserts and conditionally deletes elements from a priority queue using Floyd insertion or standard insertion.
     * This method processes an integer array by inserting elements into a priority queue and, based on a random condition,
     * attempts to remove an element from the queue.
     *
     * @param a     the array of integers to be inserted into the priority queue
     * @param floyd a flag that determines whether to use Floyd insertion method for the priority queue
     */
    // Insert and delete random integer array with floyd methods according to parameter
    private void insertArray(int[] a, final boolean floyd,int d) {
        PriorityQueue<Integer> pq = new PriorityQueue_BinaryHeap<>(a.length, true, Integer::compare, floyd, d);
        final Random random = new Random();
        for (int j : a) {
            pq.give(j);
            if (random.nextBoolean()) {
                try {
                    pq.take();
                } catch (PQException e) {
                    e.printStackTrace(); // TODO use logging
                }
            }
        }
    }

    /**
     * Performs a benchmark test by inserting and deleting elements, measuring the operation's execution time.
     * This method uses the Benchmark_Timer to calculate the average runtime for the given operation.
     *
     * @param n      the number of random integers to be generated and processed.
     * @param m      the number of times the benchmark test is repeated.
     * @param floyd  a flag indicating whether the Floyd's heap construction method should be used during the insertion.
     * @return the average execution time for the benchmark process, in milliseconds.
     */
    private double insertDeleteN(final int n, int m, final boolean floyd, int d) {
        final Random ran = new Random();
        int[] random = new int[n];
        for (int i = 0; i < n; i++) {
            random[i] = ran.nextInt(n);
        }
        Benchmark<Boolean> bm = new Benchmark_Timer<>(
                "testPQwithFloydOff",
                config,
                null,
                b -> insertArray(random, floyd, d),
                null);
        return bm.run(true, m);

    }

    private static Collection<String> lineAsList(String line) {
        List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    private static Collection<String> getLeipzigWords(String line) {
        return getWords(SortBenchmarkHelper.regexLeipzig, line);
    }

    // CONSIDER: to be eliminated soon.
    private Benchmark<LocalDateTime[]> benchmarkFactory(String description, Consumer<LocalDateTime[]> sorter, Consumer<LocalDateTime[]> checker) {
        return new Benchmark_Timer<>(
                description, config,
                (xs) -> Arrays.copyOf(xs, xs.length),
                sorter,
                checker);
    }

    private static final double LgE = Utilities.lg(Math.E);

    private final Config config;
}
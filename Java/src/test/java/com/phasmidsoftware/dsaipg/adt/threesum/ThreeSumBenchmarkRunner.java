package com.phasmidsoftware.dsaipg.adt.threesum;
import java.util.function.Supplier;

public class ThreeSumBenchmarkRunner {
    public static void main(String[] args) {
        // 设定 N 的测试值 (建议从较小的值开始，因为 Cubic 很慢)
        int[] nValues = {50, 100, 200, 400, 800, 1600, 3200, 6400, 12800};

        System.out.printf("%-10s %-15s %-15s %-15s%n", "N", "Cubic (ms)", "Quadratic (ms)", "Quadrithmic (ms)");

        for (int n : nValues) {
            // 1. 生成数据 (使用 Source 类)
            // m (值域) 设得大一点以减少重复，seed 可以固定以便复现
            Supplier<int[]> supplier = new Source(n, n * 10, 0L).intsSupplier(10);
            int[] input = supplier.get();

            // 2. 运行并计时 Cubic (O(N^3))
            // 注意：当 N 很大时 (比如 > 1600)，Cubic 可能会非常慢，可以根据情况跳过
            long startCubic = System.nanoTime();
            if (n <= 1600) {
                new ThreeSumCubic(input).getTriples();
            }
            long timeCubic = (System.nanoTime() - startCubic) / 1_000_000; // 转换为毫秒

            // 3. 运行并计时 Quadratic (O(N^2))
            long startQuad = System.nanoTime();
            new ThreeSumQuadratic(input).getTriples();
            long timeQuad = (System.nanoTime() - startQuad) / 1_000_000;

            // 4. 运行并计时 Quadrithmic (O(N^2 log N) 或 O(N log N) 取决于具体实现)
            // 这里假设 Quadrithmic 需要排序，通常实现内部会处理，或者你传入前需要排序
            long startQuadrithmic = System.nanoTime();
            new ThreeSumQuadrithmic(input).getTriples();
            long timeQuadrithmic = (System.nanoTime() - startQuadrithmic) / 1_000_000;

            // 5. 打印结果
            String cubicTimeStr = (n > 1600) ? "-" : String.valueOf(timeCubic);
            System.out.printf("%-10d %-15s %-15d %-15d%n", n, cubicTimeStr, timeQuad, timeQuadrithmic);
        }
    }
}

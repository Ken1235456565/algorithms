/*
 * Copyright (c) 2017. Phasmid Software
 */

package com.phasmidsoftware.dsaipg.misc;

import java.util.function.DoubleFunction;

/**
 * The NewtonApproximation class demonstrates the Newton-Raphson method to find an approximate solution
 * for the equation cos(x) = x.
 * <p>
 * The Newton-Raphson method is an iterative numerical technique used to approximate the roots of a real-valued function.
 * In this case, the method solves the equation by iterating until the calculated y value (cos(x) - x) is close
 * enough to zero within a given tolerance.
 * <p>
 * The iterative formula used is:
 * x = x + y / (sin(x) + 1)
 * where:
 * y = cos(x) - x
 * <p>
 * The solution stops when the absolute value of y is less than the specified tolerance (1E-7).
 * <p>
 * This implementation demonstrates the use of a fixed iteration limit (200) to ensure termination,
 * and it displays the found solution if the convergence criterion is met within this limit.
 */
class NewtonApproximation {
    /**
     * The main method demonstrates the use of the Newton-Raphson approximation method to solve the equation cos(x) = x.
     * It iteratively refines the approximation of x until the difference (y = cos(x) - x) is within a specified tolerance (1E-7).
     * The iteration stops after a fixed number of attempts (200) or upon finding a solution that meets the precision criterion.
     *
     * @param args command-line arguments; not utilized in this implementation
     */
//    public static void main(String[] args) {
//        // Newton's Approximation to solve cos(x) = x
//        double x = 1.0;
//        int left = 200;
//        for (; left > 0; left--) {
//            final double y = Math.cos(x) - x;
//            if (Math.abs(y) < 1E-7) {
//                System.out.println("the solution to cos(x)=x is: " + x);
//                System.exit(0);
//            }
//            x = x + y / (Math.sin(x) + 1);
//        }
//    }

    public static void main(String[] args) {
        // 1. 定义函数 f(x) = x^2 - 2
        DoubleFunction<Double> f = x -> x * x - 2;
        // 2. 定义导数 f'(x) = 2x
        DoubleFunction<Double> df = x -> 2 * x;

        double x = 1.0; // 初始猜测值
        int maxIterations = 200;
        double tolerance = 1E-10; // 精度控制

        for (int i = 0; i < maxIterations; i++) {
            double y = f.apply(x);

            // 如果足够接近 0，则停止迭代
            if (Math.abs(y) < tolerance) {
                System.out.println("the solution to x^2 - 2 = 0 is: " + x);
                System.exit(0);
            }

            // 牛顿迭代公式: x = x - f(x) / f'(x)
            x = x - y / df.apply(x);
        }
    }
}
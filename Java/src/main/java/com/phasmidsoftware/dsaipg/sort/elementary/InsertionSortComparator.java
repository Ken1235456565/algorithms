package com.phasmidsoftware.dsaipg.sort.elementary;

import com.phasmidsoftware.dsaipg.sort.helper.Helper;

import java.util.Comparator;

public class InsertionSortComparator<X> {

    private final Helper<X> helper;

    public InsertionSortComparator(Helper<X> helper) {
        this.helper = helper;
    }

    /**
     * Sort xs[from..to) in place using insertion sort.
     */
    public void sort(X[] xs, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            X key = xs[i];
            int j = i - 1;
            while (j >= from && helper.compare(xs[j], key) > 0) {
                xs[j + 1] = xs[j];
                j--;
            }
            xs[j + 1] = key;
        }
    }

    /**
     * Count the number of inversions in array xs using the given comparator.
     */
    public static <X> long countInversions(X[] xs, Comparator<X> comparator) {
        long count = 0;
        for (int i = 0; i < xs.length - 1; i++)
            for (int j = i + 1; j < xs.length; j++)
                if (comparator.compare(xs[i], xs[j]) > 0)
                    count++;
        return count;
    }
}
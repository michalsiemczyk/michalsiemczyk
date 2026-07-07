package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/**
 * Quick sort z partycjonowaniem Lomuto i pivotem-mediana z trzech,
 * co chroni przed degradacja O(n^2) na danych prawie posortowanych.
 */
public final class QuickSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        quicksort(a, 0, a.length - 1, out);
        out.accept(SortStep.done());
    }

    private void quicksort(int[] a, int lo, int hi, Consumer<SortStep> out) {
        if (lo > hi) {
            return;
        }
        if (lo == hi) {
            out.accept(SortStep.markSorted(lo));
            return;
        }
        int p = partition(a, lo, hi, out);
        out.accept(SortStep.markSorted(p)); // pivot jest juz na swoim miejscu
        quicksort(a, lo, p - 1, out);
        quicksort(a, p + 1, hi, out);
    }

    private int partition(int[] a, int lo, int hi, Consumer<SortStep> out) {
        medianOfThreeToEnd(a, lo, hi, out);
        int pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            out.accept(SortStep.compare(j, hi));
            if (a[j] < pivot) {
                i++;
                if (i != j) {
                    swap(a, i, j, out);
                }
            }
        }
        if (i + 1 != hi) {
            swap(a, i + 1, hi, out);
        }
        return i + 1;
    }

    /** Ustawia mediane z {lo, mid, hi} na pozycji hi, skad zostanie uzyta jako pivot. */
    private void medianOfThreeToEnd(int[] a, int lo, int hi, Consumer<SortStep> out) {
        int mid = lo + (hi - lo) / 2;
        out.accept(SortStep.compare(lo, mid));
        if (a[mid] < a[lo]) {
            swap(a, lo, mid, out);
        }
        out.accept(SortStep.compare(lo, hi));
        if (a[hi] < a[lo]) {
            swap(a, lo, hi, out);
        }
        out.accept(SortStep.compare(mid, hi));
        if (a[mid] < a[hi]) {
            swap(a, mid, hi, out);
        }
    }
}

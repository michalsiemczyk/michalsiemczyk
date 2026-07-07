package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/**
 * Merge sort (top-down). Scalanie odbywa sie przez bufor pomocniczy,
 * a kazdy zapis do tablicy glownej jest emitowany jako OVERWRITE.
 */
public final class MergeSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        if (a.length > 1) {
            mergesort(a, new int[a.length], 0, a.length - 1, out);
        }
        out.accept(SortStep.done());
    }

    private void mergesort(int[] a, int[] buf, int lo, int hi, Consumer<SortStep> out) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergesort(a, buf, lo, mid, out);
        mergesort(a, buf, mid + 1, hi, out);
        merge(a, buf, lo, mid, hi, out);
    }

    private void merge(int[] a, int[] buf, int lo, int mid, int hi, Consumer<SortStep> out) {
        System.arraycopy(a, lo, buf, lo, hi - lo + 1);
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = buf[j++];
            } else if (j > hi) {
                a[k] = buf[i++];
            } else {
                out.accept(SortStep.compare(i, j));
                a[k] = (buf[i] <= buf[j]) ? buf[i++] : buf[j++];
            }
            out.accept(SortStep.overwrite(k, a[k]));
        }
    }
}

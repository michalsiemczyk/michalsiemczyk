package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/** Sortowanie przez wybieranie - minimum trafia na poczatek nieposortowanej czesci. */
public final class SelectionSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                out.accept(SortStep.compare(min, j));
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min, out);
            }
            out.accept(SortStep.markSorted(i));
        }
        if (n > 0) {
            out.accept(SortStep.markSorted(n - 1));
        }
        out.accept(SortStep.done());
    }
}

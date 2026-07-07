package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/** Sortowanie babelkowe z wczesnym wyjsciem, gdy przebieg nie wykonal zadnej zamiany. */
public final class BubbleSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                out.accept(SortStep.compare(j, j + 1));
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1, out);
                    swapped = true;
                }
            }
            out.accept(SortStep.markSorted(n - 1 - i));
            if (!swapped) {
                break;
            }
        }
        out.accept(SortStep.done());
    }
}

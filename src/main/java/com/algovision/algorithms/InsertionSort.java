package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/** Sortowanie przez wstawianie - przesuwanie elementow emitowane jako OVERWRITE. */
public final class InsertionSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0) {
                out.accept(SortStep.compare(j, j + 1));
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                out.accept(SortStep.overwrite(j + 1, a[j]));
                j--;
            }
            if (a[j + 1] != key) {
                a[j + 1] = key;
                out.accept(SortStep.overwrite(j + 1, key));
            }
        }
        out.accept(SortStep.done());
    }
}

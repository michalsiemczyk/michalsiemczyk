package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/** Heap sort na kopcu max budowanym w miejscu. */
public final class HeapSort implements SortingAlgorithm {

    @Override
    public void sort(int[] a, Consumer<SortStep> out) {
        int n = a.length;
        // budowa kopca od ostatniego rodzica w dol
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, i, n, out);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end, out); // maksimum trafia na koniec
            out.accept(SortStep.markSorted(end));
            siftDown(a, 0, end, out);
        }
        if (n > 0) {
            out.accept(SortStep.markSorted(0));
        }
        out.accept(SortStep.done());
    }

    private void siftDown(int[] a, int root, int size, Consumer<SortStep> out) {
        while (true) {
            int child = 2 * root + 1;
            if (child >= size) {
                return;
            }
            if (child + 1 < size) {
                out.accept(SortStep.compare(child, child + 1));
                if (a[child + 1] > a[child]) {
                    child++;
                }
            }
            out.accept(SortStep.compare(root, child));
            if (a[root] >= a[child]) {
                return;
            }
            swap(a, root, child, out);
            root = child;
        }
    }
}

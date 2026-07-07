package com.algovision.algorithms;

import java.util.function.Supplier;

/**
 * Typy algorytmow sortowania - fabryka strategii dla UI.
 * UI operuje wylacznie na tym enumie i interfejsie {@link SortingAlgorithm}.
 */
public enum SortAlgorithmType {
    BUBBLE("Bubble Sort", BubbleSort::new),
    INSERTION("Insertion Sort", InsertionSort::new),
    SELECTION("Selection Sort", SelectionSort::new),
    QUICK("Quick Sort", QuickSort::new),
    MERGE("Merge Sort", MergeSort::new),
    HEAP("Heap Sort", HeapSort::new);

    private final String displayName;
    private final Supplier<SortingAlgorithm> factory;

    SortAlgorithmType(String displayName, Supplier<SortingAlgorithm> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    public SortingAlgorithm create() {
        return factory.get();
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

package com.algovision.algorithms;

import com.algovision.model.SortStep;

import java.util.function.Consumer;

/**
 * Wspolny interfejs strategii sortowania.
 * Implementacja sortuje przekazana tablice "w miejscu" i emituje kroki
 * ({@link SortStep}) opisujace kazda operacje - to jedyny kanal komunikacji
 * z warstwa UI. Algorytm nigdy nie dotyka JavaFX.
 */
public interface SortingAlgorithm {

    /**
     * Sortuje tablice rosnaco, emitujac kroki do przekazanego konsumenta.
     * Ostatnim wyemitowanym krokiem musi byc {@code SortStep.done()}.
     */
    void sort(int[] array, Consumer<SortStep> out);

    /** Pomocnicza zamiana elementow z emisja kroku SWAP. */
    default void swap(int[] a, int i, int j, Consumer<SortStep> out) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        out.accept(SortStep.swap(i, j));
    }
}

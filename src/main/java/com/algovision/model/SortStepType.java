package com.algovision.model;

/** Rodzaj pojedynczego kroku emitowanego przez algorytm sortujacy. */
public enum SortStepType {
    /** Porownanie dwoch elementow (indexA, indexB). */
    COMPARE,
    /** Zamiana miejscami dwoch elementow (indexA, indexB). */
    SWAP,
    /** Nadpisanie pojedynczej komorki wartoscia (indexA, value) - np. merge sort. */
    OVERWRITE,
    /** Element na pozycji indexA trafil na swoje ostateczne miejsce. */
    MARK_SORTED,
    /** Algorytm zakonczyl prace - cala tablica jest posortowana. */
    DONE
}

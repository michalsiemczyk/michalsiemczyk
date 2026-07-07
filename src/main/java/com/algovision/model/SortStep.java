package com.algovision.model;

/**
 * Niemutowalny opis pojedynczego kroku sortowania.
 * Algorytmy emituja strumien takich rekordow, a UI odtwarza je bez znajomosci
 * szczegolow implementacyjnych algorytmu (wzorzec Strategia + playback krokow).
 */
public record SortStep(SortStepType type, int indexA, int indexB, int value) {

    public static SortStep compare(int i, int j) {
        return new SortStep(SortStepType.COMPARE, i, j, 0);
    }

    public static SortStep swap(int i, int j) {
        return new SortStep(SortStepType.SWAP, i, j, 0);
    }

    public static SortStep overwrite(int index, int value) {
        return new SortStep(SortStepType.OVERWRITE, index, -1, value);
    }

    public static SortStep markSorted(int index) {
        return new SortStep(SortStepType.MARK_SORTED, index, -1, 0);
    }

    public static SortStep done() {
        return new SortStep(SortStepType.DONE, -1, -1, 0);
    }
}

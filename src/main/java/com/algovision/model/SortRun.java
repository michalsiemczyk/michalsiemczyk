package com.algovision.model;

import java.util.List;

/**
 * Wynik jednego przebiegu algorytmu sortujacego: nagrane kroki + czas obliczen.
 * Statystyki liczone sa leniwie przez Stream API.
 */
public record SortRun(List<SortStep> steps, long computeTimeMicros) {

    public long comparisons() {
        return steps.stream().filter(s -> s.type() == SortStepType.COMPARE).count();
    }

    public long writes() {
        return steps.stream()
                .filter(s -> s.type() == SortStepType.SWAP || s.type() == SortStepType.OVERWRITE)
                .count();
    }

    /** Operacje "znaczace" (porownania + zapisy) - miara uzywana w Race Mode. */
    public long operations() {
        return comparisons() + writes();
    }

    public double computeTimeMs() {
        return computeTimeMicros / 1000.0;
    }
}

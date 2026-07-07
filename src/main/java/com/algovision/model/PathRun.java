package com.algovision.model;

import java.util.List;

/** Wynik jednego przebiegu algorytmu pathfindingu: nagrane kroki + statystyki + czas. */
public record PathRun(List<GridStep> steps, PathResult result, long computeTimeMicros) {

    public double computeTimeMs() {
        return computeTimeMicros / 1000.0;
    }
}

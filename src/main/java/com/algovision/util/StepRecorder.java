package com.algovision.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generyczna kolejka krokow - wspolna dla sortowania ({@code StepRecorder<SortStep>})
 * i pathfindingu ({@code StepRecorder<GridStep>}). Algorytm emituje do niej kroki,
 * a UI odbiera gotowa, niemodyfikowalna liste do odtworzenia.
 */
public final class StepRecorder<T> implements Consumer<T> {

    private final List<T> steps = new ArrayList<>();

    @Override
    public void accept(T step) {
        steps.add(step);
    }

    /** Nagrane kroki jako lista tylko do odczytu. */
    public List<T> steps() {
        return Collections.unmodifiableList(steps);
    }

    public int size() {
        return steps.size();
    }
}

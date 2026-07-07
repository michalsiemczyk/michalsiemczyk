package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.function.Consumer;

/**
 * Wspolny interfejs strategii pathfindingu.
 * Implementacja czyta siatke (nie modyfikuje jej) i emituje kroki
 * ({@link GridStep}) opisujace kolejnosc odwiedzania pol oraz znaleziona sciezke.
 */
public interface PathfindingAlgorithm {

    /**
     * Szuka sciezki od startu do celu siatki.
     * Ostatnim wyemitowanym krokiem musi byc {@code GridStep.done()}.
     */
    PathResult solve(Grid grid, Consumer<GridStep> out);
}

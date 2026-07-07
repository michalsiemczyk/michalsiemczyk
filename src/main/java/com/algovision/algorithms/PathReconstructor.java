package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Wspolna rekonstrukcja sciezki z tablicy poprzednikow (wspoldzielona przez algorytmy). */
final class PathReconstructor {

    private PathReconstructor() {
    }

    /**
     * Odtwarza sciezke od celu do startu po tablicy poprzednikow (prev[id] = id poprzednika,
     * -1 = brak), emituje kroki PATH w kolejnosci od startu i liczy dlugosc oraz koszt.
     */
    static PathResult emitPath(Grid grid, int[] prev, int visitedCount, Consumer<GridStep> out) {
        int cols = grid.cols();
        List<Integer> path = new ArrayList<>();
        int cur = grid.goalRow() * cols + grid.goalCol();
        while (cur != -1) {
            path.add(cur);
            cur = prev[cur];
        }
        Collections.reverse(path);

        int cost = 0;
        for (int i = 1; i < path.size(); i++) { // koszt liczony bez pola startowego
            cost += grid.weightAt(path.get(i) / cols, path.get(i) % cols);
        }
        path.forEach(id -> out.accept(GridStep.path(id / cols, id % cols)));
        out.accept(GridStep.done());
        return new PathResult(true, visitedCount, path.size() - 1, cost);
    }
}

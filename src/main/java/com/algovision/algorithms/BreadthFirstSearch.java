package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * BFS - gwarantuje najkrotsza sciezke w sensie liczby krawedzi (ignoruje wagi).
 * Charakterystyczna "fala" rozchodzaca sie rownomiernie od startu.
 */
public final class BreadthFirstSearch implements PathfindingAlgorithm {

    @Override
    public PathResult solve(Grid grid, Consumer<GridStep> out) {
        int cols = grid.cols();
        int start = grid.startRow() * cols + grid.startCol();
        int goal = grid.goalRow() * cols + grid.goalCol();

        boolean[] seen = new boolean[grid.rows() * cols];
        int[] prev = new int[seen.length];
        Arrays.fill(prev, -1);

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        seen[start] = true;
        queue.add(start);
        int visited = 0;

        while (!queue.isEmpty()) {
            int id = queue.poll();
            visited++;
            out.accept(GridStep.visit(id / cols, id % cols));
            if (id == goal) {
                return PathReconstructor.emitPath(grid, prev, visited, out);
            }
            for (int[] nb : grid.neighbors(id / cols, id % cols)) {
                int nid = nb[0] * cols + nb[1];
                if (!seen[nid]) {
                    seen[nid] = true;
                    prev[nid] = id;
                    queue.add(nid);
                    out.accept(GridStep.frontier(nb[0], nb[1]));
                }
            }
        }
        out.accept(GridStep.done());
        return PathResult.notFound(visited);
    }
}

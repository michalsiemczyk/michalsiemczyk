package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * DFS (iteracyjny, na jawnym stosie). Znajduje JAKAS sciezke - bez gwarancji,
 * ze najkrotsza; w wizualizacji swietnie kontrastuje z "fala" BFS.
 */
public final class DepthFirstSearch implements PathfindingAlgorithm {

    @Override
    public PathResult solve(Grid grid, Consumer<GridStep> out) {
        int cols = grid.cols();
        int start = grid.startRow() * cols + grid.startCol();
        int goal = grid.goalRow() * cols + grid.goalCol();

        boolean[] visitedArr = new boolean[grid.rows() * cols];
        int[] prev = new int[visitedArr.length];
        Arrays.fill(prev, -1);

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        int visited = 0;

        while (!stack.isEmpty()) {
            int id = stack.pop();
            if (visitedArr[id]) {
                continue;
            }
            visitedArr[id] = true;
            visited++;
            out.accept(GridStep.visit(id / cols, id % cols));
            if (id == goal) {
                return PathReconstructor.emitPath(grid, prev, visited, out);
            }
            // odwracamy kolejnosc sasiadow, by DFS eksplorowal najpierw "gore"
            List<int[]> neighbors = grid.neighbors(id / cols, id % cols);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                int[] nb = neighbors.get(i);
                int nid = nb[0] * cols + nb[1];
                if (!visitedArr[nid]) {
                    prev[nid] = id;
                    stack.push(nid);
                    out.accept(GridStep.frontier(nb[0], nb[1]));
                }
            }
        }
        out.accept(GridStep.done());
        return PathResult.notFound(visited);
    }
}

package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * A* z heurystyka Manhattan. Poniewaz minimalna waga pola wynosi 1,
 * odleglosc Manhattan nigdy nie przeszacowuje kosztu (heurystyka dopuszczalna),
 * wiec A* zwraca sciezke optymalna kosztowo - tak jak Dijkstra, ale odwiedza
 * zwykle znacznie mniej wezlow.
 */
public final class AStarSearch implements PathfindingAlgorithm {

    @Override
    public PathResult solve(Grid grid, Consumer<GridStep> out) {
        int cols = grid.cols();
        int start = grid.startRow() * cols + grid.startCol();
        int goal = grid.goalRow() * cols + grid.goalCol();
        int n = grid.rows() * cols;

        int[] g = new int[n];
        int[] prev = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(g, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        g[start] = 0;

        // wpisy [id, f = g + h]; remis rozstrzygany mniejszym h (blizej celu)
        PriorityQueue<int[]> pq = new PriorityQueue<>(
                Comparator.<int[]>comparingInt(e -> e[1]).thenComparingInt(e -> e[2]));
        pq.add(new int[]{start, heuristic(grid, start), heuristic(grid, start)});
        int visited = 0;

        while (!pq.isEmpty()) {
            int id = pq.poll()[0];
            if (settled[id]) {
                continue;
            }
            settled[id] = true;
            visited++;
            out.accept(GridStep.visit(id / cols, id % cols));
            if (id == goal) {
                return PathReconstructor.emitPath(grid, prev, visited, out);
            }
            for (int[] nb : grid.neighbors(id / cols, id % cols)) {
                int nid = nb[0] * cols + nb[1];
                if (settled[nid]) {
                    continue;
                }
                int ng = g[id] + grid.weightAt(nb[0], nb[1]);
                if (ng < g[nid]) {
                    g[nid] = ng;
                    prev[nid] = id;
                    int h = heuristic(grid, nid);
                    pq.add(new int[]{nid, ng + h, h});
                    out.accept(GridStep.frontier(nb[0], nb[1]));
                }
            }
        }
        out.accept(GridStep.done());
        return PathResult.notFound(visited);
    }

    /** Odleglosc Manhattan od wezla do celu. */
    private int heuristic(Grid grid, int id) {
        int r = id / grid.cols();
        int c = id % grid.cols();
        return Math.abs(r - grid.goalRow()) + Math.abs(c - grid.goalCol());
    }
}

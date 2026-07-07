package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Algorytm Dijkstry - najtansza sciezka z uwzglednieniem wag pol.
 * Koszt wejscia na pole = waga tego pola (zwykle 1, "bagno" 5).
 */
public final class DijkstraSearch implements PathfindingAlgorithm {

    @Override
    public PathResult solve(Grid grid, Consumer<GridStep> out) {
        int cols = grid.cols();
        int start = grid.startRow() * cols + grid.startCol();
        int goal = grid.goalRow() * cols + grid.goalCol();
        int n = grid.rows() * cols;

        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        // wpisy [id, dist]; przestarzale wpisy odfiltrowuje flaga settled
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e[1]));
        pq.add(new int[]{start, 0});
        int visited = 0;

        while (!pq.isEmpty()) {
            int[] entry = pq.poll();
            int id = entry[0];
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
                int nd = dist[id] + grid.weightAt(nb[0], nb[1]);
                if (nd < dist[nid]) {
                    dist[nid] = nd;
                    prev[nid] = id;
                    pq.add(new int[]{nid, nd});
                    out.accept(GridStep.frontier(nb[0], nb[1]));
                }
            }
        }
        out.accept(GridStep.done());
        return PathResult.notFound(visited);
    }
}

package com.algovision.algorithms;

import java.util.function.Supplier;

/** Typy algorytmow pathfindingu - fabryka strategii dla UI. */
public enum PathAlgorithmType {
    BFS("BFS", BreadthFirstSearch::new),
    DFS("DFS", DepthFirstSearch::new),
    DIJKSTRA("Dijkstra", DijkstraSearch::new),
    ASTAR("A* (Manhattan)", AStarSearch::new);

    private final String displayName;
    private final Supplier<PathfindingAlgorithm> factory;

    PathAlgorithmType(String displayName, Supplier<PathfindingAlgorithm> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    public PathfindingAlgorithm create() {
        return factory.get();
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

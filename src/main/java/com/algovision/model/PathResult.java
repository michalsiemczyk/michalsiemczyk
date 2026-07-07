package com.algovision.model;

/**
 * Podsumowanie pracy algorytmu pathfindingu.
 *
 * @param found        czy cel zostal osiagniety
 * @param visitedCount ile wezlow zostalo ostatecznie odwiedzonych
 * @param pathLength   dlugosc sciezki w krawedziach (0 gdy brak sciezki)
 * @param pathCost     laczny koszt sciezki z uwzglednieniem wag (0 gdy brak sciezki)
 */
public record PathResult(boolean found, int visitedCount, int pathLength, int pathCost) {

    public static PathResult notFound(int visitedCount) {
        return new PathResult(false, visitedCount, 0, 0);
    }
}

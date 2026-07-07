package com.algovision.algorithms;

import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.GridStepType;
import com.algovision.model.PathResult;
import com.algovision.util.StepRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testy poprawnosci strategii pathfindingu na znanych siatkach. */
class PathfindingAlgorithmTest {

    @ParameterizedTest
    @EnumSource(PathAlgorithmType.class)
    @DisplayName("Otwarta siatka 5x5: kazdy algorytm znajduje sciezke")
    void findsPathOnOpenGrid(PathAlgorithmType type) {
        Grid grid = new Grid(5, 5);
        grid.setStart(0, 0);
        grid.setGoal(4, 4);

        PathResult result = type.create().solve(grid, step -> { });

        assertTrue(result.found(), type + " powinien znalezc sciezke");
        assertTrue(result.visitedCount() > 0);
        assertTrue(result.pathLength() >= 8, "sciezka nie moze byc krotsza niz Manhattan");
    }

    @ParameterizedTest
    @EnumSource(value = PathAlgorithmType.class, names = {"BFS", "DIJKSTRA", "ASTAR"})
    @DisplayName("Otwarta siatka 5x5: algorytmy optymalne daja sciezke dlugosci 8")
    void optimalAlgorithmsFindShortestPath(PathAlgorithmType type) {
        Grid grid = new Grid(5, 5);
        grid.setStart(0, 0);
        grid.setGoal(4, 4);

        PathResult result = type.create().solve(grid, step -> { });

        assertEquals(8, result.pathLength(),
                type + " powinien znalezc najkrotsza sciezke (Manhattan = 8)");
    }

    @ParameterizedTest
    @EnumSource(value = PathAlgorithmType.class, names = {"BFS", "DIJKSTRA", "ASTAR"})
    @DisplayName("Sciana wymusza obejscie - znana najkrotsza dlugosc 6")
    void findsShortestDetourAroundWall(PathAlgorithmType type) {
        // 3x3, sciany (0,1) i (1,1) - jedyna droga prowadzi dolem, 6 krawedzi
        Grid grid = new Grid(3, 3);
        grid.setStart(0, 0);
        grid.setGoal(0, 2);
        grid.setWall(0, 1, true);
        grid.setWall(1, 1, true);

        PathResult result = type.create().solve(grid, step -> { });

        assertTrue(result.found());
        assertEquals(6, result.pathLength());
    }

    @ParameterizedTest
    @EnumSource(value = PathAlgorithmType.class, names = {"DIJKSTRA", "ASTAR"})
    @DisplayName("Wagi: algorytmy wazone omijaja drogie pola (koszt 6 zamiast 31)")
    void weightedAlgorithmsAvoidExpensiveCells(PathAlgorithmType type) {
        // 3x5: prosta droga srodkiem kosztuje 31 (pola z waga 10),
        // objazd gornym wierszem tylko 6 - algorytm wazony musi wybrac objazd
        Grid grid = new Grid(3, 5);
        grid.setStart(1, 0);
        grid.setGoal(1, 4);
        grid.setWeight(1, 1, 10);
        grid.setWeight(1, 2, 10);
        grid.setWeight(1, 3, 10);

        PathResult result = type.create().solve(grid, step -> { });

        assertTrue(result.found());
        assertEquals(6, result.pathCost(), type + " powinien znalezc sciezke o koszcie 6");
        assertEquals(6, result.pathLength(), "objazd ma 6 krawedzi");
    }

    @ParameterizedTest
    @EnumSource(PathAlgorithmType.class)
    @DisplayName("Cel odciety scianami - brak sciezki")
    void reportsNotFoundWhenGoalUnreachable(PathAlgorithmType type) {
        Grid grid = new Grid(3, 3);
        grid.setStart(0, 0);
        grid.setGoal(2, 2);
        grid.setWall(0, 1, true);
        grid.setWall(1, 0, true);
        grid.setWall(1, 1, true);

        PathResult result = type.create().solve(grid, step -> { });

        assertFalse(result.found());
        assertEquals(0, result.pathLength());
    }

    @Test
    @DisplayName("A* odwiedza nie wiecej wezlow niz Dijkstra na tej samej siatce")
    void aStarVisitsNoMoreThanDijkstra() {
        Grid grid = new Grid(15, 25);
        grid.setStart(7, 2);
        grid.setGoal(7, 22);

        PathResult dijkstra = new DijkstraSearch().solve(grid.copy(), step -> { });
        PathResult aStar = new AStarSearch().solve(grid.copy(), step -> { });

        assertEquals(dijkstra.pathCost(), aStar.pathCost(), "oba musza byc optymalne");
        assertTrue(aStar.visitedCount() <= dijkstra.visitedCount(),
                "heurystyka Manhattan powinna ograniczac przeszukiwanie");
    }

    @ParameterizedTest
    @EnumSource(PathAlgorithmType.class)
    @DisplayName("Strumien krokow: PATH pojawia sie po znalezieniu, DONE zawsze na koncu")
    void emitsWellFormedStepStream(PathAlgorithmType type) {
        Grid grid = new Grid(5, 5);
        grid.setStart(0, 0);
        grid.setGoal(4, 4);

        StepRecorder<GridStep> recorder = new StepRecorder<>();
        PathResult result = type.create().solve(grid, recorder);
        List<GridStep> steps = recorder.steps();

        assertEquals(GridStepType.DONE, steps.get(steps.size() - 1).type());
        long pathSteps = steps.stream().filter(s -> s.type() == GridStepType.PATH).count();
        assertEquals(result.pathLength() + 1, pathSteps,
                "krokow PATH powinno byc tyle, ile pol sciezki (dlugosc + 1)");
    }
}

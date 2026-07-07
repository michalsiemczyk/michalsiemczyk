package com.algovision.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testy modelu siatki - edycja scian, wag oraz kopiowanie. */
class GridTest {

    @Test
    @DisplayName("Start i cel nie moga zostac zamalowane sciana")
    void startAndGoalCannotBecomeWalls() {
        Grid grid = new Grid(10, 10);
        grid.setWall(grid.startRow(), grid.startCol(), true);
        grid.setWall(grid.goalRow(), grid.goalCol(), true);

        assertFalse(grid.isWall(grid.startRow(), grid.startCol()));
        assertFalse(grid.isWall(grid.goalRow(), grid.goalCol()));
    }

    @Test
    @DisplayName("Kopia siatki jest gleboka - edycja kopii nie zmienia oryginalu")
    void copyIsDeep() {
        Grid grid = new Grid(10, 10);
        grid.setWall(3, 3, true);
        grid.setWeight(4, 4, Grid.HEAVY_WEIGHT);

        Grid copy = grid.copy();
        copy.setWall(3, 3, false);
        copy.setWeight(4, 4, 1);
        copy.setStart(0, 0);

        assertTrue(grid.isWall(3, 3));
        assertEquals(Grid.HEAVY_WEIGHT, grid.weightAt(4, 4));
        assertFalse(grid.isStart(0, 0));
    }

    @Test
    @DisplayName("Sasiedzi nie wychodza poza siatke i omijaja sciany")
    void neighborsRespectBoundsAndWalls() {
        Grid grid = new Grid(5, 5);
        assertEquals(2, grid.neighbors(0, 0).size());

        grid.setWall(0, 1, true);
        assertEquals(1, grid.neighbors(0, 0).size());
    }

    @Test
    @DisplayName("Losowe sciany nigdy nie zakrywaja startu ani celu")
    void randomWallsKeepStartAndGoalFree() {
        Grid grid = new Grid(25, 40);
        grid.randomizeWalls(0.9, new Random(7));

        assertFalse(grid.isWall(grid.startRow(), grid.startCol()));
        assertFalse(grid.isWall(grid.goalRow(), grid.goalCol()));
    }
}

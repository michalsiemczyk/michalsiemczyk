package com.algovision.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Model siatki pathfindingu: sciany, wagi terenu, start i cel.
 * Czysty model (bez JavaFX), dzieki czemu algorytmy da sie testowac jednostkowo.
 */
public final class Grid {

    public static final int DEFAULT_ROWS = 25;
    public static final int DEFAULT_COLS = 40;
    /** Koszt wejscia na pole "ciezkie" (bagno). Zwykle pole ma koszt 1. */
    public static final int HEAVY_WEIGHT = 5;

    private final int rows;
    private final int cols;
    private final boolean[][] walls;
    private final int[][] weights;
    private int startRow;
    private int startCol;
    private int goalRow;
    private int goalCol;

    public Grid(int rows, int cols) {
        if (rows < 2 || cols < 2) {
            throw new IllegalArgumentException("Siatka musi miec co najmniej 2x2 pola");
        }
        this.rows = rows;
        this.cols = cols;
        this.walls = new boolean[rows][cols];
        this.weights = new int[rows][cols];
        for (int[] row : weights) {
            java.util.Arrays.fill(row, 1);
        }
        this.startRow = rows / 2;
        this.startCol = cols / 8;
        this.goalRow = rows / 2;
        this.goalCol = cols - 1 - cols / 8;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public boolean isWall(int r, int c) {
        return walls[r][c];
    }

    public void setWall(int r, int c, boolean wall) {
        if (isStart(r, c) || isGoal(r, c)) {
            return; // start i cel nigdy nie moga byc sciana
        }
        walls[r][c] = wall;
    }

    public int weightAt(int r, int c) {
        return weights[r][c];
    }

    public void setWeight(int r, int c, int weight) {
        if (weight < 1) {
            throw new IllegalArgumentException("Waga musi byc >= 1");
        }
        weights[r][c] = weight;
    }

    public boolean isStart(int r, int c) {
        return r == startRow && c == startCol;
    }

    public boolean isGoal(int r, int c) {
        return r == goalRow && c == goalCol;
    }

    public int startRow() {
        return startRow;
    }

    public int startCol() {
        return startCol;
    }

    public int goalRow() {
        return goalRow;
    }

    public int goalCol() {
        return goalCol;
    }

    public void setStart(int r, int c) {
        if (inBounds(r, c) && !walls[r][c] && !isGoal(r, c)) {
            startRow = r;
            startCol = c;
        }
    }

    public void setGoal(int r, int c) {
        if (inBounds(r, c) && !walls[r][c] && !isStart(r, c)) {
            goalRow = r;
            goalCol = c;
        }
    }

    /** Sasiedzi w stalej kolejnosci (gora, prawo, dol, lewo) - deterministyczne testy. */
    public List<int[]> neighbors(int r, int c) {
        List<int[]> result = new ArrayList<>(4);
        int[][] deltas = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        for (int[] d : deltas) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (inBounds(nr, nc) && !walls[nr][nc]) {
                result.add(new int[]{nr, nc});
            }
        }
        return result;
    }

    public void clearWalls() {
        for (boolean[] row : walls) {
            java.util.Arrays.fill(row, false);
        }
    }

    public void clearWeights() {
        for (int[] row : weights) {
            java.util.Arrays.fill(row, 1);
        }
    }

    /** Gleboka kopia - uzywana w Race Mode, by oba algorytmy dostaly identyczne dane. */
    public Grid copy() {
        Grid g = new Grid(rows, cols);
        for (int r = 0; r < rows; r++) {
            System.arraycopy(walls[r], 0, g.walls[r], 0, cols);
            System.arraycopy(weights[r], 0, g.weights[r], 0, cols);
        }
        g.startRow = startRow;
        g.startCol = startCol;
        g.goalRow = goalRow;
        g.goalCol = goalCol;
        return g;
    }

    /** Losowe sciany o zadanej gestosci; start i cel zawsze zostaja wolne. */
    public void randomizeWalls(double density, Random random) {
        clearWalls();
        IntStream.range(0, rows * cols)
                .filter(i -> random.nextDouble() < density)
                .forEach(i -> setWall(i / cols, i % cols, true));
        walls[startRow][startCol] = false;
        walls[goalRow][goalCol] = false;
    }
}

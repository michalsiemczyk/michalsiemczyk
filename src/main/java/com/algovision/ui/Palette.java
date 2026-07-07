package com.algovision.ui;

import javafx.scene.paint.Color;

/** Centralna paleta kolorow rysowanych na Canvas (reszta stylowania w style.css). */
public final class Palette {

    private Palette() {
    }

    // tla
    public static final Color CANVAS_BG = Color.web("#12141F");
    public static final Color GRID_LINE = Color.web("#FFFFFF", 0.05);

    // akcenty motywu
    public static final Color CYAN = Color.web("#22D3EE");
    public static final Color VIOLET = Color.web("#A78BFA");
    public static final Color GREEN = Color.web("#4ADE80");
    public static final Color AMBER = Color.web("#FBBF24");
    public static final Color RED = Color.web("#F87171");

    // slupki sortowania
    public static final Color BAR_NORMAL = Color.web("#525B85");
    public static final Color BAR_COMPARED = AMBER;
    public static final Color BAR_SWAPPED = RED;
    public static final Color BAR_SORTED = GREEN;

    // siatka pathfindingu
    public static final Color CELL_EMPTY = Color.web("#181B2A");
    public static final Color CELL_WALL = Color.web("#3E4568");
    public static final Color CELL_WEIGHT = Color.web("#FBBF24", 0.28);
    public static final Color CELL_FRONTIER = Color.web("#A78BFA", 0.55);
    public static final Color CELL_VISITED = Color.web("#22D3EE", 0.32);
    public static final Color CELL_PATH = GREEN;
    public static final Color CELL_START = CYAN;
    public static final Color CELL_GOAL = VIOLET;
}

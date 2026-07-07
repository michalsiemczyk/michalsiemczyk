package com.algovision.ui;

import com.algovision.model.CellState;
import com.algovision.model.Grid;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.util.function.BiConsumer;

/**
 * Rysowanie siatki pathfindingu + obsluga edycji myszka.
 * Nakladka animacji (odwiedzone/frontier/sciezka) jest oddzielona od bazowej
 * siatki (sciany/wagi), dzieki czemu reset animacji nie niszczy edycji.
 */
public final class GridCanvas extends Pane {

    private final Canvas canvas = new Canvas();
    private Grid grid;
    private CellState[][] overlay;
    private BiConsumer<Integer, Integer> onCellPressed = (r, c) -> { };
    private BiConsumer<Integer, Integer> onCellDragged = (r, c) -> { };

    public GridCanvas(Grid grid) {
        setGrid(grid);
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());

        canvas.setOnMousePressed(e -> fire(e.getX(), e.getY(), onCellPressed));
        canvas.setOnMouseDragged(e -> fire(e.getX(), e.getY(), onCellDragged));
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        clearOverlay();
    }

    public void setOnCellPressed(BiConsumer<Integer, Integer> handler) {
        this.onCellPressed = handler;
    }

    public void setOnCellDragged(BiConsumer<Integer, Integer> handler) {
        this.onCellDragged = handler;
    }

    public void setOverlay(int r, int c, CellState state) {
        overlay[r][c] = state;
    }

    public void clearOverlay() {
        overlay = new CellState[grid.rows()][grid.cols()];
        for (CellState[] row : overlay) {
            java.util.Arrays.fill(row, CellState.NONE);
        }
    }

    private void fire(double x, double y, BiConsumer<Integer, Integer> handler) {
        double cell = cellSize();
        if (cell <= 0) {
            return;
        }
        double ox = offsetX();
        double oy = offsetY();
        int c = (int) ((x - ox) / cell);
        int r = (int) ((y - oy) / cell);
        if (grid.inBounds(r, c)) {
            handler.accept(r, c);
        }
    }

    private double cellSize() {
        return Math.min(canvas.getWidth() / grid.cols(), canvas.getHeight() / grid.rows());
    }

    private double offsetX() {
        return (canvas.getWidth() - cellSize() * grid.cols()) / 2;
    }

    private double offsetY() {
        return (canvas.getHeight() - cellSize() * grid.rows()) / 2;
    }

    public void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        g.setFill(Palette.CANVAS_BG);
        g.fillRect(0, 0, w, h);

        double cell = cellSize();
        double ox = offsetX();
        double oy = offsetY();

        for (int r = 0; r < grid.rows(); r++) {
            for (int c = 0; c < grid.cols(); c++) {
                double x = ox + c * cell;
                double y = oy + r * cell;

                g.setFill(grid.isWall(r, c) ? Palette.CELL_WALL : Palette.CELL_EMPTY);
                g.fillRect(x, y, cell, cell);

                if (!grid.isWall(r, c) && grid.weightAt(r, c) > 1) {
                    g.setFill(Palette.CELL_WEIGHT);
                    g.fillRect(x, y, cell, cell);
                }

                switch (overlay[r][c]) {
                    case VISITED -> {
                        g.setFill(Palette.CELL_VISITED);
                        g.fillRect(x, y, cell, cell);
                    }
                    case FRONTIER -> {
                        g.setFill(Palette.CELL_FRONTIER);
                        g.fillRect(x, y, cell, cell);
                    }
                    case PATH -> {
                        g.setFill(Palette.CELL_PATH);
                        g.fillRoundRect(x + cell * 0.15, y + cell * 0.15,
                                cell * 0.7, cell * 0.7, cell * 0.3, cell * 0.3);
                    }
                    case NONE -> { }
                }

                if (grid.isStart(r, c)) {
                    g.setFill(Palette.CELL_START);
                    g.fillOval(x + cell * 0.12, y + cell * 0.12, cell * 0.76, cell * 0.76);
                } else if (grid.isGoal(r, c)) {
                    g.setFill(Palette.CELL_GOAL);
                    g.fillOval(x + cell * 0.12, y + cell * 0.12, cell * 0.76, cell * 0.76);
                }
            }
        }

        // subtelne linie siatki
        g.setStroke(Palette.GRID_LINE);
        g.setLineWidth(1);
        for (int c = 0; c <= grid.cols(); c++) {
            g.strokeLine(ox + c * cell, oy, ox + c * cell, oy + grid.rows() * cell);
        }
        for (int r = 0; r <= grid.rows(); r++) {
            g.strokeLine(ox, oy + r * cell, ox + grid.cols() * cell, oy + r * cell);
        }
    }
}

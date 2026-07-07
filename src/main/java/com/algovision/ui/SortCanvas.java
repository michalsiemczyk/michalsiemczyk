package com.algovision.ui;

import com.algovision.model.BarState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Rysowanie slupkow sortowania na Canvas.
 * Komponent jest "glupi": dostaje tablice wartosci + stany i tylko rysuje.
 */
public final class SortCanvas extends Pane {

    private final Canvas canvas = new Canvas();
    private int[] values = new int[0];
    private BarState[] states = new BarState[0];

    public SortCanvas() {
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());
    }

    /** Ustawia dane do narysowania (kopiowane defensywnie) i odswieza widok. */
    public void setData(int[] values, BarState[] states) {
        this.values = values.clone();
        this.states = states.clone();
        draw();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        g.setFill(Palette.CANVAS_BG);
        g.fillRect(0, 0, w, h);
        int n = values.length;
        if (n == 0) {
            return;
        }
        int max = Arrays.stream(values).max().orElse(1);
        double pad = 12;
        double barSlot = (w - 2 * pad) / n;
        double gap = Math.min(2, barSlot * 0.15);

        IntStream.range(0, n).forEach(i -> {
            double barH = Math.max(2, (values[i] / (double) max) * (h - 2 * pad));
            double x = pad + i * barSlot;
            double y = h - pad - barH;
            Color base = colorFor(states[i]);
            g.setFill(base);
            g.fillRoundRect(x, y, Math.max(1, barSlot - gap), barH, 3, 3);
            // delikatny "neonowy" rozblysk na szczycie slupka
            g.setFill(base.deriveColor(0, 1, 1.6, 0.9));
            g.fillRoundRect(x, y, Math.max(1, barSlot - gap), Math.min(4, barH), 3, 3);
        });
    }

    private Color colorFor(BarState state) {
        return switch (state) {
            case NORMAL -> Palette.BAR_NORMAL;
            case COMPARED -> Palette.BAR_COMPARED;
            case SWAPPED -> Palette.BAR_SWAPPED;
            case SORTED -> Palette.BAR_SORTED;
        };
    }
}

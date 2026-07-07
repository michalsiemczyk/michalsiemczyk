package com.algovision.model;

/**
 * Niemutowalny opis pojedynczego kroku pathfindingu (co narysowac na siatce).
 * Analogicznie do {@link SortStep} - UI odtwarza kroki, nie zna algorytmu.
 */
public record GridStep(GridStepType type, int row, int col) {

    public static GridStep visit(int row, int col) {
        return new GridStep(GridStepType.VISIT, row, col);
    }

    public static GridStep frontier(int row, int col) {
        return new GridStep(GridStepType.FRONTIER, row, col);
    }

    public static GridStep path(int row, int col) {
        return new GridStep(GridStepType.PATH, row, col);
    }

    public static GridStep done() {
        return new GridStep(GridStepType.DONE, -1, -1);
    }
}

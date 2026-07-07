package com.algovision.ui;

import com.algovision.algorithms.PathAlgorithmType;
import com.algovision.algorithms.PathfindingAlgorithm;
import com.algovision.model.CellState;
import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;
import com.algovision.model.PathRun;
import com.algovision.util.BackgroundCompute;
import com.algovision.util.StepRecorder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Random;

/**
 * Modul 2 - pathfinding na edytowalnej siatce.
 * LPM stawia/usuwa sciany (przeciaganie maluje), osobne tryby pozwalaja
 * malowac wagi oraz przestawiac start i cel.
 */
public final class PathView implements ModuleView {

    private enum EditMode { WALL, WEIGHT, START, GOAL }

    private final BorderPane root = new BorderPane();
    private final Grid grid = new Grid(Grid.DEFAULT_ROWS, Grid.DEFAULT_COLS);
    private final GridCanvas canvas = new GridCanvas(grid);
    private final StepPlayer<GridStep> player = new StepPlayer<>(this::applyStep);
    private final Random random = new Random();

    private final ComboBox<PathAlgorithmType> algorithmBox = new ComboBox<>();
    private final Button playButton = new Button("▶  Start");
    private final Button stepButton = new Button("Krok");
    private final Button resetButton = new Button("Reset");
    private final Button clearButton = new Button("Wyczysc");
    private final Button randomButton = new Button("Losowe");
    private final Slider speedSlider = new Slider(1, 100, 45);
    private final ToggleGroup editGroup = new ToggleGroup();
    private final Label visitedValue = new Label("0");
    private final Label lengthValue = new Label("—");
    private final Label costValue = new Label("—");
    private final Label timeValue = new Label("—");
    private final Label statusLabel = new Label("");

    private EditMode editMode = EditMode.WALL;
    private boolean paintWallValue = true;
    private int paintWeightValue = Grid.HEAVY_WEIGHT;
    private PathRun run;
    private boolean computing;
    private int visitedCount;
    private int pathCells;

    public PathView() {
        buildLayout();
        wireEvents();
        canvas.draw();
    }

    @Override
    public Node node() {
        return root;
    }

    @Override
    public void onHidden() {
        player.pause();
        updatePlayButton();
    }

    // ---------------------------------------------------------------- layout

    private void buildLayout() {
        Label heading = new Label("Pathfinding");
        heading.getStyleClass().add("view-title");
        Label subheading = new Label(
                "Edytowalna siatka " + Grid.DEFAULT_ROWS + "×" + Grid.DEFAULT_COLS
                        + " — rysuj sciany i wagi, obserwuj fale przeszukiwania");
        subheading.getStyleClass().add("view-subtitle");
        VBox header = new VBox(2, heading, subheading);
        header.setPadding(new Insets(0, 0, 16, 0));

        StackPane canvasCard = new StackPane(canvas);
        canvasCard.getStyleClass().add("card");
        canvasCard.setPadding(new Insets(10));

        VBox controls = buildControlPanel();
        BorderPane.setMargin(controls, new Insets(0, 0, 0, 16));

        root.setTop(header);
        root.setCenter(canvasCard);
        root.setRight(controls);
        root.setPadding(new Insets(24));
    }

    private VBox buildControlPanel() {
        algorithmBox.getItems().setAll(PathAlgorithmType.values());
        algorithmBox.getSelectionModel().select(PathAlgorithmType.BFS);
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        playButton.getStyleClass().add("btn-primary");
        playButton.setMaxWidth(Double.MAX_VALUE);
        for (Button b : List.of(stepButton, resetButton, clearButton, randomButton)) {
            b.getStyleClass().add("btn-ghost");
            b.setMaxWidth(Double.MAX_VALUE);
        }
        HBox stepReset = new HBox(8, stepButton, resetButton);
        HBox.setHgrow(stepButton, Priority.ALWAYS);
        HBox.setHgrow(resetButton, Priority.ALWAYS);
        HBox gridButtons = new HBox(8, clearButton, randomButton);
        HBox.setHgrow(clearButton, Priority.ALWAYS);
        HBox.setHgrow(randomButton, Priority.ALWAYS);

        GridPane modeGrid = new GridPane();
        modeGrid.setHgap(8);
        modeGrid.setVgap(8);
        modeGrid.add(modeToggle("Sciany", EditMode.WALL, true), 0, 0);
        modeGrid.add(modeToggle("Wagi ×" + Grid.HEAVY_WEIGHT, EditMode.WEIGHT, false), 1, 0);
        modeGrid.add(modeToggle("Start", EditMode.START, false), 0, 1);
        modeGrid.add(modeToggle("Cel", EditMode.GOAL, false), 1, 1);

        GridPane stats = new GridPane();
        stats.getStyleClass().add("stats-grid");
        stats.setHgap(12);
        stats.setVgap(8);
        stats.addRow(0, statLabel("Odwiedzone wezly"), styledValue(visitedValue));
        stats.addRow(1, statLabel("Dlugosc sciezki"), styledValue(lengthValue));
        stats.addRow(2, statLabel("Koszt (wagi)"), styledValue(costValue));
        stats.addRow(3, statLabel("Czas obliczen"), styledValue(timeValue));

        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        VBox panel = new VBox(14,
                caption("Algorytm"), algorithmBox,
                playButton, stepReset,
                separator(),
                caption("Tryb edycji"), modeGrid,
                gridButtons,
                separator(),
                caption("Predkosc"), speedSlider,
                separator(),
                caption("Statystyki"), stats,
                statusLabel);
        panel.getStyleClass().addAll("card", "control-panel");
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(280);
        return panel;
    }

    private ToggleButton modeToggle(String text, EditMode mode, boolean selected) {
        ToggleButton t = new ToggleButton(text);
        t.setToggleGroup(editGroup);
        t.setSelected(selected);
        t.setUserData(mode);
        t.getStyleClass().add("mode-toggle");
        t.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(t, Priority.ALWAYS);
        return t;
    }

    private Label caption(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("caption");
        return l;
    }

    private Label statLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stat-label");
        return l;
    }

    private Label styledValue(Label label) {
        label.getStyleClass().add("stat-value");
        return label;
    }

    private Region separator() {
        Region r = new Region();
        r.getStyleClass().add("panel-separator");
        return r;
    }

    // ---------------------------------------------------------------- events

    private void wireEvents() {
        player.setSpeed(speedSlider.getValue());
        player.setAfterBatch(this::refreshView);
        player.setOnFinished(this::onPlaybackFinished);

        playButton.setOnAction(e -> togglePlay());
        stepButton.setOnAction(e -> ensureRun(() -> {
            player.stepOnce();
            updatePlayButton();
        }));
        resetButton.setOnAction(e -> resetPlayback());
        clearButton.setOnAction(e -> {
            grid.clearWalls();
            grid.clearWeights();
            invalidateRun();
        });
        randomButton.setOnAction(e -> {
            grid.randomizeWalls(0.28, random);
            invalidateRun();
        });

        speedSlider.valueProperty().addListener((o, a, b) -> player.setSpeed(b.doubleValue()));
        algorithmBox.valueProperty().addListener((o, a, b) -> invalidateRun());
        editGroup.selectedToggleProperty().addListener((o, a, b) -> {
            // ToggleGroup pozwala odznaczyc wszystko - przywracamy poprzedni wybor
            if (b == null) {
                if (a != null) {
                    a.setSelected(true);
                }
            } else {
                editMode = (EditMode) b.getUserData();
            }
        });

        canvas.setOnCellPressed(this::onCellPressed);
        canvas.setOnCellDragged(this::onCellDragged);
    }

    private void onCellPressed(int r, int c) {
        invalidateRunKeepGrid();
        switch (editMode) {
            case WALL -> {
                paintWallValue = !grid.isWall(r, c);
                grid.setWall(r, c, paintWallValue);
            }
            case WEIGHT -> {
                paintWeightValue = grid.weightAt(r, c) > 1 ? 1 : Grid.HEAVY_WEIGHT;
                if (!grid.isWall(r, c)) {
                    grid.setWeight(r, c, paintWeightValue);
                }
            }
            case START -> grid.setStart(r, c);
            case GOAL -> grid.setGoal(r, c);
        }
        canvas.draw();
    }

    private void onCellDragged(int r, int c) {
        switch (editMode) {
            case WALL -> grid.setWall(r, c, paintWallValue);
            case WEIGHT -> {
                if (!grid.isWall(r, c)) {
                    grid.setWeight(r, c, paintWeightValue);
                }
            }
            case START -> grid.setStart(r, c);
            case GOAL -> grid.setGoal(r, c);
        }
        canvas.draw();
    }

    private void togglePlay() {
        if (player.isPlaying()) {
            player.pause();
            updatePlayButton();
        } else {
            if (player.status() == StepPlayer.Status.FINISHED) {
                resetPlayback();
            }
            ensureRun(() -> {
                player.play();
                updatePlayButton();
            });
        }
    }

    private void ensureRun(Runnable then) {
        if (run != null) {
            then.run();
            return;
        }
        if (computing) {
            return;
        }
        computing = true;
        Grid snapshot = grid.copy(); // algorytm liczy na kopii - edycja nie psuje obliczen
        PathfindingAlgorithm algorithm = algorithmBox.getValue().create();
        BackgroundCompute.run(() -> {
            StepRecorder<GridStep> recorder = new StepRecorder<>();
            long t0 = System.nanoTime();
            PathResult result = algorithm.solve(snapshot, recorder);
            long micros = (System.nanoTime() - t0) / 1_000;
            return new PathRun(recorder.steps(), result, micros);
        }, result -> {
            computing = false;
            run = result;
            player.load(run.steps());
            timeValue.setText(String.format("%.2f ms", run.computeTimeMs()));
            then.run();
        });
    }

    /** Uniewaznia obliczenia po zmianie siatki lub algorytmu. */
    private void invalidateRun() {
        invalidateRunKeepGrid();
        canvas.draw();
    }

    private void invalidateRunKeepGrid() {
        player.load(List.of());
        run = null;
        resetOverlayState();
        updatePlayButton();
        refreshStats();
    }

    private void resetPlayback() {
        player.reset();
        if (run != null) {
            player.load(run.steps());
        }
        resetOverlayState();
        canvas.draw();
        refreshStats();
        updatePlayButton();
    }

    private void resetOverlayState() {
        canvas.clearOverlay();
        visitedCount = 0;
        pathCells = 0;
        statusLabel.setText("");
        lengthValue.setText("—");
        costValue.setText("—");
    }

    // -------------------------------------------------------------- playback

    private void applyStep(GridStep step) {
        switch (step.type()) {
            case VISIT -> {
                canvas.setOverlay(step.row(), step.col(), CellState.VISITED);
                visitedCount++;
            }
            case FRONTIER -> canvas.setOverlay(step.row(), step.col(), CellState.FRONTIER);
            case PATH -> {
                canvas.setOverlay(step.row(), step.col(), CellState.PATH);
                pathCells++;
            }
            case DONE -> { }
        }
    }

    private void refreshView() {
        canvas.draw();
        refreshStats();
    }

    private void refreshStats() {
        visitedValue.setText(String.format("%,d", visitedCount));
        if (pathCells > 0) {
            lengthValue.setText(String.format("%,d", pathCells - 1));
        }
    }

    private void onPlaybackFinished() {
        updatePlayButton();
        if (run == null) {
            return;
        }
        PathResult result = run.result();
        if (result.found()) {
            lengthValue.setText(String.format("%,d", result.pathLength()));
            costValue.setText(String.format("%,d", result.pathCost()));
            statusLabel.setText("Sciezka znaleziona ✔");
        } else {
            statusLabel.setText("Brak sciezki do celu ✖");
        }
    }

    private void updatePlayButton() {
        playButton.setText(player.isPlaying() ? "⏸  Pauza" : "▶  Start");
    }
}

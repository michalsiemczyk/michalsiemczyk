package com.algovision.ui;

import com.algovision.algorithms.BreadthFirstSearch;
import com.algovision.algorithms.PathAlgorithmType;
import com.algovision.algorithms.SortAlgorithmType;
import com.algovision.model.BarState;
import com.algovision.model.CellState;
import com.algovision.model.Grid;
import com.algovision.model.GridStep;
import com.algovision.model.PathResult;
import com.algovision.model.PathRun;
import com.algovision.model.SortRun;
import com.algovision.model.SortStep;
import com.algovision.util.ArrayGenerator;
import com.algovision.util.BackgroundCompute;
import com.algovision.util.StepRecorder;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Modul 3 - Race Mode: dwa algorytmy scigaja sie na identycznych danych.
 * Oba tory odtwarzaja kroki z ta sama predkoscia, wiec wygrywa algorytm,
 * ktory potrzebuje mniej operacji. Baner oglasza zwyciezce.
 */
public final class RaceView implements ModuleView {

    private enum Category { SORT, PATH }

    /** Wspolny kontrakt toru wyscigu - niezalezny od rodzaju algorytmu. */
    private interface Lane {
        Node node();

        void compute(Runnable whenReady);

        void play();

        void pause();

        void setSpeed(double sliderValue);

        String algorithmName();

        long operations();

        double timeMs();

        String operationsUnit();
    }

    private final BorderPane root = new BorderPane();
    private final HBox lanesBox = new HBox(16);
    private final Label banner = new Label();
    private final HBox bannerBox = new HBox(banner);

    private final ToggleGroup categoryGroup = new ToggleGroup();
    private final ToggleButton sortToggle = new ToggleButton("Sortowanie");
    private final ToggleButton pathToggle = new ToggleButton("Pathfinding");
    private final ComboBox<Object> comboA = new ComboBox<>();
    private final ComboBox<Object> comboB = new ComboBox<>();
    private final Button startButton = new Button("🏁  Start wyscigu");
    private final Button resetButton = new Button("Reset");
    private final Button newDataButton = new Button("Nowe dane");
    private final Slider speedSlider = new Slider(1, 100, 45);

    private final Random random = new Random();
    private Category category = Category.SORT;
    private int[] sharedArray;
    private Grid sharedGrid;
    private Lane laneA;
    private Lane laneB;
    private Lane winner;
    private int finishedLanes;
    private boolean racing;

    public RaceView() {
        sharedArray = ArrayGenerator.random(80, random);
        sharedGrid = newSolvableGrid();
        buildLayout();
        wireEvents();
        rebuildLanes();
    }

    @Override
    public Node node() {
        return root;
    }

    @Override
    public void onHidden() {
        pauseLanes();
    }

    // ---------------------------------------------------------------- layout

    private void buildLayout() {
        Label heading = new Label("Race Mode");
        heading.getStyleClass().add("view-title");
        Label subheading = new Label("Dwa algorytmy, identyczne dane, jeden zwyciezca");
        subheading.getStyleClass().add("view-subtitle");
        VBox header = new VBox(2, heading, subheading);

        sortToggle.setToggleGroup(categoryGroup);
        pathToggle.setToggleGroup(categoryGroup);
        sortToggle.setSelected(true);
        sortToggle.getStyleClass().add("mode-toggle");
        pathToggle.getStyleClass().add("mode-toggle");

        startButton.getStyleClass().add("btn-primary");
        resetButton.getStyleClass().add("btn-ghost");
        newDataButton.getStyleClass().add("btn-ghost");

        Label vs = new Label("vs");
        vs.getStyleClass().add("vs-label");

        Label speedCaption = new Label("Predkosc");
        speedCaption.getStyleClass().add("caption");
        speedSlider.setPrefWidth(140);

        HBox configBar = new HBox(10,
                sortToggle, pathToggle,
                spacer(12),
                comboA, vs, comboB,
                spacer(12),
                startButton, resetButton, newDataButton,
                growingSpacer(),
                speedCaption, speedSlider);
        configBar.setAlignment(Pos.CENTER_LEFT);
        configBar.getStyleClass().addAll("card", "race-config");
        configBar.setPadding(new Insets(12, 16, 12, 16));

        VBox top = new VBox(16, header, configBar);
        top.setPadding(new Insets(0, 0, 16, 0));

        lanesBox.setAlignment(Pos.CENTER);

        banner.getStyleClass().add("race-banner");
        bannerBox.setAlignment(Pos.TOP_CENTER);
        bannerBox.setPadding(new Insets(24, 0, 0, 0));
        bannerBox.setMouseTransparent(true);
        bannerBox.setVisible(false);

        StackPane arena = new StackPane(lanesBox, bannerBox);
        root.setTop(top);
        root.setCenter(arena);
        root.setPadding(new Insets(24));
    }

    private Region spacer(double width) {
        Region r = new Region();
        r.setPrefWidth(width);
        return r;
    }

    private Region growingSpacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ---------------------------------------------------------------- events

    private void wireEvents() {
        categoryGroup.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) {
                if (a != null) {
                    a.setSelected(true);
                }
                return;
            }
            category = (b == sortToggle) ? Category.SORT : Category.PATH;
            populateCombos();
            rebuildLanes();
        });
        comboA.valueProperty().addListener((o, a, b) -> {
            if (b != null && !racing) {
                rebuildLanes();
            }
        });
        comboB.valueProperty().addListener((o, a, b) -> {
            if (b != null && !racing) {
                rebuildLanes();
            }
        });
        startButton.setOnAction(e -> startRace());
        resetButton.setOnAction(e -> rebuildLanes());
        newDataButton.setOnAction(e -> {
            sharedArray = ArrayGenerator.random(80, random);
            sharedGrid = newSolvableGrid();
            rebuildLanes();
        });
        speedSlider.valueProperty().addListener((o, a, b) -> {
            if (laneA != null) {
                laneA.setSpeed(b.doubleValue());
            }
            if (laneB != null) {
                laneB.setSpeed(b.doubleValue());
            }
        });
        populateCombos();
    }

    private void populateCombos() {
        Object[] items = category == Category.SORT
                ? SortAlgorithmType.values()
                : PathAlgorithmType.values();
        comboA.getItems().setAll(items);
        comboB.getItems().setAll(items);
        if (category == Category.SORT) {
            comboA.setValue(SortAlgorithmType.BUBBLE);
            comboB.setValue(SortAlgorithmType.QUICK);
        } else {
            comboA.setValue(PathAlgorithmType.BFS);
            comboB.setValue(PathAlgorithmType.ASTAR);
        }
    }

    /** Buduje oba tory od zera na aktualnych danych - takze pelni role resetu. */
    private void rebuildLanes() {
        pauseLanes();
        racing = false;
        winner = null;
        finishedLanes = 0;
        hideBanner();
        startButton.setDisable(false);

        if (category == Category.SORT) {
            laneA = new SortLane((SortAlgorithmType) comboA.getValue(), sharedArray);
            laneB = new SortLane((SortAlgorithmType) comboB.getValue(), sharedArray);
        } else {
            laneA = new PathLane((PathAlgorithmType) comboA.getValue(), sharedGrid.copy());
            laneB = new PathLane((PathAlgorithmType) comboB.getValue(), sharedGrid.copy());
        }
        laneA.setSpeed(speedSlider.getValue());
        laneB.setSpeed(speedSlider.getValue());
        lanesBox.getChildren().setAll(laneA.node(), laneB.node());
        HBox.setHgrow(laneA.node(), Priority.ALWAYS);
        HBox.setHgrow(laneB.node(), Priority.ALWAYS);
    }

    private void startRace() {
        if (racing) {
            return;
        }
        racing = true;
        startButton.setDisable(true);
        // najpierw oba tory licza kroki w tle, start dopiero gdy OBA sa gotowe
        final int[] pending = {2};
        Runnable readyLatch = () -> {
            if (--pending[0] == 0) {
                laneA.play();
                laneB.play();
            }
        };
        laneA.compute(readyLatch);
        laneB.compute(readyLatch);
    }

    private void onLaneFinished(Lane lane) {
        finishedLanes++;
        if (winner == null) {
            winner = lane;
            Lane loser = (lane == laneA) ? laneB : laneA;
            showBanner(String.format("🏆  %s wygrywa!  %,d %s vs %,d %s  ·  %.2f ms vs %.2f ms",
                    winner.algorithmName(),
                    winner.operations(), winner.operationsUnit(),
                    loser.operations(), loser.operationsUnit(),
                    winner.timeMs(), loser.timeMs()));
        }
        if (finishedLanes >= 2) {
            racing = false;
            startButton.setDisable(false);
        }
    }

    private void pauseLanes() {
        if (laneA != null) {
            laneA.pause();
        }
        if (laneB != null) {
            laneB.pause();
        }
    }

    private void showBanner(String text) {
        banner.setText(text);
        bannerBox.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(400), bannerBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), bannerBox);
        slide.setFromY(-30);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    private void hideBanner() {
        bannerBox.setVisible(false);
    }

    /** Losowa siatka, na ktorej na pewno istnieje sciezka start→cel. */
    private Grid newSolvableGrid() {
        Grid g = new Grid(Grid.DEFAULT_ROWS, Grid.DEFAULT_COLS);
        for (int attempt = 0; attempt < 100; attempt++) {
            g.randomizeWalls(0.3, random);
            if (new BreadthFirstSearch().solve(g, step -> { }).found()) {
                return g;
            }
        }
        g.clearWalls();
        return g;
    }

    // ------------------------------------------------------------ sort lane

    private final class SortLane implements Lane {

        private final SortAlgorithmType type;
        private final SortCanvas canvas = new SortCanvas();
        private final StepPlayer<SortStep> player = new StepPlayer<>(this::applyStep);
        private final Label statsLabel = new Label();
        private final VBox box;
        private final int[] values;
        private final BarState[] states;
        private final List<Integer> transientMarks = new ArrayList<>();
        private long comparisons;
        private long writes;
        private SortRun run;

        SortLane(SortAlgorithmType type, int[] sharedData) {
            this.type = type;
            this.values = sharedData.clone();
            this.states = new BarState[values.length];
            Arrays.fill(states, BarState.NORMAL);
            this.box = buildBox(type.displayName(), canvas, statsLabel);
            player.setAfterBatch(this::refresh);
            player.setOnFinished(() -> onLaneFinished(this));
            refresh();
        }

        @Override
        public Node node() {
            return box;
        }

        @Override
        public void compute(Runnable whenReady) {
            int[] input = values.clone();
            BackgroundCompute.run(() -> {
                StepRecorder<SortStep> recorder = new StepRecorder<>();
                long t0 = System.nanoTime();
                type.create().sort(input, recorder);
                long micros = (System.nanoTime() - t0) / 1_000;
                return new SortRun(recorder.steps(), micros);
            }, result -> {
                run = result;
                player.load(run.steps());
                whenReady.run();
            });
        }

        @Override
        public void play() {
            player.play();
        }

        @Override
        public void pause() {
            player.pause();
        }

        @Override
        public void setSpeed(double sliderValue) {
            player.setSpeed(sliderValue);
        }

        @Override
        public String algorithmName() {
            return type.displayName();
        }

        @Override
        public long operations() {
            return run == null ? 0 : run.operations();
        }

        @Override
        public double timeMs() {
            return run == null ? 0 : run.computeTimeMs();
        }

        @Override
        public String operationsUnit() {
            return "operacji";
        }

        private void applyStep(SortStep step) {
            transientMarks.forEach(i -> {
                if (states[i] != BarState.SORTED) {
                    states[i] = BarState.NORMAL;
                }
            });
            transientMarks.clear();
            switch (step.type()) {
                case COMPARE -> {
                    mark(step.indexA(), BarState.COMPARED);
                    mark(step.indexB(), BarState.COMPARED);
                    comparisons++;
                }
                case SWAP -> {
                    int tmp = values[step.indexA()];
                    values[step.indexA()] = values[step.indexB()];
                    values[step.indexB()] = tmp;
                    mark(step.indexA(), BarState.SWAPPED);
                    mark(step.indexB(), BarState.SWAPPED);
                    writes++;
                }
                case OVERWRITE -> {
                    values[step.indexA()] = step.value();
                    mark(step.indexA(), BarState.SWAPPED);
                    writes++;
                }
                case MARK_SORTED -> states[step.indexA()] = BarState.SORTED;
                case DONE -> Arrays.fill(states, BarState.SORTED);
            }
        }

        private void mark(int index, BarState state) {
            if (states[index] != BarState.SORTED) {
                states[index] = state;
                transientMarks.add(index);
            }
        }

        private void refresh() {
            canvas.setData(values, states);
            statsLabel.setText(String.format("Porownania: %,d  ·  Zapisy: %,d", comparisons, writes));
        }
    }

    // ------------------------------------------------------------ path lane

    private final class PathLane implements Lane {

        private final PathAlgorithmType type;
        private final Grid grid;
        private final GridCanvas canvas;
        private final StepPlayer<GridStep> player = new StepPlayer<>(this::applyStep);
        private final Label statsLabel = new Label();
        private final VBox box;
        private int visitedCount;
        private int pathCells;
        private PathRun run;

        PathLane(PathAlgorithmType type, Grid grid) {
            this.type = type;
            this.grid = grid;
            this.canvas = new GridCanvas(grid);
            this.box = buildBox(type.displayName(), canvas, statsLabel);
            player.setAfterBatch(this::refresh);
            player.setOnFinished(() -> onLaneFinished(this));
            refresh();
        }

        @Override
        public Node node() {
            return box;
        }

        @Override
        public void compute(Runnable whenReady) {
            Grid snapshot = grid.copy();
            BackgroundCompute.run(() -> {
                StepRecorder<GridStep> recorder = new StepRecorder<>();
                long t0 = System.nanoTime();
                PathResult result = type.create().solve(snapshot, recorder);
                long micros = (System.nanoTime() - t0) / 1_000;
                return new PathRun(recorder.steps(), result, micros);
            }, result -> {
                run = result;
                player.load(run.steps());
                whenReady.run();
            });
        }

        @Override
        public void play() {
            player.play();
        }

        @Override
        public void pause() {
            player.pause();
        }

        @Override
        public void setSpeed(double sliderValue) {
            player.setSpeed(sliderValue);
        }

        @Override
        public String algorithmName() {
            return type.displayName();
        }

        @Override
        public long operations() {
            return run == null ? 0 : run.result().visitedCount();
        }

        @Override
        public double timeMs() {
            return run == null ? 0 : run.computeTimeMs();
        }

        @Override
        public String operationsUnit() {
            return "wezlow";
        }

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

        private void refresh() {
            canvas.draw();
            String length = pathCells > 0 ? String.valueOf(pathCells - 1) : "—";
            statsLabel.setText(String.format("Odwiedzone: %,d  ·  Sciezka: %s", visitedCount, length));
        }
    }

    /** Wspolna ramka toru: naglowek z nazwa algorytmu + canvas w karcie. */
    private VBox buildBox(String name, Region canvas, Label statsLabel) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("lane-title");
        statsLabel.getStyleClass().add("lane-stats");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox headerRow = new HBox(8, nameLabel, grow, statsLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(canvas, Priority.ALWAYS);
        VBox box = new VBox(10, headerRow, canvas);
        box.getStyleClass().addAll("card", "lane-card");
        box.setPadding(new Insets(14));
        return box;
    }
}

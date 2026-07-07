package com.algovision.ui;

import com.algovision.algorithms.SortAlgorithmType;
import com.algovision.algorithms.SortingAlgorithm;
import com.algovision.model.BarState;
import com.algovision.model.SortRun;
import com.algovision.model.SortStep;
import com.algovision.util.ArrayGenerator;
import com.algovision.util.BackgroundCompute;
import com.algovision.util.StepRecorder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modul 1 - wizualizacja sortowania.
 * Kontroler w duchu MVC: model to nagrany {@link SortRun}, widok to
 * {@link SortCanvas}, a klasa spina je playbackiem {@link StepPlayer}.
 */
public final class SortView implements ModuleView {

    private final BorderPane root = new BorderPane();
    private final SortCanvas canvas = new SortCanvas();
    private final StepPlayer<SortStep> player = new StepPlayer<>(this::applyStep);

    private final ComboBox<SortAlgorithmType> algorithmBox = new ComboBox<>();
    private final Button playButton = new Button("▶  Start");
    private final Button stepButton = new Button("Krok");
    private final Button resetButton = new Button("Reset");
    private final Button newArrayButton = new Button("Nowa losowa tablica");
    private final Slider speedSlider = new Slider(1, 100, 40);
    private final Slider sizeSlider = new Slider(10, 200, 60);
    private final Label comparisonsValue = new Label("0");
    private final Label writesValue = new Label("0");
    private final Label timeValue = new Label("—");
    private final Label sizeValueLabel = new Label("60");

    private int[] initialArray = new int[0];
    private int[] values = new int[0];
    private BarState[] states = new BarState[0];
    private final List<Integer> transientMarks = new ArrayList<>();
    private long comparisons;
    private long writes;
    private SortRun run;
    private boolean computing;

    public SortView() {
        buildLayout();
        wireEvents();
        regenerateArray();
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
        Label heading = new Label("Sortowanie");
        heading.getStyleClass().add("view-title");
        Label subheading = new Label("Animowana wizualizacja szesciu klasycznych algorytmow");
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
        algorithmBox.getItems().setAll(SortAlgorithmType.values());
        algorithmBox.getSelectionModel().select(SortAlgorithmType.BUBBLE);
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        playButton.getStyleClass().add("btn-primary");
        playButton.setMaxWidth(Double.MAX_VALUE);
        stepButton.getStyleClass().add("btn-ghost");
        resetButton.getStyleClass().add("btn-ghost");
        newArrayButton.getStyleClass().add("btn-ghost");
        newArrayButton.setMaxWidth(Double.MAX_VALUE);
        HBox stepReset = new HBox(8, stepButton, resetButton);
        stepButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(stepButton, Priority.ALWAYS);
        HBox.setHgrow(resetButton, Priority.ALWAYS);

        Label speedCaption = caption("Predkosc");
        Label sizeCaption = caption("Liczba elementow");
        sizeValueLabel.getStyleClass().add("stat-value-small");
        HBox sizeRow = new HBox(8, sizeCaption, spacer(), sizeValueLabel);

        GridPane stats = new GridPane();
        stats.getStyleClass().add("stats-grid");
        stats.setHgap(12);
        stats.setVgap(8);
        stats.addRow(0, statLabel("Porownania"), styledValue(comparisonsValue));
        stats.addRow(1, statLabel("Zamiany / zapisy"), styledValue(writesValue));
        stats.addRow(2, statLabel("Czas obliczen"), styledValue(timeValue));

        VBox panel = new VBox(14,
                caption("Algorytm"), algorithmBox,
                playButton, stepReset, newArrayButton,
                separator(),
                speedCaption, speedSlider,
                sizeRow, sizeSlider,
                separator(),
                caption("Statystyki"), stats);
        panel.getStyleClass().addAll("card", "control-panel");
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(280);
        return panel;
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

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ---------------------------------------------------------------- events

    private void wireEvents() {
        player.setSpeed(speedSlider.getValue());
        player.setAfterBatch(this::refreshView);
        player.setOnFinished(this::updatePlayButton);

        playButton.setOnAction(e -> togglePlay());
        stepButton.setOnAction(e -> ensureRun(() -> {
            player.stepOnce();
            updatePlayButton();
        }));
        resetButton.setOnAction(e -> resetPlayback());
        newArrayButton.setOnAction(e -> regenerateArray());

        speedSlider.valueProperty().addListener((o, a, b) -> player.setSpeed(b.doubleValue()));
        sizeSlider.valueProperty().addListener((o, a, b) -> {
            sizeValueLabel.setText(String.valueOf(b.intValue()));
            if (a.intValue() != b.intValue()) {
                regenerateArray();
            }
        });
        algorithmBox.valueProperty().addListener((o, a, b) -> invalidateRun());
    }

    private void togglePlay() {
        if (player.isPlaying()) {
            player.pause();
            updatePlayButton();
        } else {
            if (player.status() == StepPlayer.Status.FINISHED) {
                resetPlayback(); // po zakonczonej animacji Start zaczyna od nowa
            }
            ensureRun(() -> {
                player.play();
                updatePlayButton();
            });
        }
    }

    /** Kroki liczone sa leniwie w tle; po ich dostarczeniu wykonujemy akcje. */
    private void ensureRun(Runnable then) {
        if (run != null) {
            then.run();
            return;
        }
        if (computing) {
            return;
        }
        computing = true;
        int[] input = initialArray.clone();
        SortingAlgorithm algorithm = algorithmBox.getValue().create();
        BackgroundCompute.run(() -> {
            StepRecorder<SortStep> recorder = new StepRecorder<>();
            long t0 = System.nanoTime();
            algorithm.sort(input, recorder);
            long micros = (System.nanoTime() - t0) / 1_000;
            return new SortRun(recorder.steps(), micros);
        }, result -> {
            computing = false;
            run = result;
            player.load(run.steps());
            timeValue.setText(String.format("%.2f ms", run.computeTimeMs()));
            then.run();
        });
    }

    private void invalidateRun() {
        player.load(List.of());
        run = null;
        resetDisplayState();
        refreshView();
        updatePlayButton();
    }

    private void resetPlayback() {
        player.reset();
        if (run != null) {
            player.load(run.steps());
        }
        resetDisplayState();
        refreshView();
        updatePlayButton();
    }

    private void regenerateArray() {
        initialArray = ArrayGenerator.random((int) sizeSlider.getValue());
        timeValue.setText("—");
        invalidateRun();
    }

    private void resetDisplayState() {
        values = initialArray.clone();
        states = new BarState[values.length];
        Arrays.fill(states, BarState.NORMAL);
        transientMarks.clear();
        comparisons = 0;
        writes = 0;
    }

    // -------------------------------------------------------------- playback

    /** Aplikuje jeden krok algorytmu do lokalnego stanu widoku. */
    private void applyStep(SortStep step) {
        clearTransientMarks();
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

    private void clearTransientMarks() {
        transientMarks.forEach(i -> {
            if (states[i] != BarState.SORTED) {
                states[i] = BarState.NORMAL;
            }
        });
        transientMarks.clear();
    }

    private void refreshView() {
        canvas.setData(values, states);
        comparisonsValue.setText(String.format("%,d", comparisons));
        writesValue.setText(String.format("%,d", writes));
    }

    private void updatePlayButton() {
        playButton.setText(player.isPlaying() ? "⏸  Pauza" : "▶  Start");
    }
}

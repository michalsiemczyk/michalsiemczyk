package com.algovision.ui;

import javafx.animation.AnimationTimer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generyczny odtwarzacz nagranych krokow - wspolny silnik playbacku dla
 * sortowania i pathfindingu. Dziala w calosci na watku JavaFX (AnimationTimer),
 * wiec pauza/reset/krok nie wymagaja zadnej synchronizacji i nie ma wyscigow
 * danych: obliczenia w tle jedynie DOSTARCZAJA niemutowalna liste krokow.
 */
public final class StepPlayer<T> {

    public enum Status { IDLE, PLAYING, PAUSED, FINISHED }

    private final Consumer<T> applier;
    private Runnable onFinished = () -> { };
    private Runnable afterBatch = () -> { };
    private List<T> steps = List.of();
    private int index;
    private double stepsPerSecond = 60;
    private double carry;
    private long lastNanos = -1;
    private Status status = Status.IDLE;

    /** Timer dawkuje kroki proporcjonalnie do uplywu czasu i ustawionej predkosci. */
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (lastNanos < 0) {
                lastNanos = now;
                return;
            }
            carry += (now - lastNanos) / 1_000_000_000.0 * stepsPerSecond;
            lastNanos = now;
            int todo = (int) carry;
            if (todo > 0) {
                carry -= todo;
                advance(todo);
            }
        }
    };

    public StepPlayer(Consumer<T> applier) {
        this.applier = applier;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * Callback wolany raz po zaaplikowaniu calej paczki krokow z danej klatki.
     * UI powinno tu odswiezyc canvas - rysowanie po kazdym kroku byloby
     * marnotrawstwem przy duzych predkosciach (tysiace krokow na sekunde).
     */
    public void setAfterBatch(Runnable afterBatch) {
        this.afterBatch = afterBatch;
    }

    /** Laduje nowa liste krokow i zatrzymuje odtwarzanie. */
    public void load(List<T> steps) {
        timer.stop();
        this.steps = steps;
        index = 0;
        status = Status.IDLE;
    }

    public void play() {
        if (steps.isEmpty() || status == Status.PLAYING || status == Status.FINISHED) {
            return;
        }
        status = Status.PLAYING;
        lastNanos = -1;
        carry = 0;
        timer.start();
    }

    public void pause() {
        if (status == Status.PLAYING) {
            timer.stop();
            status = Status.PAUSED;
        }
    }

    /** Krok do przodu: pauzuje odtwarzanie i aplikuje dokladnie jeden krok. */
    public void stepOnce() {
        if (status == Status.FINISHED) {
            return;
        }
        pause();
        advance(1);
    }

    public void reset() {
        timer.stop();
        index = 0;
        carry = 0;
        status = Status.IDLE;
    }

    /** Mapowanie suwaka 1-100 na kroki/sekunde (kwadratowo: ~1.25 do ~2500). */
    public void setSpeed(double sliderValue) {
        stepsPerSecond = 1 + sliderValue * sliderValue / 4.0;
    }

    public Status status() {
        return status;
    }

    public boolean isPlaying() {
        return status == Status.PLAYING;
    }

    public int stepIndex() {
        return index;
    }

    public int totalSteps() {
        return steps.size();
    }

    private void advance(int n) {
        for (int i = 0; i < n && index < steps.size(); i++) {
            applier.accept(steps.get(index++));
        }
        afterBatch.run();
        if (!steps.isEmpty() && index >= steps.size() && status != Status.FINISHED) {
            timer.stop();
            status = Status.FINISHED;
            onFinished.run();
        }
    }
}

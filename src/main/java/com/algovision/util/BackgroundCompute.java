package com.algovision.util;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Uruchamia obliczenia algorytmu na osobnym watku (javafx.concurrent.Task).
 * Wynik trafia do konsumenta juz na watku JavaFX (Task wewnetrznie korzysta
 * z Platform.runLater przy setOnSucceeded), wiec UI nigdy nie jest dotykane
 * z watku roboczego.
 */
public final class BackgroundCompute {

    private BackgroundCompute() {
    }

    public static <T> void run(Supplier<T> job, Consumer<T> onSuccessFx) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return job.get();
            }
        };
        task.setOnSucceeded(e -> onSuccessFx.accept(task.getValue()));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });
        Thread thread = new Thread(task, "algovision-compute");
        thread.setDaemon(true);
        thread.start();
    }
}

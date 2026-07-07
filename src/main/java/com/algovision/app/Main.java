package com.algovision.app;

import com.algovision.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/** Punkt wejscia JavaFX - buduje scene i laduje arkusz stylow. */
public final class Main extends Application {

    @Override
    public void start(Stage stage) {
        AppShell shell = new AppShell();
        Scene scene = new Scene(shell.getRoot(), 1280, 800);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/com/algovision/ui/style.css")).toExternalForm());
        stage.setTitle("AlgoVision Studio");
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

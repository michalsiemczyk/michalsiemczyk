package com.algovision.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Glowna powloka aplikacji: sidebar z modulami po lewej, tresc po srodku.
 * Moduly tworzone sa leniwie i przelaczane z plynnym przejsciem fade.
 */
public final class AppShell {

    private enum Module { SORT, PATH, RACE }

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private final Map<Module, ModuleView> views = new EnumMap<>(Module.class);
    private final Map<Module, Supplier<ModuleView>> factories = new EnumMap<>(Module.class);
    private ModuleView current;

    public AppShell() {
        factories.put(Module.SORT, SortView::new);
        factories.put(Module.PATH, PathView::new);
        factories.put(Module.RACE, RaceView::new);

        root.getStyleClass().add("app-root");
        root.setLeft(buildSidebar());
        root.setCenter(content);
        switchTo(Module.SORT);
    }

    public Parent getRoot() {
        return root;
    }

    private VBox buildSidebar() {
        Label logoAlgo = new Label("Algo");
        logoAlgo.getStyleClass().add("logo-algo");
        Label logoVision = new Label("Vision");
        logoVision.getStyleClass().add("logo-vision");
        HBox logo = new HBox(logoAlgo, logoVision);
        logo.setAlignment(Pos.CENTER_LEFT);
        Label logoSub = new Label("STUDIO");
        logoSub.getStyleClass().add("logo-sub");
        VBox logoBox = new VBox(0, logo, logoSub);
        logoBox.setPadding(new Insets(4, 8, 24, 8));

        ToggleGroup group = new ToggleGroup();
        ToggleButton sortBtn = navButton(group, "Sortowanie",
                "M3 21h4v-9H3v9zm7 0h4V3h-4v18zm7 0h4v-13h-4v13z");
        ToggleButton pathBtn = navButton(group, "Pathfinding",
                "M4 4h6v6H4V4zm10 10h6v6h-6v-6zm-6.5-1.5h3v-1h-3v1zm5-5h1v3h-1v-3zm-1 8.5h1v3h-1v-3zm5.5-8.5h3v-1h-3v1z"
                        + "M13 6.5a1.5 1.5 0 1 1 3 0 1.5 1.5 0 0 1-3 0zM6 16a1.5 1.5 0 1 1 3 0 1.5 1.5 0 0 1-3 0z");
        ToggleButton raceBtn = navButton(group, "Race Mode",
                "M5 2v20h2v-8h10.5l-2.2-4 2.2-4H7V2H5zm4 4h2v2H9V6zm4 0h2v2h-2V6zm-2 2h2v2h-2V8zm-2 2h2v2H9v-2zm4 0h2v2h-2v-2z");
        sortBtn.setSelected(true);

        sortBtn.setOnAction(e -> switchTo(Module.SORT));
        pathBtn.setOnAction(e -> switchTo(Module.PATH));
        raceBtn.setOnAction(e -> switchTo(Module.RACE));
        // klik w aktywna zakladke nie moze jej odznaczyc
        group.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null && a != null) {
                a.setSelected(true);
            }
        });

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        Label footer = new Label("Java 21 · JavaFX · offline");
        footer.getStyleClass().add("sidebar-footer");

        VBox sidebar = new VBox(6, logoBox, sortBtn, pathBtn, raceBtn, grow, footer);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setPrefWidth(210);
        return sidebar;
    }

    private ToggleButton navButton(ToggleGroup group, String text, String svg) {
        SVGPath icon = new SVGPath();
        icon.setContent(svg);
        icon.getStyleClass().add("nav-icon");
        ToggleButton button = new ToggleButton(text, icon);
        button.setToggleGroup(group);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphicTextGap(12);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void switchTo(Module module) {
        ModuleView next = views.computeIfAbsent(module, m -> factories.get(m).get());
        if (current == next) {
            return;
        }
        if (current != null) {
            current.onHidden();
        }
        Node node = next.node();
        node.setOpacity(0);
        content.getChildren().setAll(node);
        FadeTransition fade = new FadeTransition(Duration.millis(280), node);
        fade.setToValue(1);
        fade.play();
        current = next;
    }
}

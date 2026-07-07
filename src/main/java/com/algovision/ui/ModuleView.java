package com.algovision.ui;

import javafx.scene.Node;

/** Wspolny kontrakt modulow aplikacji (sortowanie / pathfinding / race). */
public interface ModuleView {

    Node node();

    /** Wolane przy przelaczeniu na inny modul - modul powinien spauzowac animacje. */
    default void onHidden() {
    }
}

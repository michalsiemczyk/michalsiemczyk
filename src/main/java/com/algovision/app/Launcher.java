package com.algovision.app;

/**
 * Posrednia klasa startowa (nie dziedziczy po Application), dzieki czemu
 * aplikacje mozna uruchamiac takze z classpath bez konfiguracji modulow JPMS.
 */
public final class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}

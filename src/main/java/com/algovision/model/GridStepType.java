package com.algovision.model;

/** Rodzaj kroku emitowanego przez algorytm szukajacy sciezki. */
public enum GridStepType {
    /** Wezel zostal zdjety z kolejki / ostatecznie odwiedzony. */
    VISIT,
    /** Wezel zostal dodany do kolejki (frontier - czolo fali). */
    FRONTIER,
    /** Wezel nalezy do znalezionej najkrotszej sciezki. */
    PATH,
    /** Koniec pracy algorytmu. */
    DONE
}

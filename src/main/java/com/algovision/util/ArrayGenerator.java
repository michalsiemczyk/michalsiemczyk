package com.algovision.util;

import java.util.Random;
import java.util.stream.IntStream;

/** Generowanie losowych tablic do sortowania (Stream API + lambdy). */
public final class ArrayGenerator {

    public static final int MIN_VALUE = 5;
    public static final int MAX_VALUE = 1000;

    private ArrayGenerator() {
    }

    /** Losowa tablica o zadanym rozmiarze, wartosci w [MIN_VALUE, MAX_VALUE]. */
    public static int[] random(int size, Random random) {
        return IntStream.generate(() -> MIN_VALUE + random.nextInt(MAX_VALUE - MIN_VALUE + 1))
                .limit(size)
                .toArray();
    }

    public static int[] random(int size) {
        return random(size, new Random());
    }
}

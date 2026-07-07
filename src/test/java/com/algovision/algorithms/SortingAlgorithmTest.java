package com.algovision.algorithms;

import com.algovision.model.SortStep;
import com.algovision.model.SortStepType;
import com.algovision.util.StepRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy poprawnosci wszystkich strategii sortowania.
 * Sprawdzamy dwie rzeczy:
 *  1) tablica po przebiegu algorytmu jest posortowana,
 *  2) ODTWORZENIE wyemitowanych krokow (SWAP/OVERWRITE) na niezaleznej kopii
 *     daje identyczny wynik - czyli kroki wiernie opisuja prace algorytmu
 *     (to na nich opiera sie caly playback w UI).
 */
class SortingAlgorithmTest {

    @ParameterizedTest
    @EnumSource(SortAlgorithmType.class)
    @DisplayName("Losowe tablice roznych rozmiarow sa poprawnie sortowane")
    void sortsRandomArrays(SortAlgorithmType type) {
        Random random = new Random(42);
        for (int size : new int[]{1, 2, 3, 10, 50, 200}) {
            int[] input = random.ints(size, 0, 1000).toArray();
            verifySort(type, input);
        }
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithmType.class)
    @DisplayName("Przypadki brzegowe: pusta, posortowana, odwrocona, duplikaty")
    void sortsEdgeCases(SortAlgorithmType type) {
        verifySort(type, new int[]{});
        verifySort(type, new int[]{7});
        verifySort(type, new int[]{1, 2, 3, 4, 5, 6});
        verifySort(type, new int[]{6, 5, 4, 3, 2, 1});
        verifySort(type, new int[]{5, 5, 5, 5});
        verifySort(type, new int[]{2, 1, 2, 1, 2, 1, 3, 3});
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithmType.class)
    @DisplayName("Strumien krokow konczy sie krokiem DONE")
    void emitsDoneAsLastStep(SortAlgorithmType type) {
        StepRecorder<SortStep> recorder = new StepRecorder<>();
        type.create().sort(new int[]{3, 1, 2}, recorder);
        List<SortStep> steps = recorder.steps();
        assertTrue(steps.size() > 0, "algorytm powinien wyemitowac kroki");
        assertEquals(SortStepType.DONE, steps.get(steps.size() - 1).type());
    }

    private void verifySort(SortAlgorithmType type, int[] input) {
        int[] working = input.clone();
        int[] expected = input.clone();
        Arrays.sort(expected);

        StepRecorder<SortStep> recorder = new StepRecorder<>();
        type.create().sort(working, recorder);

        assertArrayEquals(expected, working,
                type + ": tablica po sortowaniu powinna byc posortowana");

        // odtworzenie krokow na swiezej kopii musi dac ten sam efekt
        int[] replayed = replay(input, recorder.steps());
        assertArrayEquals(expected, replayed,
                type + ": odtworzenie krokow powinno posortowac tablice");
    }

    /** Symulacja playbacku UI: aplikuje wylacznie kroki mutujace. */
    private int[] replay(int[] input, List<SortStep> steps) {
        int[] a = input.clone();
        steps.forEach(step -> {
            switch (step.type()) {
                case SWAP -> {
                    int tmp = a[step.indexA()];
                    a[step.indexA()] = a[step.indexB()];
                    a[step.indexB()] = tmp;
                }
                case OVERWRITE -> a[step.indexA()] = step.value();
                default -> { }
            }
        });
        return a;
    }
}

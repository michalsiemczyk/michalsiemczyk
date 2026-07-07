# AlgoVision Studio

Interaktywne studio wizualizacji algorytmów napisane w **Java 21 + JavaFX 21**.
Dwa moduły — **Sortowanie** i **Pathfinding** — oraz wyróżniający tryb
**Race Mode**, w którym dwa algorytmy ścigają się obok siebie na identycznych
danych. Aplikacja działa w pełni offline, bez żadnych zależności sieciowych.

## Uruchomienie

Wymagania: **JDK 21** i **Maven 3.8+** (JavaFX pobierze się automatycznie z Maven Central).

```bash
mvn clean javafx:run
```

Budowa i testy:

```bash
mvn clean compile   # kompilacja
mvn test            # testy JUnit 5
```

## Funkcje

### Moduł 1 — Sortowanie
- 6 algorytmów: **Bubble, Insertion, Selection, Quick, Merge, Heap sort**
  (każdy jako osobna strategia za interfejsem `SortingAlgorithm`)
- animowane słupki o wysokości proporcjonalnej do wartości
- kolory stanów: neutralny / porównywany (żółty) / zamieniany (czerwony) / posortowany (zielony)
- kontrolki: play, pauza, krok-do-przodu, reset, suwak prędkości (1–100),
  suwak liczby elementów (10–200), nowa losowa tablica
- statystyki na żywo: porównania, zamiany/zapisy, czas obliczeń (ms)

### Moduł 2 — Pathfinding
- 4 algorytmy: **BFS, DFS, Dijkstra, A\*** (heurystyka Manhattan)
  za wspólnym interfejsem `PathfindingAlgorithm`
- siatka **25×40** edytowalna myszą: LPM stawia/usuwa ściany, przeciąganie
  maluje; osobne tryby: **Ściany / Wagi ×5 / Start / Cel**
- animowana „fala" odwiedzanych pól (odwiedzone + frontier), na końcu
  podświetlona najkrótsza ścieżka
- statystyki: odwiedzone węzły, długość ścieżki, koszt (wagi), czas

### Race Mode ⚡
- ekran podzielony na dwa tory; wybierasz algorytm A i B
  (osobno dla kategorii Sortowanie i Pathfinding)
- oba algorytmy startują na **identycznych danych** i animują się równocześnie
  z tą samą prędkością odtwarzania kroków
- baner ogłasza zwycięzcę wraz z liczbą operacji i czasem obliczeń obu stron

## Architektura

Najważniejsza zasada: **algorytmy nie znają UI**. Każdy algorytm emituje
strumień niemutowalnych kroków (`record SortStep`, `record GridStep`)
opisujących co się wydarzyło (porównanie, zamiana, odwiedzenie pola…).
UI odtwarza te kroki we własnym tempie.

- **Wzorzec Strategia** — enumy `SortAlgorithmType` / `PathAlgorithmType`
  pełnią rolę fabryk strategii; UI operuje wyłącznie na interfejsach.
- **Wątki** — obliczenia algorytmu biegną na osobnym wątku
  (`javafx.concurrent.Task` w `BackgroundCompute`), a wynik (niemutowalna
  lista kroków) trafia do UI na wątku JavaFX. Playback (`StepPlayer`,
  `AnimationTimer`) działa w całości na wątku FX, więc pauza / krok / reset
  nie wymagają synchronizacji i nie ma wyścigów danych.
- **Generyki** — `StepRecorder<T>` (wspólna kolejka kroków) i `StepPlayer<T>`
  (wspólny silnik odtwarzania) obsługują oba moduły.
- **Stream API + lambdy** — generowanie tablic (`ArrayGenerator`),
  statystyki (`SortRun.comparisons()`), losowanie ścian siatki.
- **MVC** — model (`com.algovision.model`) i algorytmy
  (`com.algovision.algorithms`) są czystą Javą (testowalną bez JavaFX);
  widoki w `com.algovision.ui` tylko rysują i przekazują zdarzenia.
- **Styling** — całość w `src/main/resources/com/algovision/ui/style.css`:
  ciemny motyw glassmorphism (tło `#0F111A`, panele `#1A1D2E`,
  akcenty `#22D3EE` / `#A78BFA`), płynne przejścia fade między modułami.

## Struktura projektu

```
algovision-studio/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/algovision/
│   │   │   ├── app/            # Main (Application), Launcher
│   │   │   ├── model/          # SortStep, GridStep, Grid, enumy stanów,
│   │   │   │                   # SortRun/PathRun/PathResult (rekordy)
│   │   │   ├── algorithms/     # SortingAlgorithm + 6 implementacji,
│   │   │   │                   # PathfindingAlgorithm + BFS/DFS/Dijkstra/A*,
│   │   │   │                   # enumy-fabryki typów algorytmów
│   │   │   ├── ui/             # AppShell, SortView, PathView, RaceView,
│   │   │   │                   # SortCanvas, GridCanvas, StepPlayer, Palette
│   │   │   └── util/           # StepRecorder<T>, ArrayGenerator,
│   │   │                       # BackgroundCompute (Task)
│   │   └── resources/com/algovision/ui/
│   │       └── style.css       # cały styling aplikacji
│   └── test/java/com/algovision/
│       ├── algorithms/         # testy sortowania (replay kroków)
│       │                       # i pathfindingu (znane siatki)
│       └── model/              # testy modelu siatki
└── README.md
```

## Testy

- **Sortowanie** — dla każdego algorytmu: losowe tablice różnych rozmiarów
  i przypadki brzegowe (pusta, posortowana, odwrócona, duplikaty); dodatkowo
  weryfikacja, że *odtworzenie wyemitowanych kroków* na niezależnej kopii
  daje posortowaną tablicę (dokładnie to robi UI).
- **Pathfinding** — znane siatki: najkrótsza ścieżka przy wymuszonym obejściu
  ściany, omijanie drogich pól przez Dijkstrę/A\*, brak ścieżki przy odciętym
  celu, A\* odwiedzający nie więcej węzłów niż Dijkstra.

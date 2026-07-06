package pauza;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Model danych aplikacji + trwały zapis do prostego pliku tekstowego
 * w katalogu domowym użytkownika (~/.pauza/data.txt). Bez zewnętrznych bibliotek.
 */
public class DataStore {

    public static final String[] CATEGORIES = {
            "Ubrania", "Elektronika", "Jedzenie", "Rozrywka", "Dom", "Uroda", "Hobby", "Inne"
    };

    public enum WishStatus { WAITING, BOUGHT, RESIGNED }

    /** Pokusa zakupowa czekająca w "poczekalni" na przemyślaną decyzję. */
    public static class Wish {
        public final String id;
        public String name = "";
        public String note = "";
        public double price;
        public long createdAt;
        public WishStatus status = WishStatus.WAITING;
        public long decidedAt;
        public boolean impulsiveBuy;

        Wish(String id) { this.id = id; }
    }

    /** Pojedynczy zapisany wydatek. */
    public static class Expense {
        public final String id;
        public String name = "";
        public String category = "Inne";
        public double amount;
        public boolean impulsive;
        public LocalDate date = LocalDate.now();

        Expense(String id) { this.id = id; }
    }

    private final Path file;
    private final List<Wish> wishes = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    private double monthlyBudget = 0;
    private String goalName = "";
    private double goalAmount = 0;
    private long firstUse = System.currentTimeMillis();
    private int coolingHours = 48;

    public DataStore(Path file) { this.file = file; }

    /* ------------------------------------------------------------------ */
    /* Wczytywanie i zapis                                                  */
    /* ------------------------------------------------------------------ */

    public static DataStore loadDefault() {
        return load(Path.of(System.getProperty("user.home"), ".pauza", "data.txt"));
    }

    public static DataStore load(Path file) {
        DataStore s = new DataStore(file);
        if (Files.exists(file)) {
            try {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    try {
                        s.parseLine(line);
                    } catch (RuntimeException bad) {
                        System.err.println("Pomijam uszkodzony wiersz danych: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    private void parseLine(String line) {
        String[] p = line.split("\\|", -1);
        switch (p[0]) {
            case "SET" -> {
                switch (p[1]) {
                    case "budget"       -> monthlyBudget = Double.parseDouble(p[2]);
                    case "goalName"     -> goalName = dec(p[2]);
                    case "goalAmount"   -> goalAmount = Double.parseDouble(p[2]);
                    case "firstUse"     -> firstUse = Long.parseLong(p[2]);
                    case "coolingHours" -> coolingHours = Integer.parseInt(p[2]);
                }
            }
            case "WISH" -> {
                Wish w = new Wish(p[1]);
                w.name = dec(p[2]);
                w.price = Double.parseDouble(p[3]);
                w.note = dec(p[4]);
                w.createdAt = Long.parseLong(p[5]);
                w.status = WishStatus.valueOf(p[6]);
                w.decidedAt = Long.parseLong(p[7]);
                w.impulsiveBuy = Boolean.parseBoolean(p[8]);
                wishes.add(w);
            }
            case "EXP" -> {
                Expense e = new Expense(p[1]);
                e.name = dec(p[2]);
                e.amount = Double.parseDouble(p[3]);
                e.category = dec(p[4]);
                e.impulsive = Boolean.parseBoolean(p[5]);
                e.date = LocalDate.ofEpochDay(Long.parseLong(p[6]));
                expenses.add(e);
            }
            default -> { /* nagłówek pliku albo nieznany typ — ignorujemy */ }
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> out = new ArrayList<>();
            out.add("PAUZA-V1");
            out.add("SET|budget|" + monthlyBudget);
            out.add("SET|goalName|" + enc(goalName));
            out.add("SET|goalAmount|" + goalAmount);
            out.add("SET|firstUse|" + firstUse);
            out.add("SET|coolingHours|" + coolingHours);
            for (Wish w : wishes) {
                out.add("WISH|" + w.id + "|" + enc(w.name) + "|" + w.price + "|" + enc(w.note)
                        + "|" + w.createdAt + "|" + w.status + "|" + w.decidedAt + "|" + w.impulsiveBuy);
            }
            for (Expense e : expenses) {
                out.add("EXP|" + e.id + "|" + enc(e.name) + "|" + e.amount + "|" + enc(e.category)
                        + "|" + e.impulsive + "|" + e.date.toEpochDay());
            }
            Files.write(file, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String enc(String s) { return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8); }
    private static String dec(String s) { return URLDecoder.decode(s, StandardCharsets.UTF_8); }

    /* ------------------------------------------------------------------ */
    /* Nasłuchiwanie zmian                                                  */
    /* ------------------------------------------------------------------ */

    public void addListener(Runnable r) { listeners.add(r); }

    private void changed() {
        save();
        for (Runnable r : listeners) r.run();
    }

    /* ------------------------------------------------------------------ */
    /* Poczekalnia (pokusy)                                                 */
    /* ------------------------------------------------------------------ */

    public Wish addWish(String name, double price, String note) {
        Wish w = new Wish(UUID.randomUUID().toString());
        w.name = name;
        w.price = price;
        w.note = note == null ? "" : note;
        w.createdAt = System.currentTimeMillis();
        wishes.add(w);
        changed();
        return w;
    }

    /** Ile milisekund zostało do końca okresu ochłonięcia (może być ujemne). */
    public long remainingMs(Wish w) {
        return w.createdAt + coolingHours * 3_600_000L - System.currentTimeMillis();
    }

    public boolean isReady(Wish w) { return remainingMs(w) <= 0; }

    /** Rezygnacja z zakupu — cena trafia do "zaoszczędzonych". */
    public void resign(Wish w) {
        w.status = WishStatus.RESIGNED;
        w.decidedAt = System.currentTimeMillis();
        changed();
    }

    /** Zakup pokusy — zapisuje też wydatek (impulsywny, gdy przed końcem odliczania). */
    public void buy(Wish w, boolean impulsive) {
        w.status = WishStatus.BOUGHT;
        w.impulsiveBuy = impulsive;
        w.decidedAt = System.currentTimeMillis();
        Expense e = new Expense(UUID.randomUUID().toString());
        e.name = w.name;
        e.amount = w.price;
        e.category = "Poczekalnia";
        e.impulsive = impulsive;
        e.date = LocalDate.now();
        expenses.add(e);
        changed();
    }

    public void deleteWish(Wish w) {
        wishes.remove(w);
        changed();
    }

    public List<Wish> waitingWishes() {
        List<Wish> out = new ArrayList<>();
        for (Wish w : wishes) if (w.status == WishStatus.WAITING) out.add(w);
        out.sort(Comparator.comparingLong(w -> w.createdAt));
        return out;
    }

    public List<Wish> decidedWishes() {
        List<Wish> out = new ArrayList<>();
        for (Wish w : wishes) if (w.status != WishStatus.WAITING) out.add(w);
        out.sort(Comparator.comparingLong((Wish w) -> w.decidedAt).reversed());
        return out;
    }

    /** Suma cen wszystkich pokus, z których użytkownik zrezygnował. */
    public double totalSaved() {
        double sum = 0;
        for (Wish w : wishes) if (w.status == WishStatus.RESIGNED) sum += w.price;
        return sum;
    }

    /* ------------------------------------------------------------------ */
    /* Wydatki                                                              */
    /* ------------------------------------------------------------------ */

    public void addExpense(String name, double amount, String category, boolean impulsive, LocalDate date) {
        Expense e = new Expense(UUID.randomUUID().toString());
        e.name = name;
        e.amount = amount;
        e.category = category;
        e.impulsive = impulsive;
        e.date = date;
        expenses.add(e);
        changed();
    }

    public void deleteExpense(Expense e) {
        expenses.remove(e);
        changed();
    }

    public List<Expense> expensesNewestFirst() {
        List<Expense> out = new ArrayList<>(expenses);
        out.sort(Comparator.comparing((Expense e) -> e.date).reversed());
        return out;
    }

    public double monthSpent(YearMonth month) {
        double sum = 0;
        for (Expense e : expenses) if (YearMonth.from(e.date).equals(month)) sum += e.amount;
        return sum;
    }

    public double monthSpent(YearMonth month, boolean impulsive) {
        double sum = 0;
        for (Expense e : expenses)
            if (e.impulsive == impulsive && YearMonth.from(e.date).equals(month)) sum += e.amount;
        return sum;
    }

    public int monthImpulsiveCount(YearMonth month) {
        int n = 0;
        for (Expense e : expenses) if (e.impulsive && YearMonth.from(e.date).equals(month)) n++;
        return n;
    }

    public double yearSpent(int year, boolean impulsive) {
        double sum = 0;
        for (Expense e : expenses)
            if (e.impulsive == impulsive && e.date.getYear() == year) sum += e.amount;
        return sum;
    }

    /** Liczba pełnych dni od ostatniego impulsywnego wydatku (albo od pierwszego użycia aplikacji). */
    public long streakDays() {
        LocalDate last = null;
        for (Expense e : expenses)
            if (e.impulsive && (last == null || e.date.isAfter(last))) last = e.date;
        LocalDate from = last != null
                ? last
                : Instant.ofEpochMilli(firstUse).atZone(ZoneId.systemDefault()).toLocalDate();
        return Math.max(0, ChronoUnit.DAYS.between(from, LocalDate.now()));
    }

    /* ------------------------------------------------------------------ */
    /* Ustawienia                                                           */
    /* ------------------------------------------------------------------ */

    public double monthlyBudget() { return monthlyBudget; }
    public String goalName()      { return goalName; }
    public double goalAmount()    { return goalAmount; }
    public int coolingHours()     { return coolingHours; }

    public void setMonthlyBudget(double v) { monthlyBudget = v; changed(); }

    public void setGoal(String name, double amount) {
        goalName = name;
        goalAmount = amount;
        changed();
    }
}

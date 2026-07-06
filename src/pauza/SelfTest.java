package pauza;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Szybki test logiki danych uruchamiany bez okna: {@code java pauza.Main --selftest}.
 * Sprawdza zapis, odczyt i wyliczenia na świeżym pliku tymczasowym.
 */
final class SelfTest {

    private SelfTest() {}

    static boolean run() {
        try {
            Path file = Files.createTempDirectory("pauza-test").resolve("data.txt");
            DataStore s = new DataStore(file);

            // Poczekalnia
            DataStore.Wish w1 = s.addWish("Słuchawki | bezprzewodowe", 349.99, "notatka, z przecinkiem");
            DataStore.Wish w2 = s.addWish("Kurtka", 520, "");
            DataStore.Wish w3 = s.addWish("Gra", 199.50, "promocja");
            check(s.waitingWishes().size() == 3, "3 pokusy czekają");
            check(!s.isReady(w1), "świeża pokusa jeszcze nie jest gotowa");
            check(s.remainingMs(w1) > 47L * 3_600_000, "odliczanie zaczyna się od ~48 h");

            s.resign(w1);
            s.buy(w2, true);
            check(s.waitingWishes().size() == 1, "1 pokusa nadal czeka");
            check(s.decidedWishes().size() == 2, "2 decyzje w historii");
            check(Math.abs(s.totalSaved() - 349.99) < 1e-9, "oszczędności = cena odpuszczonej pokusy");

            // Zakup z poczekalni tworzy wydatek
            check(s.expensesNewestFirst().size() == 1, "zakup pokusy zapisany jako wydatek");
            check(s.expensesNewestFirst().get(0).impulsive, "zakup przed czasem jest impulsywny");
            check(s.streakDays() == 0, "impulsywny zakup zeruje serię");

            // Wydatki i budżet
            s.addExpense("Bilet do kina", 35, "Rozrywka", false, LocalDate.now());
            s.addExpense("Stara bluza", 120, "Ubrania", true, LocalDate.now().minusMonths(2));
            s.setMonthlyBudget(1500);
            YearMonth now = YearMonth.now();
            check(Math.abs(s.monthSpent(now) - 555.0) < 1e-9, "suma wydatków w tym miesiącu");
            check(Math.abs(s.monthSpent(now, true) - 520.0) < 1e-9, "suma impulsywnych w tym miesiącu");
            check(s.monthImpulsiveCount(now) == 1, "licznik impulsywnych w tym miesiącu");

            // Cel
            s.setGoal("Wakacje nad morzem", 3000);

            // Zapis i ponowny odczyt — wszystko musi się zgadzać
            DataStore r = DataStore.load(file);
            check(r.waitingWishes().size() == 1, "po odczycie: 1 pokusa czeka");
            check(r.waitingWishes().get(0).name.equals("Gra"), "po odczycie: nazwa pokusy");
            check(r.decidedWishes().size() == 2, "po odczycie: 2 decyzje");
            check(Math.abs(r.totalSaved() - 349.99) < 1e-9, "po odczycie: oszczędności");
            check(r.expensesNewestFirst().size() == 3, "po odczycie: 3 wydatki");
            check(Math.abs(r.monthlyBudget() - 1500) < 1e-9, "po odczycie: budżet");
            check(r.goalName().equals("Wakacje nad morzem"), "po odczycie: nazwa celu");
            check(Math.abs(r.goalAmount() - 3000) < 1e-9, "po odczycie: kwota celu");
            for (DataStore.Wish w : r.decidedWishes()) {
                if (w.status == DataStore.WishStatus.RESIGNED) {
                    check(w.name.equals("Słuchawki | bezprzewodowe"), "po odczycie: znaki specjalne w nazwie");
                    check(w.note.equals("notatka, z przecinkiem"), "po odczycie: znaki specjalne w notatce");
                }
            }

            // Formatowanie odliczania
            check(CoolingPanel.formatRemaining(0).equals("00:00:00"), "format 0 ms");
            check(CoolingPanel.formatRemaining(-5000).equals("00:00:00"), "format ujemny nie wybucha");
            check(CoolingPanel.formatRemaining(90_061_000L).equals("1 d 01:01:01"), "format z dniami");

            // Parsowanie kwot
            check(Ui.parseAmount("149,99") == 149.99, "kwota z przecinkiem");
            check(Ui.parseAmount("1 200") == 1200.0, "kwota ze spacją");
            check(Ui.parseAmount("abc") == null, "śmieci odrzucone");
            check(Ui.parseAmount("-5") == null, "ujemna odrzucona");

            System.out.println("SELFTEST OK — wszystkie " + checks + " sprawdzeń zaliczonych.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int checks = 0;

    private static void check(boolean condition, String what) {
        checks++;
        if (!condition) throw new AssertionError("Nie powiodło się: " + what);
    }
}

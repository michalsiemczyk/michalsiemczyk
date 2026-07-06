package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/** Ekran startowy: najważniejsze liczby, cytat dnia i szybkie akcje. */
public class DashboardPanel extends JPanel implements AppFrame.View {

    private static final String[] QUOTES = {
            "Impuls mija. Oszczędności zostają.",
            "Za każdym razem, gdy odpuszczasz zbędny zakup, płacisz samemu sobie.",
            "Wyprzedaż to nie okazja, jeśli niczego nie potrzebujesz.",
            "48 godzin czekania kosztuje mniej niż nietrafiony zakup.",
            "Szczęścia nie znajdziesz w koszyku.",
            "Reklamy znają Twoje słabości. Ty znasz swoje cele.",
            "Twoje przyszłe ja dziękuje Ci za każdą odpuszczoną pokusę.",
            "Nie jesteś tym, co kupujesz.",
            "Minimalizm to nie brak — to wolność.",
            "Mały krok dziś to wielka zmiana za rok.",
            "Nie kupuj rzeczy — kupuj czas i spokój.",
            "Portfel pełen, głowa lekka.",
    };

    private final DataStore store;
    private final JLabel greeting = Ui.label("", Theme.bold(26), Theme.TEXT);
    private final JLabel dateLabel = Ui.subtitle("");
    private final JLabel streakValue = Ui.label("", Theme.bold(26), Theme.MINT);
    private final JLabel savedValue = Ui.label("", Theme.bold(26), Theme.ACCENT);
    private final JLabel waitingValue = Ui.label("", Theme.bold(26), Theme.AMBER);
    private final JLabel budgetValue = Ui.label("", Theme.bold(26), Theme.TEXT);
    private final JLabel budgetSub = Ui.label("", Theme.regular(12), Theme.MUTED);
    private final JLabel quoteLabel = new JLabel();

    public DashboardPanel(DataStore store, AppFrame frame) {
        this.store = store;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel header = Ui.column();
        greeting.setAlignmentX(0f);
        dateLabel.setAlignmentX(0f);
        header.add(greeting);
        header.add(Box.createVerticalStrut(3));
        header.add(dateLabel);
        header.add(Box.createVerticalStrut(20));
        add(header, BorderLayout.NORTH);

        JPanel body = Ui.column();

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 14));
        stats.setOpaque(false);
        stats.setAlignmentX(0f);
        stats.add(statCard("Dni bez impulsów", streakValue,
                Ui.label("Nie przerywaj serii!", Theme.regular(12), Theme.MUTED)));
        stats.add(statCard("Zaoszczędzone", savedValue,
                Ui.label("Z odpuszczonych pokus", Theme.regular(12), Theme.MUTED)));
        stats.add(statCard("W poczekalni", waitingValue,
                Ui.label("Czekają na decyzję", Theme.regular(12), Theme.MUTED)));
        stats.add(statCard("Wydatki — miesiąc", budgetValue, budgetSub));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, stats.getPreferredSize().height));
        body.add(stats);
        body.add(Box.createVerticalStrut(16));

        Ui.GradientCard quoteCard = new Ui.GradientCard();
        quoteCard.setLayout(new BorderLayout());
        quoteCard.setAlignmentX(0f);
        JLabel quoteTitle = Ui.label("MYŚL NA DZIŚ", Theme.bold(11), new Color(255, 255, 255, 190));
        quoteLabel.setFont(Theme.font(Font.ITALIC | Font.BOLD, 18));
        quoteLabel.setForeground(Color.WHITE);
        quoteCard.add(quoteTitle, BorderLayout.NORTH);
        quoteCard.add(Box.createVerticalStrut(8), BorderLayout.WEST);
        quoteCard.add(quoteLabel, BorderLayout.CENTER);
        quoteCard.setPreferredSize(new Dimension(100, 110));
        quoteCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        body.add(quoteCard);
        body.add(Box.createVerticalStrut(16));

        Ui.Card actions = new Ui.Card();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setAlignmentX(0f);
        JLabel actionsTitle = Ui.section("Co chcesz teraz zrobić?");
        actionsTitle.setAlignmentX(0f);
        actions.add(actionsTitle);
        actions.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(0f);
        Ui.PillButton toCooling = new Ui.PillButton("Mam ochotę coś kupić…", Theme.ACCENT, Color.WHITE);
        toCooling.addActionListener(e -> frame.show("cooling"));
        Ui.PillButton toSos = new Ui.PillButton("SOS — silna pokusa!", Theme.RED_SOFT, Theme.RED);
        toSos.addActionListener(e -> frame.show("sos"));
        Ui.PillButton toExpense = new Ui.PillButton("Zapisz wydatek", Theme.MINT_SOFT, Theme.MINT);
        toExpense.addActionListener(e -> frame.show("expenses"));
        buttons.add(toCooling);
        buttons.add(toSos);
        buttons.add(toExpense);
        actions.add(buttons);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, actions.getPreferredSize().height));
        body.add(actions);
        body.add(Box.createVerticalGlue());

        add(body, BorderLayout.CENTER);
        refreshView();
    }

    private JPanel statCard(String title, JLabel value, JLabel sub) {
        Ui.Card card = new Ui.Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = Ui.label(title, Theme.bold(12), Theme.MUTED);
        t.setAlignmentX(0f);
        value.setAlignmentX(0f);
        sub.setAlignmentX(0f);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(value);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        return card;
    }

    @Override public void refreshView() {
        int hour = LocalTime.now().getHour();
        String hello = hour < 5 ? "Dobranoc" : hour < 12 ? "Dzień dobry!" : hour < 18 ? "Miłego dnia!" : "Dobry wieczór!";
        greeting.setText(hello);
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Theme.PL)));

        long days = store.streakDays();
        streakValue.setText(days == 1 ? "1 dzień" : days + " dni");
        savedValue.setText(Theme.money(store.totalSaved()));
        waitingValue.setText(String.valueOf(store.waitingWishes().size()));

        double spent = store.monthSpent(YearMonth.now());
        double budget = store.monthlyBudget();
        budgetValue.setText(Theme.money(spent));
        if (budget > 0) {
            double left = budget - spent;
            budgetValue.setForeground(spent > budget ? Theme.RED : Theme.TEXT);
            budgetSub.setText(left >= 0
                    ? "Zostało " + Theme.money(left)
                    : "Przekroczono o " + Theme.money(-left));
        } else {
            budgetValue.setForeground(Theme.TEXT);
            budgetSub.setText("Budżet ustawisz w Wydatkach");
        }

        String quote = QUOTES[(int) (LocalDate.now().toEpochDay() % QUOTES.length)];
        quoteLabel.setText("<html>„" + quote + "”</html>");
    }
}

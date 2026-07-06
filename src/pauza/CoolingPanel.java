package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * "Poczekalnia" — serce aplikacji. Każdą pokusę zapisujesz tutaj
 * i czekasz 48 godzin, zanim podejmiesz decyzję o zakupie.
 */
public class CoolingPanel extends JPanel implements AppFrame.View {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Theme.PL);

    private final DataStore store;
    private final JTextField nameField = Ui.textField(16);
    private final JTextField priceField = Ui.textField(7);
    private final JTextField noteField = Ui.textField(14);
    private final JLabel formError = Ui.label(" ", Theme.bold(12), Theme.RED);
    private final JPanel listColumn = Ui.column();
    private final List<WishRow> rows = new ArrayList<>();

    public CoolingPanel(DataStore store) {
        this.store = store;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel top = Ui.column();
        JLabel title = Ui.title("Poczekalnia");
        title.setAlignmentX(0f);
        JLabel sub = Ui.subtitle("Masz na coś ochotę? Zapisz to i odczekaj " + store.coolingHours()
                + " godzin. Większość pokus mija sama — a Ty zatrzymujesz pieniądze.");
        sub.setAlignmentX(0f);
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);
        top.add(Box.createVerticalStrut(16));

        Ui.Card form = new Ui.Card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(0f);
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fields.setOpaque(false);
        fields.setAlignmentX(0f);
        fields.add(Ui.field("CO CHCESZ KUPIĆ?", nameField));
        fields.add(Ui.field("CENA (ZŁ)", priceField));
        fields.add(Ui.field("NOTATKA (OPCJONALNIE)", noteField));
        Ui.PillButton addBtn = new Ui.PillButton("Do poczekalni", Theme.ACCENT, Color.WHITE);
        addBtn.addActionListener(e -> addWish());
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setBorder(new EmptyBorder(19, 0, 0, 0));
        btnWrap.add(addBtn);
        fields.add(btnWrap);
        form.add(fields);
        formError.setAlignmentX(0f);
        form.add(Box.createVerticalStrut(4));
        form.add(formError);
        top.add(form);
        top.add(Box.createVerticalStrut(16));
        add(top, BorderLayout.NORTH);

        add(Ui.scroll(listColumn), BorderLayout.CENTER);

        // Odświeżanie odliczania co sekundę.
        new Timer(1000, e -> {
            for (WishRow row : rows) row.tick();
        }).start();

        refreshView();
    }

    private void addWish() {
        String name = nameField.getText().trim();
        Double price = Ui.parseAmount(priceField.getText());
        if (name.isEmpty()) {
            formError.setText("Wpisz, co chcesz kupić.");
            return;
        }
        if (price == null) {
            formError.setText("Podaj poprawną cenę, np. 149,99.");
            return;
        }
        formError.setText(" ");
        store.addWish(name, price, noteField.getText().trim());
        nameField.setText("");
        priceField.setText("");
        noteField.setText("");
    }

    @Override public void refreshView() {
        listColumn.removeAll();
        rows.clear();

        List<DataStore.Wish> waiting = store.waitingWishes();
        if (waiting.isEmpty()) {
            Ui.Card empty = new Ui.Card();
            empty.setLayout(new BorderLayout());
            empty.setAlignmentX(0f);
            JLabel l = Ui.label("Poczekalnia jest pusta — to dobry znak. Trzymaj tak dalej!",
                    Theme.regular(14), Theme.MUTED);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            empty.add(l);
            listColumn.add(empty);
        } else {
            for (DataStore.Wish w : waiting) {
                WishRow row = new WishRow(w);
                rows.add(row);
                listColumn.add(row);
                listColumn.add(Box.createVerticalStrut(10));
            }
        }

        List<DataStore.Wish> decided = store.decidedWishes();
        if (!decided.isEmpty()) {
            listColumn.add(Box.createVerticalStrut(14));
            JLabel h = Ui.section("Historia decyzji");
            h.setAlignmentX(0f);
            listColumn.add(h);
            listColumn.add(Box.createVerticalStrut(10));
            int limit = Math.min(decided.size(), 10);
            for (int i = 0; i < limit; i++) {
                listColumn.add(decidedRow(decided.get(i)));
                listColumn.add(Box.createVerticalStrut(8));
            }
        }

        listColumn.revalidate();
        listColumn.repaint();
    }

    private JComponent decidedRow(DataStore.Wish w) {
        Ui.Card card = new Ui.Card(14);
        card.setBorder(new EmptyBorder(12, 18, 12, 18));
        card.setLayout(new BorderLayout(12, 0));
        card.setAlignmentX(0f);

        JPanel left = Ui.column();
        JLabel name = Ui.label(w.name, Theme.bold(14), Theme.TEXT);
        name.setAlignmentX(0f);
        JLabel when = Ui.label(Instant.ofEpochMilli(w.decidedAt).atZone(ZoneId.systemDefault()).format(DATE_FMT),
                Theme.regular(11), Theme.MUTED);
        when.setAlignmentX(0f);
        left.add(name);
        left.add(when);
        card.add(left, BorderLayout.CENTER);

        Ui.Chip chip;
        if (w.status == DataStore.WishStatus.RESIGNED) {
            chip = new Ui.Chip("Odpuszczone  +" + Theme.money(w.price), Theme.MINT_SOFT, Theme.MINT);
        } else if (w.impulsiveBuy) {
            chip = new Ui.Chip("Kupione impulsywnie  " + Theme.money(w.price), Theme.RED_SOFT, Theme.RED);
        } else {
            chip = new Ui.Chip("Kupione po namyśle  " + Theme.money(w.price), Theme.ACCENT_SOFT, Theme.ACCENT);
        }
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(chip);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    /** Wiersz oczekującej pokusy z żywym odliczaniem i przyciskami decyzji. */
    private class WishRow extends Ui.Card {
        private final DataStore.Wish wish;
        private final Ui.Chip countdown = new Ui.Chip("", Theme.AMBER_SOFT, Theme.AMBER);
        private final Ui.PillButton buyBtn = new Ui.PillButton("", Theme.AMBER_SOFT, Theme.AMBER, 14, 7);
        private boolean lastReady;

        WishRow(DataStore.Wish wish) {
            this.wish = wish;
            setAlignmentX(0f);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;

            JPanel texts = Ui.column();
            JLabel name = Ui.label(wish.name, Theme.bold(15), Theme.TEXT);
            name.setAlignmentX(0f);
            texts.add(name);
            if (!wish.note.isEmpty()) {
                JLabel note = Ui.label(wish.note, Theme.regular(12), Theme.MUTED);
                note.setAlignmentX(0f);
                texts.add(note);
            }
            JLabel added = Ui.label("dodano " + Instant.ofEpochMilli(wish.createdAt)
                            .atZone(ZoneId.systemDefault()).format(DATE_FMT),
                    Theme.regular(11), Theme.MUTED);
            added.setAlignmentX(0f);
            texts.add(added);

            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(texts, c);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.gridx = 1;
            c.insets = new Insets(0, 12, 0, 12);
            add(Ui.label(Theme.money(wish.price), Theme.bold(16), Theme.TEXT), c);

            c.gridx = 2;
            c.insets = new Insets(0, 0, 0, 14);
            add(countdown, c);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            Ui.PillButton resignBtn = new Ui.PillButton("Odpuszczam", Theme.MINT, Color.WHITE, 14, 7);
            resignBtn.addActionListener(e -> resign());
            buyBtn.addActionListener(e -> buy());
            Ui.PillButton deleteBtn = new Ui.PillButton("Usuń", Theme.RED_SOFT, Theme.RED, 12, 7);
            deleteBtn.addActionListener(e -> delete());
            buttons.add(resignBtn);
            buttons.add(buyBtn);
            buttons.add(deleteBtn);
            c.gridx = 3;
            c.insets = new Insets(0, 0, 0, 0);
            add(buttons, c);

            lastReady = store.isReady(wish);
            applyState();
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }

        void tick() {
            boolean ready = store.isReady(wish);
            if (ready != lastReady) {
                lastReady = ready;
                applyState();
            }
            if (!ready) countdown.setText(formatRemaining(store.remainingMs(wish)));
        }

        private void applyState() {
            if (store.isReady(wish)) {
                countdown.setText("Czas minął!");
                countdown.setColors(Theme.MINT_SOFT, Theme.MINT);
                buyBtn.setText("Kupuję po namyśle");
            } else {
                countdown.setText(formatRemaining(store.remainingMs(wish)));
                countdown.setColors(Theme.AMBER_SOFT, Theme.AMBER);
                buyBtn.setText("Kupuję mimo to");
            }
            revalidate();
            repaint();
        }

        private void resign() {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Świetna decyzja! Dodać " + Theme.money(wish.price) + " do Twoich oszczędności?",
                    "Odpuszczam zakup", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) store.resign(wish);
        }

        private void buy() {
            boolean ready = store.isReady(wish);
            String msg = ready
                    ? "Minęło pełne " + store.coolingHours() + " godzin — to przemyślana decyzja.\nZapisać zakup „"
                      + wish.name + "” (" + Theme.money(wish.price) + ") w wydatkach?"
                    : "Odliczanie jeszcze trwa (" + formatRemaining(store.remainingMs(wish))
                      + ").\nKupno teraz zostanie zapisane jako zakup IMPULSYWNY i wyzeruje Twoją serię.\nNa pewno kupujesz?";
            int ok = JOptionPane.showConfirmDialog(this, msg, "Kupuję",
                    JOptionPane.YES_NO_OPTION, ready ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) store.buy(wish, !ready);
        }

        private void delete() {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Usunąć „" + wish.name + "” z poczekalni bez śladu?",
                    "Usuń", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) store.deleteWish(wish);
        }
    }

    static String formatRemaining(long ms) {
        long s = Math.max(0, ms / 1000);
        long d = s / 86_400;
        s %= 86_400;
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        String clock = String.format("%02d:%02d:%02d", h, m, sec);
        return d > 0 ? d + " d " + clock : clock;
    }
}

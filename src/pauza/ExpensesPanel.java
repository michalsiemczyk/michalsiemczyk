package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Dziennik wydatków z miesięcznym budżetem i oznaczaniem zakupów impulsywnych. */
public class ExpensesPanel extends JPanel implements AppFrame.View {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("d MMM", Theme.PL);

    private final DataStore store;
    private final JLabel budgetTitle = Ui.label("", Theme.bold(12), Theme.MUTED);
    private final JLabel budgetValue = Ui.label("", Theme.bold(24), Theme.TEXT);
    private final JLabel budgetSub = Ui.label("", Theme.regular(12), Theme.MUTED);
    private final Ui.Bar budgetBar = new Ui.Bar();
    private final JLabel impulsiveValue = Ui.label("", Theme.bold(24), Theme.RED);
    private final JLabel impulsiveSub = Ui.label("", Theme.regular(12), Theme.MUTED);
    private final JTextField nameField = Ui.textField(14);
    private final JTextField amountField = Ui.textField(7);
    private final JComboBox<String> categoryBox = Ui.comboBox(DataStore.CATEGORIES);
    private final JCheckBox impulsiveCheck = Ui.checkBox("To był zakup impulsywny");
    private final JLabel formError = Ui.label(" ", Theme.bold(12), Theme.RED);
    private final JPanel listColumn = Ui.column();

    public ExpensesPanel(DataStore store) {
        this.store = store;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel top = Ui.column();
        JLabel title = Ui.title("Wydatki");
        title.setAlignmentX(0f);
        JLabel sub = Ui.subtitle("Zapisuj wszystko, co kupujesz. Świadomość to pierwszy krok do zmiany.");
        sub.setAlignmentX(0f);
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);
        top.add(Box.createVerticalStrut(16));

        JPanel cards = new JPanel(new GridLayout(1, 2, 14, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(0f);

        Ui.Card budgetCard = new Ui.Card();
        budgetCard.setLayout(new BoxLayout(budgetCard, BoxLayout.Y_AXIS));
        budgetTitle.setAlignmentX(0f);
        budgetValue.setAlignmentX(0f);
        budgetSub.setAlignmentX(0f);
        budgetBar.setAlignmentX(0f);
        budgetBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        budgetCard.add(budgetTitle);
        budgetCard.add(Box.createVerticalStrut(6));
        budgetCard.add(budgetValue);
        budgetCard.add(Box.createVerticalStrut(10));
        budgetCard.add(budgetBar);
        budgetCard.add(Box.createVerticalStrut(8));
        budgetCard.add(budgetSub);
        budgetCard.add(Box.createVerticalStrut(10));
        Ui.PillButton setBudget = new Ui.PillButton("Zmień budżet", Theme.ACCENT_SOFT, Theme.ACCENT, 14, 7);
        setBudget.setAlignmentX(0f);
        setBudget.addActionListener(e -> changeBudget());
        budgetCard.add(setBudget);
        cards.add(budgetCard);

        Ui.Card impulsiveCard = new Ui.Card();
        impulsiveCard.setLayout(new BoxLayout(impulsiveCard, BoxLayout.Y_AXIS));
        JLabel it = Ui.label("IMPULSYWNE W TYM MIESIĄCU", Theme.bold(12), Theme.MUTED);
        it.setAlignmentX(0f);
        impulsiveValue.setAlignmentX(0f);
        impulsiveSub.setAlignmentX(0f);
        impulsiveCard.add(it);
        impulsiveCard.add(Box.createVerticalStrut(6));
        impulsiveCard.add(impulsiveValue);
        impulsiveCard.add(Box.createVerticalStrut(4));
        impulsiveCard.add(impulsiveSub);
        JLabel tip = Ui.label("Trzymaj ten licznik jak najniżej.", Theme.regular(12), Theme.MUTED);
        tip.setAlignmentX(0f);
        impulsiveCard.add(Box.createVerticalGlue());
        impulsiveCard.add(tip);
        cards.add(impulsiveCard);

        top.add(cards);
        top.add(Box.createVerticalStrut(14));

        Ui.Card form = new Ui.Card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(0f);
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fields.setOpaque(false);
        fields.setAlignmentX(0f);
        fields.add(Ui.field("CO KUPIONO?", nameField));
        fields.add(Ui.field("KWOTA (ZŁ)", amountField));
        fields.add(Ui.field("KATEGORIA", categoryBox));
        JPanel checkWrap = new JPanel(new BorderLayout());
        checkWrap.setOpaque(false);
        checkWrap.setBorder(new EmptyBorder(19, 0, 0, 0));
        checkWrap.add(impulsiveCheck);
        fields.add(checkWrap);
        Ui.PillButton addBtn = new Ui.PillButton("Dodaj wydatek", Theme.ACCENT, Color.WHITE);
        addBtn.addActionListener(e -> addExpense());
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
        refreshView();
    }

    private void changeBudget() {
        String current = store.monthlyBudget() > 0 ? String.valueOf(store.monthlyBudget()) : "";
        String input = (String) JOptionPane.showInputDialog(this,
                "Ile maksymalnie chcesz wydawać miesięcznie (zł)?",
                "Miesięczny budżet", JOptionPane.PLAIN_MESSAGE, null, null, current);
        if (input == null) return;
        Double v = Ui.parseAmount(input);
        if (v == null) {
            JOptionPane.showMessageDialog(this, "To nie wygląda na poprawną kwotę.",
                    "Ups", JOptionPane.WARNING_MESSAGE);
            return;
        }
        store.setMonthlyBudget(v);
    }

    private void addExpense() {
        String name = nameField.getText().trim();
        Double amount = Ui.parseAmount(amountField.getText());
        if (name.isEmpty()) {
            formError.setText("Wpisz, co kupiono.");
            return;
        }
        if (amount == null) {
            formError.setText("Podaj poprawną kwotę, np. 59,99.");
            return;
        }
        formError.setText(" ");
        store.addExpense(name, amount, (String) categoryBox.getSelectedItem(),
                impulsiveCheck.isSelected(), LocalDate.now());
        nameField.setText("");
        amountField.setText("");
        impulsiveCheck.setSelected(false);
    }

    @Override public void refreshView() {
        YearMonth now = YearMonth.now();
        String monthName = now.format(DateTimeFormatter.ofPattern("LLLL yyyy", Theme.PL));
        budgetTitle.setText(("BUDŻET — " + monthName).toUpperCase(Theme.PL));

        double spent = store.monthSpent(now);
        double budget = store.monthlyBudget();
        if (budget > 0) {
            budgetValue.setText(Theme.money(spent) + "  /  " + Theme.money(budget));
            double f = spent / budget;
            budgetBar.set(f, f < 0.75 ? Theme.MINT : f <= 1.0 ? Theme.AMBER : Theme.RED);
            budgetSub.setText(spent <= budget
                    ? "Zostało " + Theme.money(budget - spent)
                    : "Przekroczono o " + Theme.money(spent - budget));
        } else {
            budgetValue.setText(Theme.money(spent));
            budgetBar.set(0, Theme.MINT);
            budgetSub.setText("Nie ustawiono jeszcze budżetu.");
        }

        impulsiveValue.setText(Theme.money(store.monthSpent(now, true)));
        int n = store.monthImpulsiveCount(now);
        impulsiveSub.setText(n == 0 ? "Zero impulsywnych zakupów — brawo!"
                : n + (n == 1 ? " zakup impulsywny" : n < 5 ? " zakupy impulsywne" : " zakupów impulsywnych"));

        listColumn.removeAll();
        List<DataStore.Expense> all = store.expensesNewestFirst();
        if (all.isEmpty()) {
            Ui.Card empty = new Ui.Card();
            empty.setLayout(new BorderLayout());
            empty.setAlignmentX(0f);
            JLabel l = Ui.label("Brak zapisanych wydatków. Dodaj pierwszy powyżej.",
                    Theme.regular(14), Theme.MUTED);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            empty.add(l);
            listColumn.add(empty);
        } else {
            JLabel h = Ui.section("Ostatnie wydatki");
            h.setAlignmentX(0f);
            listColumn.add(h);
            listColumn.add(Box.createVerticalStrut(10));
            int limit = Math.min(all.size(), 50);
            for (int i = 0; i < limit; i++) {
                listColumn.add(expenseRow(all.get(i)));
                listColumn.add(Box.createVerticalStrut(8));
            }
        }
        listColumn.revalidate();
        listColumn.repaint();
    }

    private JComponent expenseRow(DataStore.Expense e) {
        Ui.Card card = new Ui.Card(14);
        card.setBorder(new EmptyBorder(11, 18, 11, 18));
        card.setAlignmentX(0f);
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

        JLabel date = Ui.label(e.date.format(DAY_FMT), Theme.bold(12), Theme.MUTED);
        date.setPreferredSize(new Dimension(56, 20));
        c.gridx = 0;
        card.add(date, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 8, 0, 8);
        card.add(Ui.label(e.name, Theme.bold(14), Theme.TEXT), c);

        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        card.add(new Ui.Chip(e.category, Theme.ACCENT_SOFT, Theme.ACCENT), c);

        if (e.impulsive) {
            c.gridx = 3;
            card.add(new Ui.Chip("impuls", Theme.RED_SOFT, Theme.RED), c);
        }

        c.gridx = 4;
        c.insets = new Insets(0, 12, 0, 12);
        card.add(Ui.label(Theme.money(e.amount), Theme.bold(15),
                e.impulsive ? Theme.RED : Theme.TEXT), c);

        Ui.PillButton del = new Ui.PillButton("Usuń", Theme.RED_SOFT, Theme.RED, 12, 6);
        del.addActionListener(ev -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Usunąć wydatek „" + e.name + "” (" + Theme.money(e.amount) + ")?",
                    "Usuń wydatek", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) store.deleteExpense(e);
        });
        c.gridx = 5;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(del, c);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }
}

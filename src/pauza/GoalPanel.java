package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cel oszczędzania: każdy odpuszczony zakup z poczekalni przybliża Cię do celu.
 */
public class GoalPanel extends JPanel implements AppFrame.View {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy", Theme.PL);

    private final DataStore store;
    private final Ring ring = new Ring();
    private final JLabel goalLabel = Ui.label("", Theme.bold(18), Theme.TEXT);
    private final JLabel goalSub = Ui.label("", Theme.regular(13), Theme.MUTED);
    private final JPanel winsColumn = Ui.column();

    public GoalPanel(DataStore store) {
        this.store = store;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel top = Ui.column();
        JLabel title = Ui.title("Cel oszczędzania");
        title.setAlignmentX(0f);
        JLabel sub = Ui.subtitle("Pieniądze z odpuszczonych zakupów pracują na Twoje marzenie.");
        sub.setAlignmentX(0f);
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);
        top.add(Box.createVerticalStrut(16));
        add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);

        Ui.Card ringCard = new Ui.Card();
        ringCard.setLayout(new BoxLayout(ringCard, BoxLayout.Y_AXIS));
        ring.setAlignmentX(0.5f);
        goalLabel.setAlignmentX(0.5f);
        goalSub.setAlignmentX(0.5f);
        ringCard.add(Box.createVerticalStrut(6));
        ringCard.add(ring);
        ringCard.add(Box.createVerticalStrut(10));
        ringCard.add(goalLabel);
        ringCard.add(Box.createVerticalStrut(4));
        ringCard.add(goalSub);
        ringCard.add(Box.createVerticalStrut(14));
        Ui.PillButton setGoal = new Ui.PillButton("Ustaw cel", Theme.ACCENT, Color.WHITE);
        setGoal.setAlignmentX(0.5f);
        setGoal.addActionListener(e -> editGoal());
        ringCard.add(setGoal);
        ringCard.add(Box.createVerticalGlue());
        grid.add(ringCard);

        Ui.Card winsCard = new Ui.Card();
        winsCard.setLayout(new BorderLayout());
        JLabel winsTitle = Ui.section("Twoje wygrane");
        JPanel winsHead = Ui.column();
        winsTitle.setAlignmentX(0f);
        JLabel winsSub = Ui.label("Każda pozycja to pokusa, którą pokonałeś(-aś).",
                Theme.regular(12), Theme.MUTED);
        winsSub.setAlignmentX(0f);
        winsHead.add(winsTitle);
        winsHead.add(Box.createVerticalStrut(3));
        winsHead.add(winsSub);
        winsHead.add(Box.createVerticalStrut(12));
        winsCard.add(winsHead, BorderLayout.NORTH);
        winsCard.add(Ui.scroll(winsColumn, Theme.CARD), BorderLayout.CENTER);
        grid.add(winsCard);

        add(grid, BorderLayout.CENTER);
        refreshView();
    }

    private void editGoal() {
        JTextField nameF = Ui.textField(16);
        nameF.setText(store.goalName());
        JTextField amountF = Ui.textField(8);
        if (store.goalAmount() > 0) amountF.setText(String.valueOf(store.goalAmount()));

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.add(new JLabel("Na co oszczędzasz?"));
        p.add(nameF);
        p.add(new JLabel("Ile potrzebujesz (zł)?"));
        p.add(amountF);

        int ok = JOptionPane.showConfirmDialog(this, p, "Twój cel",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String name = nameF.getText().trim();
        Double amount = Ui.parseAmount(amountF.getText());
        if (name.isEmpty() || amount == null) {
            JOptionPane.showMessageDialog(this, "Podaj nazwę celu i poprawną kwotę.",
                    "Ups", JOptionPane.WARNING_MESSAGE);
            return;
        }
        store.setGoal(name, amount);
    }

    @Override public void refreshView() {
        double saved = store.totalSaved();
        double goal = store.goalAmount();

        if (goal > 0) {
            ring.set(saved / goal, Theme.money(saved), "z " + Theme.money(goal));
            goalLabel.setText(store.goalName());
            goalSub.setText(saved >= goal
                    ? "Cel osiągnięty! Jesteś mistrzem odpuszczania."
                    : "Brakuje jeszcze " + Theme.money(goal - saved) + ".");
        } else {
            ring.set(0, Theme.money(saved), "zaoszczędzone");
            goalLabel.setText("Nie masz jeszcze celu");
            goalSub.setText("Ustaw go — oszczędzanie z celem jest łatwiejsze.");
        }

        winsColumn.removeAll();
        List<DataStore.Wish> decided = store.decidedWishes();
        boolean any = false;
        for (DataStore.Wish w : decided) {
            if (w.status != DataStore.WishStatus.RESIGNED) continue;
            any = true;
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(7, 2, 7, 2));
            row.setAlignmentX(0f);
            JPanel left = Ui.column();
            JLabel n = Ui.label(w.name, Theme.bold(14), Theme.TEXT);
            n.setAlignmentX(0f);
            JLabel d = Ui.label(Instant.ofEpochMilli(w.decidedAt)
                    .atZone(ZoneId.systemDefault()).format(DATE_FMT), Theme.regular(11), Theme.MUTED);
            d.setAlignmentX(0f);
            left.add(n);
            left.add(d);
            row.add(left, BorderLayout.CENTER);
            JLabel amount = Ui.label("+" + Theme.money(w.price), Theme.bold(14), Theme.MINT);
            row.add(amount, BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            winsColumn.add(row);
            JSeparator sep = new JSeparator();
            sep.setForeground(Theme.CARD_BORDER);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            winsColumn.add(sep);
        }
        if (!any) {
            JLabel empty = Ui.label("Jeszcze nic tu nie ma. Odpuść pierwszą pokusę w Poczekalni!",
                    Theme.regular(13), Theme.MUTED);
            empty.setAlignmentX(0f);
            winsColumn.add(empty);
        }
        winsColumn.revalidate();
        winsColumn.repaint();
    }

    /** Pierścień postępu z kwotą w środku. */
    private static class Ring extends JComponent {
        private double fraction;
        private String center = "";
        private String sub = "";

        Ring() {
            setPreferredSize(new Dimension(230, 230));
            setMaximumSize(new Dimension(230, 230));
        }

        void set(double fraction, String center, String sub) {
            this.fraction = Math.max(0, Math.min(1, fraction));
            this.center = center;
            this.sub = sub;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 24;
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.ACCENT_SOFT);
            g2.draw(new Arc2D.Float(x, y, size, size, 0, 360, Arc2D.OPEN));
            if (fraction > 0) {
                g2.setColor(fraction >= 1 ? Theme.MINT : Theme.ACCENT);
                g2.draw(new Arc2D.Float(x, y, size, size, 90, (float) (-360 * fraction), Arc2D.OPEN));
            }

            g2.setFont(Theme.bold(22));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(center, (getWidth() - fm.stringWidth(center)) / 2, getHeight() / 2);
            g2.setFont(Theme.regular(13));
            g2.setColor(Theme.MUTED);
            fm = g2.getFontMetrics();
            g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, getHeight() / 2 + 22);
            g2.dispose();
        }
    }
}

package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Statystyki: porównanie wydatków planowanych i impulsywnych + skuteczność poczekalni. */
public class StatsPanel extends JPanel implements AppFrame.View {

    private final DataStore store;
    private final JLabel plannedValue = Ui.label("", Theme.bold(24), Theme.ACCENT);
    private final JLabel impulsiveValue = Ui.label("", Theme.bold(24), Theme.RED);
    private final JLabel effectValue = Ui.label("", Theme.bold(24), Theme.MINT);
    private final JLabel effectSub = Ui.label("", Theme.regular(12), Theme.MUTED);
    private final BarChart chart = new BarChart();

    public StatsPanel(DataStore store) {
        this.store = store;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel top = Ui.column();
        JLabel title = Ui.title("Statystyki");
        title.setAlignmentX(0f);
        JLabel sub = Ui.subtitle("Zobacz, jak zmieniają się Twoje nawyki z miesiąca na miesiąc.");
        sub.setAlignmentX(0f);
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);
        top.add(Box.createVerticalStrut(16));

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(0f);
        cards.add(statCard("PLANOWANE (TEN ROK)", plannedValue,
                Ui.label("Zakupy przemyślane", Theme.regular(12), Theme.MUTED)));
        cards.add(statCard("IMPULSYWNE (TEN ROK)", impulsiveValue,
                Ui.label("Tego chcemy mniej", Theme.regular(12), Theme.MUTED)));
        cards.add(statCard("SKUTECZNOŚĆ POCZEKALNI", effectValue, effectSub));
        top.add(cards);
        top.add(Box.createVerticalStrut(14));
        add(top, BorderLayout.NORTH);

        Ui.Card chartCard = new Ui.Card();
        chartCard.setLayout(new BorderLayout());
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        head.setOpaque(false);
        head.add(Ui.section("Ostatnie 6 miesięcy"));
        head.add(legendDot(Theme.ACCENT, "planowane"));
        head.add(legendDot(Theme.RED, "impulsywne"));
        chartCard.add(head, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

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

    private JComponent legendDot(Color color, String text) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, getHeight() / 2 - 4, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(Theme.regular(12));
        l.setForeground(Theme.MUTED);
        l.setBorder(new EmptyBorder(0, 13, 0, 0));
        return l;
    }

    @Override public void refreshView() {
        int year = LocalDate.now().getYear();
        plannedValue.setText(Theme.money(store.yearSpent(year, false)));
        impulsiveValue.setText(Theme.money(store.yearSpent(year, true)));

        int resigned = 0, bought = 0;
        for (DataStore.Wish w : store.decidedWishes()) {
            if (w.status == DataStore.WishStatus.RESIGNED) resigned++;
            else bought++;
        }
        if (resigned + bought == 0) {
            effectValue.setText("—");
            effectSub.setText("Brak decyzji w poczekalni");
        } else {
            effectValue.setText(Math.round(100.0 * resigned / (resigned + bought)) + "%");
            effectSub.setText("Odpuszczone " + resigned + " z " + (resigned + bought) + " pokus");
        }

        List<BarChart.MonthData> data = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = now.minusMonths(i);
            data.add(new BarChart.MonthData(
                    m.format(DateTimeFormatter.ofPattern("LLL", Theme.PL)),
                    store.monthSpent(m, false),
                    store.monthSpent(m, true)));
        }
        chart.setData(data);
    }

    /** Prosty, ręcznie malowany wykres słupkowy (dwa słupki na miesiąc). */
    static class BarChart extends JComponent {

        record MonthData(String label, double planned, double impulsive) {}

        private List<MonthData> data = new ArrayList<>();

        BarChart() { setPreferredSize(new Dimension(100, 280)); }

        void setData(List<MonthData> data) {
            this.data = data;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padLeft = 56, padRight = 12, padTop = 18, padBottom = 30;
            int plotW = getWidth() - padLeft - padRight;
            int plotH = getHeight() - padTop - padBottom;
            if (plotW <= 0 || plotH <= 0 || data.isEmpty()) { g2.dispose(); return; }

            double max = 0;
            for (MonthData m : data) max = Math.max(max, Math.max(m.planned, m.impulsive));
            boolean empty = max <= 0;
            double niceMax = niceCeil(Math.max(max, 1));

            // Siatka pozioma z podpisami kwot
            g2.setFont(Theme.regular(11));
            for (int i = 0; i <= 4; i++) {
                int y = padTop + plotH - (int) (plotH * i / 4.0);
                g2.setColor(new Color(0xEC, 0xEA, 0xF4));
                g2.drawLine(padLeft, y, getWidth() - padRight, y);
                g2.setColor(Theme.MUTED);
                String label = formatShort(niceMax * i / 4);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, padLeft - 8 - fm.stringWidth(label), y + 4);
            }

            int n = data.size();
            float slot = plotW / (float) n;
            int barW = (int) Math.min(26, slot / 3.2f);
            int gap = 6;

            for (int i = 0; i < n; i++) {
                MonthData m = data.get(i);
                float centerX = padLeft + slot * i + slot / 2;
                drawBar(g2, centerX - barW - gap / 2f, m.planned, niceMax, plotH, padTop, barW, Theme.ACCENT);
                drawBar(g2, centerX + gap / 2f, m.impulsive, niceMax, plotH, padTop, barW, Theme.RED);
                g2.setColor(Theme.MUTED);
                g2.setFont(Theme.bold(12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(m.label, centerX - fm.stringWidth(m.label) / 2f, getHeight() - 8);
            }

            if (empty) {
                String msg = "Brak danych — dodaj pierwsze wydatki, a wykres ożyje.";
                g2.setFont(Theme.regular(14));
                g2.setColor(Theme.MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, padTop + plotH / 2);
            }
            g2.dispose();
        }

        private void drawBar(Graphics2D g2, float x, double value, double max,
                             int plotH, int padTop, int barW, Color color) {
            if (value <= 0) return;
            int h = (int) Math.max(4, Math.round(plotH * value / max));
            int y = padTop + plotH - h;
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, h, 7, 7));
        }

        private static String formatShort(double v) {
            if (v >= 1000) {
                double k = v / 1000;
                return (k == Math.floor(k) ? String.valueOf((long) k) : String.format(Theme.PL, "%.1f", k)) + " tys.";
            }
            return String.valueOf(Math.round(v));
        }

        private static double niceCeil(double v) {
            double pow = Math.pow(10, Math.floor(Math.log10(v)));
            for (double m : new double[]{1, 2, 2.5, 5, 10}) {
                if (m * pow >= v) return m * pow;
            }
            return 10 * pow;
        }
    }
}

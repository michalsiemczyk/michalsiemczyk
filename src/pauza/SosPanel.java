package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Tryb SOS na moment silnej pokusy: ćwiczenie oddechowe 4-4-4
 * i lista pytań kontrolnych, które studzą impuls.
 */
public class SosPanel extends JPanel implements AppFrame.View {

    private static final String[] QUESTIONS = {
            "Czy naprawdę tego potrzebuję?",
            "Czy mam już coś podobnego?",
            "Czy kupił(a)bym to za pełną cenę?",
            "Czy za tydzień nadal będę tego chcieć?",
            "Czy stać mnie na to bez pożyczania?",
            "Czy nie kupuję z nudów albo stresu?",
    };

    private final List<JCheckBox> checks = new ArrayList<>();
    private final JLabel checkStatus = Ui.label(" ", Theme.bold(13), Theme.ACCENT);
    private final Breathing breathing = new Breathing();
    private final Ui.PillButton breathBtn = new Ui.PillButton("Zacznij oddychać", Theme.ACCENT, Color.WHITE);

    public SosPanel(DataStore store, AppFrame frame) {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel top = Ui.column();
        JLabel title = Ui.title("SOS — czujesz pokusę?");
        title.setAlignmentX(0f);
        JLabel sub = Ui.subtitle("Zatrzymaj się. Impuls zakupowy zwykle mija po kilku minutach — daj sobie te minuty.");
        sub.setAlignmentX(0f);
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);
        top.add(Box.createVerticalStrut(16));
        add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);

        Ui.Card breathCard = new Ui.Card();
        breathCard.setLayout(new BoxLayout(breathCard, BoxLayout.Y_AXIS));
        JLabel bt = Ui.section("Oddech 4·4·4");
        bt.setAlignmentX(0.5f);
        JLabel bs = Ui.label("4 sekundy wdechu, 4 zatrzymania, 4 wydechu.", Theme.regular(12), Theme.MUTED);
        bs.setAlignmentX(0.5f);
        breathing.setAlignmentX(0.5f);
        breathBtn.setAlignmentX(0.5f);
        breathBtn.addActionListener(e -> {
            breathing.toggle();
            breathBtn.setText(breathing.isRunning() ? "Zatrzymaj" : "Zacznij oddychać");
        });
        breathCard.add(bt);
        breathCard.add(Box.createVerticalStrut(2));
        breathCard.add(bs);
        breathCard.add(breathing);
        breathCard.add(breathBtn);
        breathCard.add(Box.createVerticalStrut(6));
        grid.add(breathCard);

        Ui.Card askCard = new Ui.Card();
        askCard.setLayout(new BoxLayout(askCard, BoxLayout.Y_AXIS));
        JLabel at = Ui.section("Zanim kupisz, zapytaj siebie:");
        at.setAlignmentX(0f);
        askCard.add(at);
        askCard.add(Box.createVerticalStrut(10));
        for (String q : QUESTIONS) {
            JCheckBox cb = Ui.checkBox(q);
            cb.setAlignmentX(0f);
            cb.addItemListener(e -> updateCheckStatus());
            checks.add(cb);
            askCard.add(cb);
            askCard.add(Box.createVerticalStrut(4));
        }
        askCard.add(Box.createVerticalStrut(8));
        checkStatus.setAlignmentX(0f);
        askCard.add(checkStatus);
        askCard.add(Box.createVerticalGlue());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.setAlignmentX(0f);
        Ui.PillButton toCooling = new Ui.PillButton("Nadal chcę — do poczekalni", Theme.AMBER_SOFT, Theme.AMBER);
        toCooling.addActionListener(e -> {
            resetChecks();
            frame.show("cooling");
        });
        Ui.PillButton passed = new Ui.PillButton("Pokusa minęła!", Theme.MINT, Color.WHITE);
        passed.addActionListener(e -> {
            resetChecks();
            JOptionPane.showMessageDialog(this,
                    "Brawo! Właśnie wygrałeś(-aś) z impulsem.\nJeśli znasz cenę pokusy, dodaj ją do Poczekalni i od razu odpuść —\nkwota trafi do Twoich oszczędności.",
                    "Zwycięstwo", JOptionPane.INFORMATION_MESSAGE);
        });
        btns.add(toCooling);
        btns.add(passed);
        askCard.add(btns);
        grid.add(askCard);

        add(grid, BorderLayout.CENTER);
        updateCheckStatus();
    }

    private void updateCheckStatus() {
        long done = checks.stream().filter(AbstractButton::isSelected).count();
        if (done == 0) {
            checkStatus.setText("Odhaczaj pytania, nad którymi się zastanowisz.");
            checkStatus.setForeground(Theme.MUTED);
        } else if (done < checks.size()) {
            checkStatus.setText("Przemyślane " + done + " z " + checks.size() + " — nie spiesz się.");
            checkStatus.setForeground(Theme.ACCENT);
        } else {
            checkStatus.setText("Wszystko przemyślane. Jeśli nadal chcesz — daj sobie jeszcze 48 godzin.");
            checkStatus.setForeground(Theme.MINT);
        }
    }

    private void resetChecks() {
        for (JCheckBox c : checks) c.setSelected(false);
    }

    @Override public void refreshView() { /* panel nie zależy od danych */ }

    /** Animowane koło oddechowe: rośnie przy wdechu, zamiera, maleje przy wydechu. */
    private static class Breathing extends JComponent {
        private static final int PHASE = 4000;               // ms na fazę
        private final Timer timer = new Timer(30, e -> repaint());
        private long start;
        private boolean running;

        Breathing() {
            setPreferredSize(new Dimension(300, 280));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        }

        boolean isRunning() { return running; }

        void toggle() {
            running = !running;
            if (running) {
                start = System.currentTimeMillis();
                timer.start();
            } else {
                timer.stop();
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            float maxR = Math.min(getWidth(), getHeight()) / 2f - 24;
            float minR = maxR * 0.45f;

            String phaseText;
            String secondsText = "";
            float p;
            if (!running) {
                phaseText = "Gotowy?";
                p = 0.3f;
            } else {
                long t = (System.currentTimeMillis() - start) % (3L * PHASE);
                long within = t % PHASE;
                int secondsLeft = (int) ((PHASE - within) / 1000) + 1;
                secondsText = String.valueOf(secondsLeft);
                if (t < PHASE)          { phaseText = "Wdech";     p = within / (float) PHASE; }
                else if (t < 2L * PHASE) { phaseText = "Zatrzymaj"; p = 1f; }
                else                     { phaseText = "Wydech";    p = 1f - within / (float) PHASE; }
            }
            float ease = p * p * (3 - 2 * p);                // płynne przyspieszanie i zwalnianie
            float r = minR + (maxR - minR) * ease;

            g2.setColor(Theme.ACCENT_SOFT);
            g2.fill(new Ellipse2D.Float(cx - maxR, cy - maxR, maxR * 2, maxR * 2));
            g2.setPaint(new GradientPaint(cx - r, cy - r, Theme.ACCENT, cx + r, cy + r, Theme.ACCENT_DARK));
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));

            g2.setColor(Color.WHITE);
            g2.setFont(Theme.bold(18));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(phaseText, cx - fm.stringWidth(phaseText) / 2, cy - (running ? 6 : -6));
            if (running) {
                g2.setFont(Theme.bold(26));
                fm = g2.getFontMetrics();
                g2.drawString(secondsText, cx - fm.stringWidth(secondsText) / 2, cy + 26);
            }
            g2.dispose();
        }
    }
}

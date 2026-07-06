package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Wspólne, ręcznie malowane komponenty interfejsu (karty, przyciski, paski postępu...). */
public final class Ui {

    private Ui() {}

    /* ------------------------------------------------------------------ */
    /* Karty                                                                */
    /* ------------------------------------------------------------------ */

    /** Biała zaokrąglona karta z delikatnym cieniem. */
    public static class Card extends JPanel {
        private final int arc;

        public Card() { this(18); }

        public Card(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBorder(new EmptyBorder(18, 22, 18, 22));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(30, 20, 60, 12));
            g2.fill(new RoundRectangle2D.Float(2, 3, w - 4, h - 4, arc + 4, arc + 4));
            g2.setColor(Theme.CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 3, arc, arc));
            g2.setColor(Theme.CARD_BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 4, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Karta z fioletowym gradientem i dekoracyjnymi okręgami — na cytaty i wyróżnienia. */
    public static class GradientCard extends JPanel {
        public GradientCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(22, 26, 22, 26));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Shape rect = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
            g2.setPaint(new GradientPaint(0, 0, Theme.ACCENT, w, h, Theme.ACCENT_DARK));
            g2.fill(rect);
            g2.setClip(rect);
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(w - 130, -70, 190, 190);
            g2.fillOval(w - 60, h - 60, 120, 120);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Przyciski i etykiety                                                 */
    /* ------------------------------------------------------------------ */

    /** Zaokrąglony, wypełniony kolorem przycisk z reakcją na najechanie. */
    public static class PillButton extends JButton {
        private final Color bg;
        private boolean hover;

        public PillButton(String text, Color bg, Color fg) { this(text, bg, fg, 18, 9); }

        public PillButton(String text, Color bg, Color fg, int hpad, int vpad) {
            super(text);
            this.bg = bg;
            setFont(Theme.bold(13));
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(vpad, hpad, vpad, hpad));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = bg;
            if (!isEnabled()) c = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 110);
            else if (hover)   c = darken(bg, 0.90f);
            g2.setColor(c);
            int h = getHeight();
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), h, h, h));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Mała zaokrąglona "pastylka" z tekstem, np. status albo kategoria. */
    public static class Chip extends JLabel {
        private Color bg;

        public Chip(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            setFont(Theme.bold(12));
            setForeground(fg);
            setBorder(new EmptyBorder(4, 11, 4, 11));
        }

        public void setColors(Color bg, Color fg) {
            this.bg = bg;
            setForeground(fg);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            int h = getHeight();
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), h, h, h));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Poziomy pasek postępu o zaokrąglonych końcach. */
    public static class Bar extends JComponent {
        private double fraction;
        private Color color = Theme.ACCENT;

        public Bar() { setPreferredSize(new Dimension(120, 10)); }

        public void set(double fraction, Color color) {
            this.fraction = Math.max(0, Math.min(1, fraction));
            this.color = color;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0xEC, 0xEA, 0xF4));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            int fw = (int) Math.round(w * fraction);
            if (fw > 0) {
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, Math.max(fw, h), h, h, h));
            }
            g2.dispose();
        }
    }

    /* ------------------------------------------------------------------ */
    /* Nawigacja boczna                                                     */
    /* ------------------------------------------------------------------ */

    /** Wektorowe ikonki nawigacji rysowane kolorem tekstu przycisku. */
    public static class NavIcon implements Icon {
        public enum Kind { HOME, CLOCK, WALLET, TARGET, HEART, CHART }

        private final Kind kind;

        public NavIcon(Kind kind) { this.kind = kind; }

        @Override public int getIconWidth()  { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (kind) {
                case HOME -> {
                    g2.drawPolyline(new int[]{2, 9, 16}, new int[]{9, 2, 9}, 3);
                    g2.drawPolyline(new int[]{4, 4, 14, 14}, new int[]{9, 15, 15, 9}, 4);
                    g2.drawLine(9, 15, 9, 11);
                }
                case CLOCK -> {
                    g2.drawOval(2, 2, 14, 14);
                    g2.drawLine(9, 9, 9, 5);
                    g2.drawLine(9, 9, 12, 11);
                }
                case WALLET -> {
                    g2.drawRoundRect(2, 4, 14, 11, 4, 4);
                    g2.drawLine(2, 8, 16, 8);
                    g2.fillOval(11, 10, 3, 3);
                }
                case TARGET -> {
                    g2.drawOval(2, 2, 14, 14);
                    g2.drawOval(6, 6, 6, 6);
                    g2.fillOval(8, 8, 3, 3);
                }
                case HEART -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(9, 15);
                    p.curveTo(3.2, 10.6, 2.2, 5.8, 5.8, 4.2);
                    p.curveTo(7.6, 3.4, 8.8, 4.8, 9, 6);
                    p.curveTo(9.2, 4.8, 10.4, 3.4, 12.2, 4.2);
                    p.curveTo(15.8, 5.8, 14.8, 10.6, 9, 15);
                    g2.draw(p);
                }
                case CHART -> {
                    g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(4, 15, 4, 11);
                    g2.drawLine(9, 15, 9, 4);
                    g2.drawLine(14, 15, 14, 8);
                }
            }
            g2.dispose();
        }
    }

    /** Przycisk w bocznym menu — podświetla się po najechaniu i gdy aktywny. */
    public static class NavButton extends JButton {
        private boolean active;
        private boolean hover;

        public NavButton(String text, NavIcon.Kind kind) {
            super(text);
            setIcon(new NavIcon(kind));
            setHorizontalAlignment(LEFT);
            setIconTextGap(12);
            setFont(Theme.bold(14));
            setForeground(Theme.WHITE_MUTED);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(0f);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : Theme.WHITE_MUTED);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active || hover) {
                g2.setColor(active ? Theme.SIDEBAR_ACTIVE : Theme.SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            }
            if (active) {
                g2.setColor(Theme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 10, 4, getHeight() - 20, 4, 4));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Pomocnicze fabryki                                                   */
    /* ------------------------------------------------------------------ */

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel title(String text)    { return label(text, Theme.bold(24), Theme.TEXT); }
    public static JLabel subtitle(String text) { return label(text, Theme.regular(14), Theme.MUTED); }
    public static JLabel section(String text)  { return label(text, Theme.bold(16), Theme.TEXT); }

    public static JTextField textField(int columns) {
        JTextField t = new JTextField(columns);
        t.setFont(Theme.regular(14));
        t.setForeground(Theme.TEXT);
        t.setCaretColor(Theme.TEXT);
        t.setBackground(Color.WHITE);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)));
        return t;
    }

    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(Theme.regular(14));
        c.setForeground(Theme.TEXT);
        c.setBackground(Color.WHITE);
        return c;
    }

    public static JCheckBox checkBox(String text) {
        JCheckBox c = new JCheckBox(text);
        c.setFont(Theme.regular(14));
        c.setForeground(Theme.TEXT);
        c.setOpaque(false);
        c.setFocusPainted(false);
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return c;
    }

    /** Podpisane pole formularza: mała szara etykieta nad kontrolką. */
    public static JPanel field(String labelText, JComponent input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = label(labelText, Theme.bold(12), Theme.MUTED);
        l.setAlignmentX(0f);
        input.setAlignmentX(0f);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(input);
        return p;
    }

    /** Pionowa kolumna na listy kart. */
    public static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static JScrollPane scroll(JComponent content) { return scroll(content, Theme.BG); }

    public static JScrollPane scroll(JComponent content, Color bg) {
        ScrollablePanel wrapper = new ScrollablePanel();
        wrapper.setBackground(bg);
        wrapper.add(content, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.getViewport().setBackground(bg);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    /** Panel, który zawsze dopasowuje szerokość do okna — zawartość nigdy nie wystaje w bok. */
    private static class ScrollablePanel extends JPanel implements Scrollable {
        ScrollablePanel() { super(new BorderLayout()); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Parsuje kwotę wpisaną przez użytkownika ("12,50", "12.50", "1 200"). Zwraca null, gdy niepoprawna. */
    public static Double parseAmount(String s) {
        if (s == null) return null;
        try {
            double v = Double.parseDouble(s.trim().replace(" ", "").replace(" ", "").replace(",", "."));
            return (v > 0 && v < 100_000_000) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Color darken(Color c, float factor) {
        return new Color(Math.round(c.getRed() * factor),
                Math.round(c.getGreen() * factor),
                Math.round(c.getBlue() * factor));
    }

    /** Wąski, nowoczesny pasek przewijania. */
    public static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0xC9C5DC);
            trackColor = Theme.BG;
        }

        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Float(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8));
            g2.dispose();
        }
    }
}

package pauza;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/** Główne okno: ciemny pasek nawigacji po lewej, widoki przełączane po prawej. */
public class AppFrame extends JFrame {

    /** Widok, który potrafi odświeżyć swoje dane. */
    public interface View {
        void refreshView();
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JComponent> views = new LinkedHashMap<>();
    private final Map<String, Ui.NavButton> navButtons = new LinkedHashMap<>();
    private JPanel navArea;
    private String currentId;

    public AppFrame(DataStore store) {
        super("Pauza — asystent świadomych zakupów");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 750);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        setIconImage(appIcon());
        setLayout(new BorderLayout());

        content.setBackground(Theme.BG);
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        addView("dashboard", "Pulpit", Ui.NavIcon.Kind.HOME, new DashboardPanel(store, this));
        addView("cooling", "Poczekalnia", Ui.NavIcon.Kind.CLOCK, new CoolingPanel(store));
        addView("expenses", "Wydatki", Ui.NavIcon.Kind.WALLET, new ExpensesPanel(store));
        addView("goal", "Cel oszczędzania", Ui.NavIcon.Kind.TARGET, new GoalPanel(store));
        addView("sos", "SOS — pokusa!", Ui.NavIcon.Kind.HEART, new SosPanel(store, this));
        addView("stats", "Statystyki", Ui.NavIcon.Kind.CHART, new StatsPanel(store));

        // Po każdej zmianie danych odśwież aktualnie widoczny ekran.
        store.addListener(() -> {
            JComponent v = views.get(currentId);
            if (v instanceof View view) view.refreshView();
        });

        show("dashboard");
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(238, 100));

        JPanel logo = Ui.column();
        logo.setBorder(new EmptyBorder(28, 26, 20, 20));
        JLabel name = Ui.label("Pauza", Theme.bold(27), Color.WHITE);
        name.setAlignmentX(0f);
        JLabel sub = Ui.label("świadome zakupy", Theme.regular(13), Theme.WHITE_MUTED);
        sub.setAlignmentX(0f);
        logo.add(name);
        logo.add(Box.createVerticalStrut(2));
        logo.add(sub);
        side.add(logo, BorderLayout.NORTH);

        navArea = Ui.column();
        navArea.setBorder(new EmptyBorder(6, 14, 6, 14));
        side.add(navArea, BorderLayout.CENTER);

        JLabel foot = Ui.label("Mniej impulsów, więcej spokoju", Theme.regular(11), Theme.WHITE_MUTED);
        foot.setBorder(new EmptyBorder(0, 26, 20, 12));
        side.add(foot, BorderLayout.SOUTH);
        return side;
    }

    private void addView(String id, String title, Ui.NavIcon.Kind kind, JComponent panel) {
        views.put(id, panel);
        content.add(panel, id);
        Ui.NavButton button = new Ui.NavButton(title, kind);
        button.addActionListener(e -> show(id));
        navButtons.put(id, button);
        navArea.add(button);
        navArea.add(Box.createVerticalStrut(4));
    }

    /** Przełącza widoczny ekran i odświeża jego zawartość. */
    public void show(String id) {
        currentId = id;
        navButtons.forEach((key, button) -> button.setActive(key.equals(id)));
        JComponent v = views.get(id);
        if (v instanceof View view) view.refreshView();
        cardLayout.show(content, id);
    }

    /** Ikona okna: fioletowy kwadrat z symbolem pauzy. */
    private static Image appIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, Theme.ACCENT, 64, 64, Theme.ACCENT_DARK));
        g2.fillRoundRect(2, 2, 60, 60, 20, 20);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(20, 18, 8, 28, 5, 5);
        g2.fillRoundRect(36, 18, 8, 28, 5, 5);
        g2.dispose();
        return img;
    }
}

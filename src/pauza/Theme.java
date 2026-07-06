package pauza;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Paleta kolorów, czcionki i formatowanie — wspólny wygląd całej aplikacji. */
public final class Theme {

    private Theme() {}

    public static final Locale PL = Locale.forLanguageTag("pl-PL");

    // Tło i karty
    public static final Color BG             = new Color(0xF4F3F8);
    public static final Color CARD           = Color.WHITE;
    public static final Color CARD_BORDER    = new Color(0xE6E3F0);

    // Boczny panel nawigacji
    public static final Color SIDEBAR        = new Color(0x1E1833);
    public static final Color SIDEBAR_HOVER  = new Color(0x2A2247);
    public static final Color SIDEBAR_ACTIVE = new Color(0x372C5E);
    public static final Color WHITE_MUTED    = new Color(255, 255, 255, 170);

    // Kolory akcentowe
    public static final Color ACCENT         = new Color(0x7C5CFF);
    public static final Color ACCENT_DARK    = new Color(0x5A3FE0);
    public static final Color ACCENT_SOFT    = new Color(0xEEE9FF);
    public static final Color MINT           = new Color(0x17A277);
    public static final Color MINT_SOFT      = new Color(0xE0F5EC);
    public static final Color RED            = new Color(0xD64550);
    public static final Color RED_SOFT       = new Color(0xFBE9EA);
    public static final Color AMBER          = new Color(0xB97613);
    public static final Color AMBER_SOFT     = new Color(0xFCF1DC);

    // Tekst
    public static final Color TEXT           = new Color(0x241F38);
    public static final Color MUTED          = new Color(0x87829E);

    private static String family;

    /** Wybiera najładniejszą dostępną czcionkę systemową. */
    public static String fontFamily() {
        if (family == null) {
            Set<String> available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            family = "SansSerif";
            for (String f : new String[]{"Inter", "SF Pro Text", "Segoe UI", "Ubuntu", "Noto Sans", "DejaVu Sans"}) {
                if (available.contains(f)) { family = f; break; }
            }
        }
        return family;
    }

    public static Font font(int style, int size) { return new Font(fontFamily(), style, size); }
    public static Font regular(int size)         { return font(Font.PLAIN, size); }
    public static Font bold(int size)            { return font(Font.BOLD, size); }

    private static final NumberFormat PLN = NumberFormat.getCurrencyInstance(PL);

    /** Formatuje kwotę jako złotówki, np. "1 234,50 zł". */
    public static String money(double value) { return PLN.format(value); }
}

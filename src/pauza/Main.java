package pauza;

import javax.swing.*;

/** Punkt wejścia aplikacji Pauza. */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "--selftest".equals(args[0])) {
            System.exit(SelfTest.run() ? 0 : 1);
        }

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // domyślny wygląd też zadziała
            }
            UIManager.put("OptionPane.messageFont", Theme.regular(14));
            UIManager.put("OptionPane.buttonFont", Theme.bold(13));
            UIManager.put("TextField.font", Theme.regular(14));
            UIManager.put("Label.font", Theme.regular(14));

            new AppFrame(DataStore.loadDefault()).setVisible(true);
        });
    }
}

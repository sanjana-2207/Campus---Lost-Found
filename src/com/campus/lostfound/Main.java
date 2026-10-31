package com.campus.lostfound;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.ui.LoginFrame;
import com.campus.lostfound.ui.UIStyle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 *
 * <p>Startup order matters:</p>
 * <ol>
 *   <li>installs the look and feel and the shared theme,</li>
 *   <li>fails fast with a readable message if MySQL or the driver is missing,</li>
 *   <li>opens the login window on the Swing event dispatch thread.</li>
 * </ol>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The cross-platform look and feel is an acceptable fallback.
        }
        UIStyle.applyGlobalDefaults();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Creating the singleton verifies db.properties and the driver
                    // before the user sees the login screen.
                    DBConnection.getInstance();
                    new LoginFrame().setVisible(true);
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(null,
                            ex.getMessage(),
                            "Cannot start Campus Lost & Found",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        });
    }
}

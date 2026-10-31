package com.campus.lostfound.util;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Small helper around {@link SwingWorker}.
 *
 * <p>JDBC calls can take hundreds of milliseconds. Running them on the Event
 * Dispatch Thread freezes every button and repaint until MySQL answers, so all
 * database work in this application goes through this utility:</p>
 * <ul>
 *   <li>{@code work} runs on a background thread,</li>
 *   <li>{@code onSuccess} runs on the EDT and may touch Swing components,</li>
 *   <li>{@code onError} also runs on the EDT so it can show a dialog.</li>
 * </ul>
 */
public final class Async {

    private Async() {
        // Utility class - no objects required.
    }

    /** Runs blocking work in the background and delivers the result on the EDT. */
    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess,
                               Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return work.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    onError.accept(ex);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    onError.accept(cause);
                }
            }
        }.execute();
    }

    /** Runs blocking work with no return value. */
    public static void run(Runnable work, Runnable onSuccess, Consumer<Throwable> onError) {
        run(() -> {
            work.run();
            return Boolean.TRUE;
        }, ignored -> onSuccess.run(), onError);
    }

    /** Standard error dialog used by the screens. */
    public static void showError(Component parent, Throwable error, String title) {
        String message = error == null || error.getMessage() == null
                ? "An unexpected error occurred. Please try again."
                : error.getMessage();
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}

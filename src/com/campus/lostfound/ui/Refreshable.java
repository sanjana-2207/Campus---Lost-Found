package com.campus.lostfound.ui;

/**
 * Implemented by every screen that reads from the database.
 *
 * <p>{@link MainFrame} calls {@link #refreshData()} each time a screen becomes
 * visible, so the numbers are always current without the user pressing a
 * refresh button.</p>
 */
public interface Refreshable {

    void refreshData();
}

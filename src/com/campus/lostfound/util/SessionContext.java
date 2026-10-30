package com.campus.lostfound.util;

import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;

/**
 * Holds the user who is currently logged in.
 *
 * <p>This is an application-wide singleton state object: the login screen fills
 * it, and every other screen reads it to decide what the user is allowed to do.
 * It is cleared on logout.</p>
 */
public final class SessionContext {

    private static User currentUser;

    private SessionContext() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** True for STAFF and ADMIN - they may verify claims. */
    public static boolean isVerifier() {
        return currentUser != null
                && (currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN);
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}

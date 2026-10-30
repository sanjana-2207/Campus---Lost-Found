package com.campus.lostfound.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing helper.
 *
 * <p>Passwords are never stored in plain text. The database keeps a 64 character
 * SHA-256 hexadecimal digest, and login compares digests.</p>
 *
 * <p><b>Note for the viva:</b> SHA-256 is used here because it is available in
 * the standard library and keeps the project dependency-free. A production
 * system should use a salted, deliberately slow algorithm such as BCrypt,
 * SCrypt or Argon2.</p>
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Utility class - no objects required.
    }

    /** Returns the lower-case SHA-256 hex digest of the supplied text. */
    public static String hash(String plainText) {
        if (plainText == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is mandatory in every JRE, so this cannot happen.
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /**
     * Simple constant-time comparison helper.
     * Avoids leaking password length through early-exit comparison.
     */
    public static boolean matches(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(plainText).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}

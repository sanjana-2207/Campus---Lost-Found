package com.campus.lostfound.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing helper.
 *
 * <p>New passwords are stored as a salted PBKDF2 digest:</p>
 * <pre>pbkdf2$PBKDF2WithHmacSHA256$120000$&lt;salt base64&gt;$&lt;hash base64&gt;</pre>
 *
 * <p>The iteration count, algorithm and random salt are stored with the hash so
 * the format can be upgraded later without breaking existing accounts. The
 * previous 64-character SHA-256 digests are still accepted on login and are
 * transparently re-hashed by {@code AuthService} after a successful sign-in.</p>
 *
 * <p><b>Note for the viva:</b> a plain SHA-256 digest is fast, so an attacker
 * with a copy of the table can try billions of guesses. PBKDF2 is deliberately
 * slow and salts every password, which defeats pre-computed rainbow tables.</p>
 */
public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2";
    private static final String PREFERRED_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FALLBACK_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Utility class - no objects required.
    }

    /** Returns a new salted PBKDF2 hash for the supplied password. */
    public static String hash(String plainText) {
        if (plainText == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        String algorithm = preferredAlgorithm();
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] digest = derive(plainText.toCharArray(), salt, ITERATIONS, algorithm);

        return PREFIX + "$" + algorithm + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Verifies a password against a stored PBKDF2 hash or a legacy SHA-256
     * digest. The comparison is constant-time in both cases.
     */
    public static boolean matches(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) {
            return false;
        }
        if (isLegacySha256(storedHash)) {
            return MessageDigest.isEqual(
                    sha256(plainText).getBytes(StandardCharsets.UTF_8),
                    storedHash.getBytes(StandardCharsets.UTF_8));
        }
        if (!storedHash.startsWith(PREFIX + "$")) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 5) {
            return false;
        }
        try {
            String algorithm = parts[1];
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            byte[] actual = derive(plainText.toCharArray(), salt, iterations, algorithm);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            // Malformed hash in the database: fail closed.
            return false;
        }
    }

    /**
     * True when a stored hash uses the old SHA-256 format or a weaker iteration
     * count, so the caller can upgrade it after a successful login.
     */
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null || isLegacySha256(storedHash)) {
            return true;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 5 || !PREFIX.equals(parts[0])) {
            return true;
        }
        try {
            return !PREFERRED_ALGORITHM.equals(parts[1])
                    || Integer.parseInt(parts[2]) < ITERATIONS;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    // ------------------------------------------------------------------
    //  Internals
    // ------------------------------------------------------------------

    private static boolean isLegacySha256(String storedHash) {
        return storedHash.length() == 64 && storedHash.matches("[0-9a-fA-F]{64}");
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations,
                                 String algorithm) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(algorithm + " is not available", ex);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Could not derive the password hash", ex);
        } finally {
            spec.clearPassword();
        }
    }

    /** Uses SHA-256 when the JRE supports it, otherwise SHA-1. */
    private static String preferredAlgorithm() {
        try {
            SecretKeyFactory.getInstance(PREFERRED_ALGORITHM);
            return PREFERRED_ALGORITHM;
        } catch (NoSuchAlgorithmException ex) {
            return FALLBACK_ALGORITHM;
        }
    }

    /** Legacy digest kept only for backwards-compatible login. */
    private static String sha256(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}

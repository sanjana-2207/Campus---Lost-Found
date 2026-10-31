package com.campus.lostfound.service;

import com.campus.lostfound.dao.JdbcUserDAO;
import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.PasswordUtil;

/**
 * Authentication and registration rules.
 *
 * <p>The service layer is the only place that knows how a password is hashed or
 * which validations a registration must pass. The Swing screens only collect
 * text and display the resulting message.</p>
 */
public class AuthService {

    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_EMAIL_LENGTH = 120;
    private static final int MAX_PHONE_LENGTH = 15;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private final UserDAO userDAO;

    public AuthService() {
        this(new JdbcUserDAO());
    }

    /** Constructor injection makes the service testable with a fake DAO. */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Validates the credentials and returns the logged-in user.
     *
     * @throws ValidationException when the e-mail is unknown or the password is wrong
     */
    public User login(String email, String plainPassword) {
        if (isBlank(email) || isBlank(plainPassword)) {
            throw new ValidationException("Enter both your e-mail address and password.");
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null || !PasswordUtil.matches(plainPassword, user.getPasswordHash())) {
            // Same message for both cases: never reveal which accounts exist.
            throw new ValidationException("Invalid e-mail address or password.");
        }

        // Upgrade a legacy SHA-256 digest (or an old iteration count) now that
        // the plain password is available. A failure here must not block login.
        if (PasswordUtil.needsRehash(user.getPasswordHash())) {
            try {
                String upgraded = PasswordUtil.hash(plainPassword);
                userDAO.updatePasswordHash(user.getUserId(), upgraded);
                user.setPasswordHash(upgraded);
            } catch (RuntimeException ignored) {
                // The old hash still works; the upgrade can be retried next time.
            }
        }
        return user;
    }

    /**
     * Creates a student account. Staff accounts are provisioned by the
     * administrator and administrators only exist through the database seed, so
     * self-registration can never escalate privileges.
     */
    public User register(String fullName, String email, String phone,
                         String plainPassword, String confirmPassword, Role role) {

        if (isBlank(fullName) || fullName.trim().length() < 3) {
            throw new ValidationException("Enter your full name (at least 3 characters).");
        }
        if (fullName.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Your full name must be at most "
                    + MAX_NAME_LENGTH + " characters.");
        }
        if (isBlank(email) || !email.trim().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            throw new ValidationException("Enter a valid e-mail address, for example name@campus.edu.");
        }
        if (email.trim().length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Your e-mail address must be at most "
                    + MAX_EMAIL_LENGTH + " characters.");
        }
        if (isBlank(phone) || !phone.replaceAll("[^0-9]", "").matches("\\d{10,12}")) {
            throw new ValidationException("Enter a valid phone number with 10 to 12 digits.");
        }
        if (phone.trim().length() > MAX_PHONE_LENGTH) {
            throw new ValidationException("Your phone number must be at most "
                    + MAX_PHONE_LENGTH + " characters.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new ValidationException("Choose a password with at least 6 characters.");
        }
        if (plainPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException("Choose a password with at most "
                    + MAX_PASSWORD_LENGTH + " characters.");
        }
        if (!plainPassword.equals(confirmPassword)) {
            throw new ValidationException("The two passwords do not match.");
        }
        if (role != Role.STUDENT) {
            throw new ValidationException("Student accounts can be created here. "
                    + "Staff accounts are issued by the lost-and-found desk.");
        }

        String normalisedEmail = email.trim().toLowerCase();
        if (userDAO.emailExists(normalisedEmail)) {
            throw new ValidationException("That e-mail address is already registered. Please sign in.");
        }

        User user = User.newUser(fullName.trim(), normalisedEmail, phone.trim(),
                PasswordUtil.hash(plainPassword), Role.STUDENT);
        userDAO.insert(user);
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.campus.web.service;

import com.campus.web.model.Role;
import com.campus.web.model.User;
import com.campus.web.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return users.findByEmailIgnoreCase(authentication.getName()).orElse(null);
    }

    @Transactional
    public User register(String fullName, String email, String phone,
                         String password, String confirm) {
        String name = clean(fullName);
        String address = clean(email).toLowerCase(Locale.ROOT);
        String number = clean(phone);

        if (name.length() < 3) {
            throw new IllegalArgumentException("Enter your full name.");
        }
        if (!address.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Enter a valid e-mail address.");
        }
        if (number.length() < 10) {
            throw new IllegalArgumentException("Enter a phone number with at least 10 digits.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Use a password with at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("The two passwords do not match.");
        }
        if (users.existsByEmailIgnoreCase(address)) {
            throw new IllegalArgumentException("An account with this e-mail already exists.");
        }

        User user = new User();
        user.setFullName(name);
        user.setEmail(address);
        user.setPhone(number);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(Role.STUDENT);
        return users.save(user);
    }

    @Transactional
    public void updateProfile(User user, String fullName, String phone, String gender) {
        String name = clean(fullName);
        String number = clean(phone);
        if (name.length() < 3) {
            throw new IllegalArgumentException("Enter your full name.");
        }
        if (number.length() < 10) {
            throw new IllegalArgumentException("Enter a phone number with at least 10 digits.");
        }
        user.setFullName(name);
        user.setPhone(number);
        user.setGender(clean(gender));
        users.save(user);
    }

    @Transactional
    public void changePassword(User user, String current, String next, String confirm) {
        if (current == null || !encoder.matches(current, user.getPasswordHash())) {
            throw new IllegalArgumentException("The current password is incorrect.");
        }
        if (next == null || next.length() < 6) {
            throw new IllegalArgumentException("Use a new password with at least 6 characters.");
        }
        if (!next.equals(confirm)) {
            throw new IllegalArgumentException("The two new passwords do not match.");
        }
        user.setPasswordHash(encoder.encode(next));
        users.save(user);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

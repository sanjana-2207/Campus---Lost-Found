package com.campus.lostfound.model;

import java.sql.Timestamp;

/**
 * A campus user.
 *
 * <p><b>Encapsulation:</b> every field is private and can only be changed
 * through a setter, so the object can never be left in an invalid state by
 * direct field access from another class.</p>
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private Role role = Role.STUDENT;
    private Timestamp createdAt;

    public User() {
    }

    public User(int userId, String fullName, String email, String phone, Role role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    /** Convenience factory used by the registration screen. */
    public static User newUser(String fullName, String email, String phone,
                               String passwordHash, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName;
    }
}

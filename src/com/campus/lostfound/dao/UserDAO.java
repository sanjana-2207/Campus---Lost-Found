package com.campus.lostfound.dao;

import com.campus.lostfound.model.User;

import java.util.List;

/** Data access contract for campus users. */
public interface UserDAO extends GenericDAO<User> {

    /** Case-insensitive e-mail lookup used by the login screen. */
    User findByEmail(String email);

    /** True when the e-mail is already registered. */
    boolean emailExists(String email);

    List<User> findAllByRole(String roleName);
}

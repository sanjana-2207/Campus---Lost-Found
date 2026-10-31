package com.campus.lostfound.dao;

import java.util.List;

/**
 * Generic CRUD contract shared by every DAO.
 *
 * <p><b>Abstraction:</b> the service layer programs against this interface, not
 * against JDBC. Swapping MySQL for another database only requires a new
 * implementation.</p>
 *
 * <p><b>Polymorphism:</b> {@code GenericDAO<Item>} and {@code GenericDAO<User>}
 * are two different types produced from one declaration (generics).</p>
 *
 * @param <T> the entity type handled by the DAO
 */
public interface GenericDAO<T> {

    /** Returns the entity with the given primary key, or {@code null}. */
    T findById(int id);

    /** Returns every row, ordered by the natural display order of the entity. */
    List<T> findAll();

    /** Inserts a new entity and returns the generated primary key. */
    int insert(T entity);

    /** Updates the mutable columns of an existing entity. */
    boolean update(T entity);

    /** Deletes the entity; children are removed by ON DELETE CASCADE. */
    boolean delete(int id);
}

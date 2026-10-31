package com.campus.lostfound.dao;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchResult;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/** Data access contract for item reports. */
public interface ItemDAO extends GenericDAO<Item> {

    /** Runs the search screen query with any combination of filters. */
    List<Item> search(ItemFilter filter);

    /** Newest N reports, used by the dashboard. */
    List<Item> findRecent(int limit);

    /** Reports created by one user. */
    List<Item> findByReporter(int userId);

    /** Standalone status change (own transaction). */
    boolean updateStatus(int itemId, ItemStatus status);

    /**
     * Locks one item row for the duration of a caller-managed transaction.
     * Returns {@code null} when the item no longer exists.
     */
    Item findByIdForUpdate(Connection connection, int itemId);

    /**
     * Status change inside a caller-managed transaction, guarded by the status
     * the caller read earlier. A zero return value means the item changed
     * underneath the workflow (optimistic lock).
     */
    boolean updateStatus(Connection connection, int itemId, ItemStatus expectedStatus,
                         ItemStatus newStatus);

    /** Dashboard counters, every status present (zero when empty). */
    Map<ItemStatus, Integer> countByStatus();

    /** Number of reports of one type (null counts every row). */
    int countByType(ItemType type);

    /** Category name -> number of reports, optionally filtered by type. */
    Map<String, Integer> countByCategory(ItemType type);

    /** Location name -> number of reports. */
    Map<String, Integer> countByLocation();

    /** "yyyy-MM" -> number of reports, optionally filtered by type. */
    Map<String, Integer> countByMonth(ItemType type);

    /** Pairs produced by the SQL matching view. */
    List<MatchResult> findPotentialMatches();
}

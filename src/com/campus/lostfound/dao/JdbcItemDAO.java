package com.campus.lostfound.dao;

import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.MatchResult;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.Urgency;
import com.campus.lostfound.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link ItemDAO}.
 *
 * <p>Every read uses the same projection (base table + lookup tables + subtype
 * table) so a single {@link #mapRow(ResultSet)} method can rebuild the correct
 * sub-class. That is the Java side of the class-table inheritance design.</p>
 */
public class JdbcItemDAO extends AbstractJdbcDAO<Item> implements ItemDAO {

    private static final String BASE_SELECT =
            "SELECT i.item_id, i.item_type, i.title, i.description, i.status, "
          + "       i.event_date, i.created_at, "
          + "       c.category_id, c.name AS category_name, c.description AS category_desc, "
          + "       l.location_id, l.name AS location_name, l.building, "
          + "       u.user_id AS reporter_id, u.full_name AS reporter_name, "
          + "       u.email AS reporter_email, u.phone AS reporter_phone, "
          + "       u.role AS reporter_role, "
          + "       li.contact, li.reward_offered, li.urgency, "
          + "       fi.storage_location, fi.handed_to_security, fi.finder_notes "
          + "FROM items i "
          + "JOIN categories c    ON c.category_id = i.category_id "
          + "JOIN locations  l    ON l.location_id = i.location_id "
          + "JOIN users      u    ON u.user_id     = i.reporter_id "
          + "LEFT JOIN lost_items  li ON li.item_id = i.item_id "
          + "LEFT JOIN found_items fi ON fi.item_id = i.item_id ";

    // ==================================================================
    //  Reads
    // ==================================================================

    @Override
    public Item findById(int id) {
        String sql = BASE_SELECT + "WHERE i.item_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap("Could not load item #" + id, ex);
        }
    }

    @Override
    public List<Item> findAll() {
        return search(new ItemFilter());
    }

    @Override
    public List<Item> search(ItemFilter filter) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1 = 1 ");
        List<Object> parameters = new ArrayList<Object>();

        if (filter != null) {
            if (filter.getItemType() != null) {
                sql.append("AND i.item_type = ? ");
                parameters.add(filter.getItemType().name());
            }
            if (filter.getStatus() != null) {
                sql.append("AND i.status = ? ");
                parameters.add(filter.getStatus().name());
            }
            if (filter.getCategoryId() != null) {
                sql.append("AND i.category_id = ? ");
                parameters.add(filter.getCategoryId());
            }
            if (filter.getLocationId() != null) {
                sql.append("AND i.location_id = ? ");
                parameters.add(filter.getLocationId());
            }
            if (filter.getReporterId() != null) {
                sql.append("AND i.reporter_id = ? ");
                parameters.add(filter.getReporterId());
            }
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                // Escape LIKE wildcards so a user typing "%" or "_" searches for
                // those characters instead of matching every row.
                sql.append("AND (i.title LIKE ? ESCAPE '!' OR i.description LIKE ? ESCAPE '!') ");
                String like = "%" + escapeLike(filter.getKeyword().trim()) + "%";
                parameters.add(like);
                parameters.add(like);
            }
            if (filter.getFromDate() != null) {
                sql.append("AND i.event_date >= ? ");
                parameters.add(filter.getFromDate());
            }
            if (filter.getToDate() != null) {
                sql.append("AND i.event_date <= ? ");
                parameters.add(filter.getToDate());
            }
        }

        sql.append("ORDER BY i.event_date DESC, i.item_id DESC");

        List<Item> items = new ArrayList<Item>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bind(statement, parameters);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw wrap("Could not run the item search", ex);
        }
    }

    @Override
    public List<Item> findRecent(int limit) {
        String sql = BASE_SELECT + "ORDER BY i.created_at DESC, i.item_id DESC LIMIT ?";
        List<Item> items = new ArrayList<Item>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw wrap("Could not load recent reports", ex);
        }
    }

    @Override
    public List<Item> findByReporter(int userId) {
        return search(new ItemFilter().setReporterId(userId));
    }

    @Override
    public List<MatchResult> findPotentialMatches() {
        String sql = "SELECT lost_item_id, lost_title, found_item_id, found_title, "
                   + "       category_name, location_name, lost_date, found_date, "
                   + "       days_apart, match_score "
                   + "FROM vw_potential_matches";

        List<MatchResult> matches = new ArrayList<MatchResult>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                MatchResult match = new MatchResult();
                match.setLostItemId(rs.getInt("lost_item_id"));
                match.setLostTitle(rs.getString("lost_title"));
                match.setFoundItemId(rs.getInt("found_item_id"));
                match.setFoundTitle(rs.getString("found_title"));
                match.setCategoryName(rs.getString("category_name"));
                match.setLocationName(rs.getString("location_name"));
                match.setLostDate(toLocalDate(rs.getDate("lost_date")));
                match.setFoundDate(toLocalDate(rs.getDate("found_date")));
                match.setDaysApart(rs.getInt("days_apart"));
                match.setScore(rs.getInt("match_score"));
                matches.add(match);
            }
            return matches;
        } catch (SQLException ex) {
            throw wrap("Could not run the matching engine", ex);
        }
    }

    // ==================================================================
    //  Writes
    // ==================================================================

    @Override
    public int insert(Item item) {
        String sql = "INSERT INTO items "
                   + "(item_type, title, description, category_id, location_id, "
                   + " event_date, reporter_id, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            int itemId;
            try (PreparedStatement statement =
                         connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, item.getItemType().name());
                statement.setString(2, item.getTitle());
                statement.setString(3, item.getDescription());
                statement.setInt(4, item.getCategory().getCategoryId());
                statement.setInt(5, item.getLocation().getLocationId());
                statement.setDate(6, java.sql.Date.valueOf(item.getEventDate()));
                statement.setInt(7, item.getReportedBy().getUserId());
                statement.setString(8, item.getStatus().name());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("The database did not return a generated item id");
                    }
                    itemId = keys.getInt(1);
                }
            }

            insertSubtypeRow(connection, item, itemId);
            connection.commit();
            item.setItemId(itemId);
            return itemId;

        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw wrap("Could not save the item report", ex);
        } catch (RuntimeException ex) {
            // A DataAccessException from insertSubtypeRow() must never leave a
            // half-written item behind.
            rollbackQuietly(connection);
            throw ex;
        } finally {
            closeTransaction(connection);
        }
    }

    @Override
    public boolean update(Item item) {
        String sql = "UPDATE items SET title = ?, description = ?, category_id = ?, "
                   + "location_id = ?, event_date = ?, status = ? WHERE item_id = ?";

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            int rows;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, item.getTitle());
                statement.setString(2, item.getDescription());
                statement.setInt(3, item.getCategory().getCategoryId());
                statement.setInt(4, item.getLocation().getLocationId());
                statement.setDate(5, java.sql.Date.valueOf(item.getEventDate()));
                statement.setString(6, item.getStatus().name());
                statement.setInt(7, item.getItemId());
                rows = statement.executeUpdate();
            }

            if (rows == 0) {
                connection.rollback();
                return false;
            }
            updateSubtypeRow(connection, item);
            connection.commit();
            return true;

        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw wrap("Could not update the item report", ex);
        } catch (RuntimeException ex) {
            rollbackQuietly(connection);
            throw ex;
        } finally {
            closeTransaction(connection);
        }
    }

    @Override
    public boolean updateStatus(int itemId, ItemStatus status) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, itemId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not change the item status", ex);
        }
    }

    @Override
    public Item findByIdForUpdate(Connection connection, int itemId) {
        String sql = BASE_SELECT + "WHERE i.item_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap("Could not lock the item report", ex);
        }
    }

    @Override
    public boolean updateStatus(Connection connection, int itemId,
                                ItemStatus expectedStatus, ItemStatus newStatus) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.name());
            statement.setInt(2, itemId);
            statement.setString(3, expectedStatus.name());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not change the item status", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not delete the item report", ex);
        }
    }

    // ==================================================================
    //  Aggregates for the dashboard and reports
    // ==================================================================

    @Override
    public Map<ItemStatus, Integer> countByStatus() {
        Map<ItemStatus, Integer> counts = new EnumMap<ItemStatus, Integer>(ItemStatus.class);
        for (ItemStatus status : ItemStatus.values()) {
            counts.put(status, 0);
        }

        String sql = "SELECT status, COUNT(*) AS total FROM items GROUP BY status";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                counts.put(ItemStatus.valueOf(rs.getString("status")), rs.getInt("total"));
            }
            return counts;
        } catch (SQLException ex) {
            throw wrap("Could not count items by status", ex);
        }
    }

    @Override
    public int countByType(ItemType type) {
        String sql = "SELECT COUNT(*) FROM items "
                   + (type == null ? "" : "WHERE item_type = ?");
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (type != null) {
                statement.setString(1, type.name());
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw wrap("Could not count the reports", ex);
        }
    }

    @Override
    public Map<String, Integer> countByCategory(ItemType type) {
        String sql = "SELECT c.name AS label, COUNT(*) AS total "
                   + "FROM items i JOIN categories c ON c.category_id = i.category_id "
                   + (type == null ? "" : "WHERE i.item_type = ? ")
                   + "GROUP BY c.name ORDER BY total DESC, c.name";

        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (type != null) {
                statement.setString(1, type.name());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("label"), rs.getInt("total"));
                }
            }
            return counts;
        } catch (SQLException ex) {
            throw wrap("Could not count items by category", ex);
        }
    }

    @Override
    public Map<String, Integer> countByLocation() {
        String sql = "SELECT l.name AS label, COUNT(*) AS total "
                   + "FROM items i JOIN locations l ON l.location_id = i.location_id "
                   + "GROUP BY l.name ORDER BY total DESC, l.name";

        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("label"), rs.getInt("total"));
            }
            return counts;
        } catch (SQLException ex) {
            throw wrap("Could not count items by location", ex);
        }
    }

    @Override
    public Map<String, Integer> countByMonth(ItemType type) {
        String sql = "SELECT DATE_FORMAT(event_date, '%Y-%m') AS ym, COUNT(*) AS total "
                   + "FROM items "
                   + (type == null ? "" : "WHERE item_type = ? ")
                   + "GROUP BY ym ORDER BY ym";

        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (type != null) {
                statement.setString(1, type.name());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("ym"), rs.getInt("total"));
                }
            }
            return counts;
        } catch (SQLException ex) {
            throw wrap("Could not build the monthly trend", ex);
        }
    }

    // ==================================================================
    //  Row mapping and helpers
    // ==================================================================

    private Item mapRow(ResultSet rs) throws SQLException {
        ItemType type = ItemType.valueOf(rs.getString("item_type"));
        Item item;

        if (type == ItemType.LOST) {
            LostItem lost = new LostItem();
            lost.setContact(rs.getString("contact"));
            lost.setRewardOffered(rs.getBigDecimal("reward_offered"));
            String urgency = rs.getString("urgency");
            lost.setUrgency(urgency == null ? Urgency.MEDIUM : Urgency.valueOf(urgency));
            item = lost;
        } else {
            FoundItem found = new FoundItem();
            found.setStorageLocation(rs.getString("storage_location"));
            found.setHandedToSecurity(rs.getBoolean("handed_to_security"));
            found.setFinderNotes(rs.getString("finder_notes"));
            item = found;
        }

        item.setItemId(rs.getInt("item_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setStatus(ItemStatus.valueOf(rs.getString("status")));
        item.setEventDate(toLocalDate(rs.getDate("event_date")));
        item.setCreatedAt(rs.getTimestamp("created_at"));

        Category category = new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("category_desc"));
        item.setCategory(category);

        Location location = new Location(
                rs.getInt("location_id"),
                rs.getString("location_name"),
                rs.getString("building"));
        item.setLocation(location);

        User reporter = new User(
                rs.getInt("reporter_id"),
                rs.getString("reporter_name"),
                rs.getString("reporter_email"),
                rs.getString("reporter_phone"),
                Role.valueOf(rs.getString("reporter_role")));
        item.setReportedBy(reporter);

        return item;
    }

    private void insertSubtypeRow(Connection connection, Item item, int itemId)
            throws SQLException {

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            String sql = "INSERT INTO lost_items (item_id, contact, reward_offered, urgency) "
                       + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, itemId);
                statement.setString(2, lost.getContact());
                statement.setBigDecimal(3, lost.getRewardOffered());
                statement.setString(4, lost.getUrgency().name());
                statement.executeUpdate();
            }
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            String sql = "INSERT INTO found_items "
                       + "(item_id, storage_location, handed_to_security, finder_notes) "
                       + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, itemId);
                statement.setString(2, found.getStorageLocation());
                statement.setBoolean(3, found.isHandedToSecurity());
                statement.setString(4, found.getFinderNotes());
                statement.executeUpdate();
            }
        }
    }

    private void updateSubtypeRow(Connection connection, Item item) throws SQLException {
        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            String sql = "UPDATE lost_items SET contact = ?, reward_offered = ?, urgency = ? "
                       + "WHERE item_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, lost.getContact());
                statement.setBigDecimal(2, lost.getRewardOffered());
                statement.setString(3, lost.getUrgency().name());
                statement.setInt(4, item.getItemId());
                statement.executeUpdate();
            }
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            String sql = "UPDATE found_items SET storage_location = ?, "
                       + "handed_to_security = ?, finder_notes = ? WHERE item_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, found.getStorageLocation());
                statement.setBoolean(2, found.isHandedToSecurity());
                statement.setString(3, found.getFinderNotes());
                statement.setInt(4, item.getItemId());
                statement.executeUpdate();
            }
        }
    }

    /** Closes the connection used for a transaction without committing it. */
    private void closeTransaction(Connection connection) {
        if (connection != null) {
            // Do NOT set auto-commit back to true here: on an unchecked failure
            // that would commit the partial transaction. The pool resets the
            // connection (rolling back if needed) when it is returned.
            closeQuietly(connection);
        }
    }

    /** Escapes LIKE metacharacters so user input is matched literally. */
    private static String escapeLike(String value) {
        return value.replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");
    }
}

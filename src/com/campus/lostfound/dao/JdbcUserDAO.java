package com.campus.lostfound.dao;

import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link UserDAO}. */
public class JdbcUserDAO extends AbstractJdbcDAO<User> implements UserDAO {

    private static final String BASE_SELECT =
            "SELECT user_id, full_name, email, phone, password_hash, role, created_at "
          + "FROM users ";

    @Override
    public User findById(int id) {
        String sql = BASE_SELECT + "WHERE user_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap("Could not load user #" + id, ex);
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = BASE_SELECT + "WHERE email = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap("Could not load the user account", ex);
        }
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw wrap("Could not check the e-mail address", ex);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = BASE_SELECT + "ORDER BY full_name";
        List<User> users = new ArrayList<User>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw wrap("Could not load users", ex);
        }
    }

    @Override
    public List<User> findAllByRole(String roleName) {
        String sql = BASE_SELECT + "WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<User>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, roleName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
            return users;
        } catch (SQLException ex) {
            throw wrap("Could not load users by role", ex);
        }
    }

    @Override
    public boolean updatePasswordHash(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not update the password", ex);
        }
    }

    @Override
    public int insert(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, user.getRole().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The database did not return a generated user id");
                }
                user.setUserId(keys.getInt(1));
            }
            return user.getUserId();

        } catch (SQLException ex) {
            throw wrap("Could not create the account", ex);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role = ? "
                   + "WHERE user_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole().name());
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not update the account", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not delete the account", ex);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}

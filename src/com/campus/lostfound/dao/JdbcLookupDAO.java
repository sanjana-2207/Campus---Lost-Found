package com.campus.lostfound.dao;

import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of the read-only {@link LookupDAO}. */
public class JdbcLookupDAO extends AbstractJdbcDAO<Category> implements LookupDAO {

    @Override
    public List<Category> findAllCategories() {
        String sql = "SELECT category_id, name, description FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<Category>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")));
            }
            return categories;
        } catch (SQLException ex) {
            throw wrap("Could not load categories", ex);
        }
    }

    @Override
    public List<Location> findAllLocations() {
        String sql = "SELECT location_id, name, building FROM locations ORDER BY name";
        List<Location> locations = new ArrayList<Location>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                locations.add(new Location(
                        rs.getInt("location_id"),
                        rs.getString("name"),
                        rs.getString("building")));
            }
            return locations;
        } catch (SQLException ex) {
            throw wrap("Could not load locations", ex);
        }
    }

    // Lookup tables are maintained directly in MySQL, so the generic CRUD
    // methods are not used by the application.

    @Override
    public Category findById(int id) {
        throw new UnsupportedOperationException("Lookup tables are read-only in this application");
    }

    @Override
    public List<Category> findAll() {
        return findAllCategories();
    }

    @Override
    public int insert(Category entity) {
        throw new UnsupportedOperationException("Lookup tables are read-only in this application");
    }

    @Override
    public boolean update(Category entity) {
        throw new UnsupportedOperationException("Lookup tables are read-only in this application");
    }

    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException("Lookup tables are read-only in this application");
    }
}

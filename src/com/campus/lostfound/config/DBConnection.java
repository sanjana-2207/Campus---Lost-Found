package com.campus.lostfound.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection factory.
 *
 * <p><b>Design patterns demonstrated:</b></p>
 * <ul>
 *   <li><b>Singleton</b> - exactly one configuration object exists for the JVM.</li>
 *   <li><b>Encapsulation</b> - the URL, user and password are private fields that
 *       callers can never read or modify; only {@link #getConnection()} is exposed.</li>
 *   <li><b>Abstraction</b> - the rest of the application asks for a
 *       {@link java.sql.Connection} and never touches DriverManager or the
 *       properties file.</li>
 * </ul>
 *
 * <p>A <i>new</i> physical connection is returned per call (not a shared one) so
 * that the claim workflow can safely use transactions without one screen
 * interfering with another.</p>
 */
public final class DBConnection {

    private static DBConnection instance;

    private final String url;
    private final String userName;
    private final String password;

    private DBConnection() {
        Properties properties = new Properties();
        try (InputStream input =
                     DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "db.properties was not found on the classpath. "
                        + "Make sure the resources folder is copied to out/ when compiling.");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read db.properties", ex);
        }

        this.url = properties.getProperty("db.url");
        this.userName = properties.getProperty("db.user");
        this.password = properties.getProperty("db.password");

        if (url == null || userName == null || password == null) {
            throw new IllegalStateException(
                    "db.properties must define db.url, db.user and db.password");
        }

        try {
            // JDBC 4 auto-loading usually handles this, but loading explicitly
            // produces a clear error message when the driver jar is missing.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "MySQL Connector/J not found. Copy mysql-connector-j-*.jar into the lib folder.",
                    ex);
        }
    }

    /** Lazily creates and returns the single configuration instance. */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /** Opens a fresh physical connection. Always close it with try-with-resources. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, userName, password);
    }

    /** Closes any JDBC resource without throwing (used in finally blocks). */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
                // Nothing useful can be done here.
            }
        }
    }
}

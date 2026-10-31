package com.campus.lostfound.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;

/**
 * Central JDBC connection factory and a small connection pool.
 *
 * <p><b>Design patterns demonstrated:</b></p>
 * <ul>
 *   <li><b>Singleton</b> - exactly one configuration object exists for the JVM.</li>
 *   <li><b>Object pool</b> - physical connections are reused instead of opening
 *       a new TCP connection for every query. This matters on the reports screen,
 *       which runs several aggregates in a row.</li>
 *   <li><b>Proxy</b> - callers receive a {@link Connection} proxy. Calling
 *       {@code close()} returns it to the pool instead of closing the socket, so
 *       every existing try-with-resources block keeps working unchanged.</li>
 * </ul>
 *
 * <p>Connection settings come from {@code resources/db.properties} and can be
 * overridden with the {@code CAMPUS_DB_URL}, {@code CAMPUS_DB_USER} and
 * {@code CAMPUS_DB_PASSWORD} environment variables (useful on a shared machine
 * or in CI where the password must not be committed).</p>
 */
public final class DBConnection {

    private static final int MAX_POOL_SIZE = 8;
    private static final long BORROW_TIMEOUT_MS = 10_000L;
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    private static DBConnection instance;

    private final String url;
    private final String userName;
    private final String password;

    private final Deque<Connection> idle = new ArrayDeque<Connection>();
    private int totalConnections;

    private DBConnection() {
        Properties properties = new Properties();
        try (InputStream input =
                     DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read db.properties", ex);
        }

        this.url = setting("CAMPUS_DB_URL", properties.getProperty("db.url"));
        this.userName = setting("CAMPUS_DB_USER", properties.getProperty("db.user"));
        String configuredPassword = setting("CAMPUS_DB_PASSWORD",
                properties.getProperty("db.password"));
        this.password = configuredPassword == null ? "" : configuredPassword;

        if (url == null || userName == null) {
            throw new IllegalStateException(
                    "Database settings are missing. Create resources/db.properties from "
                    + "db.properties.example, or set CAMPUS_DB_URL and CAMPUS_DB_USER.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "MySQL Connector/J not found. Copy mysql-connector-j-*.jar into the lib folder.",
                    ex);
        }
        DriverManager.setLoginTimeout(CONNECT_TIMEOUT_MS / 1000);
    }

    /** Lazily creates and returns the single configuration instance. */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Borrows a pooled connection. The caller must still close it (preferably
     * with try-with-resources); closing returns it to the pool.
     */
    public synchronized Connection getConnection() throws SQLException {
        long deadline = System.currentTimeMillis() + BORROW_TIMEOUT_MS;

        while (true) {
            Connection physical = idle.pollFirst();
            if (physical != null) {
                if (isUsable(physical)) {
                    return wrap(physical);
                }
                discard(physical);
                continue;
            }

            if (totalConnections < MAX_POOL_SIZE) {
                Connection created = createPhysicalConnection();
                totalConnections++;
                return wrap(created);
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                throw new SQLException(
                        "Timed out waiting for a free database connection. Please try again.");
            }
            try {
                wait(Math.min(remaining, 500L));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for a database connection", ex);
            }
        }
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

    /** Closes every idle physical connection (called on application exit). */
    public synchronized void shutdown() {
        for (Connection connection : idle) {
            closePhysical(connection);
        }
        idle.clear();
        totalConnections = 0;
    }

    // ------------------------------------------------------------------
    //  Pool internals
    // ------------------------------------------------------------------

    private Connection createPhysicalConnection() throws SQLException {
        Properties info = new Properties();
        info.setProperty("user", userName);
        info.setProperty("password", password);
        info.setProperty("connectTimeout", String.valueOf(CONNECT_TIMEOUT_MS));
        info.setProperty("socketTimeout", String.valueOf(SOCKET_TIMEOUT_MS));
        info.setProperty("useSSL", "false");
        info.setProperty("allowPublicKeyRetrieval", "true");
        info.setProperty("serverTimezone", "UTC");
        return DriverManager.getConnection(url, info);
    }

    private Connection wrap(Connection physical) {
        return (Connection) Proxy.newProxyInstance(
                DBConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(physical));
    }

    private boolean isUsable(Connection connection) {
        try {
            return !connection.isClosed() && connection.isValid(2);
        } catch (SQLException ex) {
            return false;
        }
    }

    private synchronized void release(Connection physical) {
        try {
            // A connection returned mid-transaction must never keep the partial
            // work, and the next borrower needs the default auto-commit state.
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
            if (physical.isClosed()) {
                totalConnections--;
                notifyAll();
                return;
            }
        } catch (SQLException ex) {
            discard(physical);
            return;
        }

        idle.addLast(physical);
        notifyAll();
    }

    private synchronized void discard(Connection physical) {
        closePhysical(physical);
        totalConnections = Math.max(0, totalConnections - 1);
        notifyAll();
    }

    private void closePhysical(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // Nothing else to do.
        }
    }

    private static String setting(String environmentKey, String propertyValue) {
        String fromEnvironment = System.getenv(environmentKey);
        if (fromEnvironment != null && !fromEnvironment.trim().isEmpty()) {
            return fromEnvironment.trim();
        }
        return propertyValue == null ? null : propertyValue.trim();
    }

    /** Returns the physical connection to the pool exactly once. */
    private final class PooledConnectionHandler implements InvocationHandler {

        private final Connection physical;
        private boolean closed;

        private PooledConnectionHandler(Connection physical) {
            this.physical = physical;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("close".equals(name)) {
                releaseOnce();
                return null;
            }
            if ("isClosed".equals(name)) {
                return closed || physical.isClosed();
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(name)) {
                return "PooledConnection[" + physical + "]";
            }
            if (closed) {
                throw new SQLException("Connection is already closed");
            }

            try {
                return method.invoke(physical, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }

        private synchronized void releaseOnce() {
            if (!closed) {
                closed = true;
                release(physical);
            }
        }
    }
}

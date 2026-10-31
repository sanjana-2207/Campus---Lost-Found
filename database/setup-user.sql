-- ============================================================================
--  Least-privilege MySQL account for the application
--  Run as root AFTER importing database/schema.sql:
--      mysql -u root -p < database/setup-user.sql
--
--  The application only needs SELECT / INSERT / UPDATE / DELETE. It must not
--  be able to drop tables, create users or read other databases, so the root
--  account is used only for installation.
-- ============================================================================

CREATE USER IF NOT EXISTS 'campus_app'@'localhost'
    IDENTIFIED BY 'campus_app_pw';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON campus_lost_found.*
    TO 'campus_app'@'localhost';

FLUSH PRIVILEGES;

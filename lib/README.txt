MySQL JDBC driver (Connector/J) -- INCLUDED
============================================

This folder already contains:

    mysql-connector-j-8.4.0.jar     (MySQL Connector/J 8.4.0, LTS)

Nothing needs to be downloaded to compile or run the project. The batch
scripts use the classpath entry "lib/*", so any jar placed here is picked up
automatically.

If you ever replace it, download Connector/J from:
    https://dev.mysql.com/downloads/connector/j/

or with Maven:

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.4.0</version>
    </dependency>

The jar is only required for compiling (the driver class is referenced) and
for running against MySQL. It is not part of the Java source submission.

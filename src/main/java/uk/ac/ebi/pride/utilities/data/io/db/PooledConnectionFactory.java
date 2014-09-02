package uk.ac.ebi.pride.utilities.data.io.db;

//~--- non-JDK imports --------------------------------------------------------

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

//~--- JDK imports ------------------------------------------------------------

/**
 * PooledConnectionFactory is a singleton which manages a database connection pool
 */
public class PooledConnectionFactory {

    /**
     * Database property file which contains all the database connection settings
     */
    private static final String DATABASE_PROP_FILE = "prop/database.prop";
    private static final Logger logger             = LoggerFactory.getLogger(PooledConnectionFactory.class);

    /**
     * Singleton instance
     */
    private static final PooledConnectionFactory instance = new PooledConnectionFactory();

    /**
     * Database connection pool
     */
    private ComboPooledDataSource connectionPool = null;

    /**
     * Database properties
     */
    private final Properties dbProperties;

    /**
     * Build a connection factory
     */
    private PooledConnectionFactory() {
        dbProperties = new Properties();

        try {
            dbProperties.load(this.getClass().getClassLoader().getResourceAsStream(DATABASE_PROP_FILE));
        } catch (IOException e) {
            String msg = "Failed to load database connection properties";

            logger.error(msg, e);

            throw new IllegalStateException(msg + ": " + e.getMessage());
        }

        try {

            // retrieve the active schema from the master schema
            String schema = getActiveSchema();

            logger.info("Using PRIDE public active schema: " + schema);

            // create a new connection pool
            setupConnectionPool(schema);
        } catch (PropertyVetoException e) {
            String msg = "Error while creating database pool";

            logger.error(msg, e);

            throw new IllegalStateException(msg + ": " + e.getMessage());
        }
    }

    /**
     * Setup connection pool
     *
     * @param schema the schema to use
     * @throws java.beans.PropertyVetoException exception
     */
    private void setupConnectionPool(String schema) throws PropertyVetoException {
        if (connectionPool == null) {
            connectionPool = new ComboPooledDataSource();
        }

        // setting up the database connection to the master database
        connectionPool.setDriverClass(dbProperties.getProperty("pride.database.driver"));

        String databaseURL = dbProperties.getProperty("pride.database.protocol") + ':'
                             + dbProperties.getProperty("pride.database.subprotocol") + ':'
                             + dbProperties.getProperty("pride.database.alias");

        if (schema != null) {
            databaseURL += "/" + schema;
        }

        connectionPool.setJdbcUrl(databaseURL);
        connectionPool.setUser(dbProperties.getProperty("pride.database.user"));
        connectionPool.setPassword(dbProperties.getProperty("pride.database.password"));
    }

    /**
     * Get the active schema
     * In pride public instance, we have two schema, normally one is for live queries, the other one is for offline update
     *
     * @return String  database schema
     */
    private String getActiveSchema() {
        String schema = null;

        // get connection to the master database
        Connection        connection = null;
        PreparedStatement stmt       = null;
        ResultSet         resultSet  = null;

        try {
            Class.forName(dbProperties.getProperty("pride.database.driver"));

            String databaseURL = dbProperties.getProperty("pride.database.protocol") + ':'
                                 + dbProperties.getProperty("pride.database.subprotocol") + ':'
                                 + dbProperties.getProperty("pride.database.alias") + "/"
                                 + dbProperties.getProperty("pride.database.master.schema");

            connection = DriverManager.getConnection(databaseURL, dbProperties.getProperty("pride.database.user"),
                    dbProperties.getProperty("pride.database.password"));
            stmt      = connection.prepareStatement("select schema_name from active_schema");
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                schema = resultSet.getString("schema_name");
            }
        } catch (SQLException e) {
            logger.error("Failed to get active DB schema.", e);
        } catch (ClassNotFoundException e) {
            logger.error("Fail to load database driver class: " + dbProperties.getProperty("pride.database.driver"));
        } finally {
            DBUtilities.releaseResources(connection, stmt, resultSet);
        }

        return schema;
    }

    /**
     * Get a pooled connection
     *
     * @return Connection   database connection
     * @throws java.sql.SQLException SQL connection exception
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (getInstance().getConnectionPool() != null) {
            return getInstance().getConnectionPool().getConnection();
        } else {
            String msg = "PooledConnectionFactory DataSource not initialized";

            logger.error(msg);

            throw new IllegalStateException(msg);
        }
    }

    /**
     * Shut down the connection pool
     */
    public static void shutdownPool() {
        try {
            DataSources.destroy(getInstance().getConnectionPool());
        } catch (SQLException e) {
            logger.error("Error while shutting down the connection pool", e);

            throw new IllegalStateException("Could not shut down database pool: " + e.getMessage());
        }
    }

    /**
     * Get singleton instance
     *
     * @return PooledConnectionFactory connection factory
     */
    public static PooledConnectionFactory getInstance() {
        return instance;
    }

    /**
     * Connection pool
     *
     * @return ComboPooledDataSource   connection pool
     */
    public ComboPooledDataSource getConnectionPool() {
        return connectionPool;
    }
}




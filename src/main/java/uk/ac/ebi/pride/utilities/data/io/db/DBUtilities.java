package uk.ac.ebi.pride.utilities.data.io.db;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//~--- JDK imports ------------------------------------------------------------

/**
 * DBUtilities provides methods for database access.
 * <p/>
 * @author rwang, ypriverol
 * Date: 01-Oct-2010
 * Time: 12:01:27
 */
public final class DBUtilities {

    private static final Logger logger = LoggerFactory.getLogger(DBUtilities.class);

    /**
     * Private Constructor
     */
    private DBUtilities() {

    }

    /**
     * It is a good idea to call this method in the finally block.
     *
     * @param stmt       statement
     * @param rs         result set
     * @param connection Connection Object to the Database
     */
    @SuppressWarnings("unchecked")
    public static void releaseResources(Connection connection, Statement stmt, ResultSet rs) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                logger.error("Error while closing the connection", ex);
            }
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                logger.error("Error while closing the result set", ex);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                logger.error("Error while closing the statement", ex);
            }
        }
    }
}




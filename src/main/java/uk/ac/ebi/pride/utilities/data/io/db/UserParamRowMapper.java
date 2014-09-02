package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.UserParam;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a row from database ResultSet to a UserParam object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @version $Id$
 */
public class UserParamRowMapper implements RowMapper<UserParam> {

    @Override
    public UserParam mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserParam(rs.getString("name"), "", rs.getString("value"), "", "", "");
    }
}

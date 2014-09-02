package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.CVLookup;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a row from database ResultSet to a CVLookup object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @version $Id$
 */
public class CVLookupRowMapper implements RowMapper<CVLookup> {
    @Override
    public CVLookup mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CVLookup(rs.getString("cv_label"), rs.getString("full_name"), rs.getString("version"), rs.getString("address"));
    }
}

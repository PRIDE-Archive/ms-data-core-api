package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.CvParam;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a row from database ResultSet to a CvParam object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @version $Id$
 */
public class CvParamRowMapper implements RowMapper<CvParam> {
    @Override
    public CvParam mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CvParam(rs.getString("accession"), rs.getString("name"),
                            rs.getString("cv_label"), rs.getString("value"), "", "", "");
    }
}

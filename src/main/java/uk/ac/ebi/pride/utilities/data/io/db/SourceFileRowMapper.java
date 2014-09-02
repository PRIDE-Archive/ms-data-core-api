package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a row from database ResultSet to a SourceFile object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @author ypriverol
 * @version $Id$
 */
public class SourceFileRowMapper implements RowMapper<SourceFile> {

    @Override
    public SourceFile mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SourceFile(null, "", rs.getString("name_of_file"), rs.getString("path_to_file"));
    }
}

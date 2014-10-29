package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Organization;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map a row from database ResultSet to a Organization object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @version $Id$
 */
public class OrganizationRowMapper implements RowMapper<Organization> {
    @Override
    public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<CvParam> cvParams = new ArrayList<CvParam>();
        CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
        cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), rs.getString("institution"), null, null, null));
        //ToDo: extract email, address information into CvParams?
        List<UserParam> userParams = null;
        return new Organization(new ParamGroup(cvParams, userParams), rs.getString("institution"), null);
    }
}

package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rwang
 * @version $Id$
 */
public class PersonRowMapper implements RowMapper<Person> {
    private final static String CONTACT_INFO = "contact information";

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<CvParam> cvParams = new ArrayList<CvParam>();
        CvTermReference contactName = CvTermReference.CONTACT_NAME;
        cvParams.add(new CvParam(contactName.getAccession(), contactName.getName(), contactName.getCvLabel(), rs.getString("contact_name"), null, null, null));
        CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
        cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), rs.getString("institution"), null, null, null));
        //ToDo: extract email, address information into CvParams?
        List<UserParam> userParams = null;
        String contactInfo = rs.getString("contact_info");
        if (contactInfo != null) {
            userParams = new ArrayList<UserParam>();
            userParams.add(new UserParam(CONTACT_INFO, null, contactInfo, null, null, null));
        }
        List<Organization> affiliation = new ArrayList<Organization>();
        affiliation.add(new Organization(null, rs.getString("institution"), null, null));
        return new Person(new ParamGroup(cvParams, userParams), null, rs.getString("contact_name"), null, null, null, affiliation, null);
    }
}

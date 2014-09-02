package uk.ac.ebi.pride.utilities.data.io.db;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map a row from database ResultSet to a Contact ParamGroup object
 * This class is used with Spring JDBC template
 *
 * @author rwang
 * @version $Id$
 */
public class ContactRowMapper implements RowMapper<ParamGroup> {
    private final static String CONTACT_INFO = "contact information";

    @Override
    public ParamGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
        //there should be a single source file per spectrum
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
        return new ParamGroup(cvParams, userParams);
    }
}

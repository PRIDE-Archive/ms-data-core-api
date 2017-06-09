package uk.ac.ebi.pride.utilities.data.utils;

import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;

import java.util.List;

/**
 * This class is used for Metadata Terms transformation, including changes from one particular CVTerm to another
 * type of CVTerm. It also allows to find particular CVterms in a list, etc.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class CvUtilities {

    public static CvParam getCVTermFromCvReference(CvTermReference cvTerm, String value){
        return new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), value, null, null, null);
    }

    public static CvParam getQuantTermFromQuantReference(QuantCvTermReference quantTerm, String value){
        return new CvParam(quantTerm.getAccession(), quantTerm.getName(), quantTerm.getCvLabel(), value, null, null, null);
    }

    public static String getMailFromCvParam(ParamGroup person) {
        String mail = "";
        if (person.getCvParams() != null) {
            for (CvParam cv : person.getCvParams()) {
                if (cv.getAccession().equals(CvTermReference.CONTACT_EMAIL.getAccession()) || cv.getValue().contains("@")) {
                    mail = cv.getValue();
                }
            }
        }
        if (mail.isEmpty()) {
            if (person.getUserParams() != null) {
                for (uk.ac.ebi.pride.utilities.data.core.UserParam cv : person.getUserParams()) {
                    if (cv.getUnitAcc().equals(CvTermReference.CONTACT_EMAIL.getAccession()) || cv.getValue().contains("@")) {
                        mail = cv.getValue();
                    }
                }
            }
        }

        return mail;
    }

    /**
     * Checks whether the passed identification object is a decoy hit. This function only checks for
     * the presence of specific cv / user Params.
     *
     * @return Boolean indicating whether the passed identification is a decoy hit.
     */
    public static boolean isDecoyHit(ParamGroup paramGroup) {
        if (paramGroup != null) {
            if (paramGroup.getCvParams() != null && !paramGroup.getCvParams().isEmpty()) {
                for (CvParam param : paramGroup.getCvParams()) {
                    if (CvTermReference.PRIDE_DECOY_HIT.getAccession().equalsIgnoreCase(param.getAccession()) ||
                            CvTermReference.MS_DECOY_PEPTIDE.getAccession().equalsIgnoreCase(param.getAccession()))
                        return true;
                }

            }
            if (paramGroup.getUserParams() != null && !paramGroup.getUserParams().isEmpty()) {
                for (uk.ac.ebi.pride.utilities.data.core.UserParam param : paramGroup.getUserParams()) {
                    if ("Decoy Hit".equals(param.getName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static String getValueFromParmGroup(List<CvParam> cvParams, String accession) {
        if(cvParams != null && cvParams.size()>0 && accession != null && accession.length() > 0){
           for(CvParam param: cvParams)
               if(param.getAccession().equalsIgnoreCase(accession))
                   return param.getValue();
        }
        return null;
    }

    public static boolean isAccessionDecoy(Protein identification) {
        String accession = identification.getDbSequence().getAccession();
        for(String prefix: PRIDEUtils.PREFIX_PRIDE_DECOY_ENTRIES)
            if(accession.toUpperCase().startsWith(prefix.toUpperCase()))
                return true;
        for(String postfix: PRIDEUtils.POSTFIX_PRIDE_DECOY_ENTRIES)
            if(accession.toUpperCase().endsWith(postfix.toUpperCase()))
                return true;
        for(String middle: PRIDEUtils.MIDDLE_PRIDE_DECOY_ENTRIES)
            if(accession.toUpperCase().contains(middle.toUpperCase()))
                return true;
        return false;
    }
}

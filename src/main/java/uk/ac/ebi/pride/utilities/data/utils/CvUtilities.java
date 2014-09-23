package uk.ac.ebi.pride.utilities.data.utils;

import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;

import java.util.List;

/**
 * @author ypriverol
 * @author rwang
 */
public class CvUtilities {

    public static CvParam getCVTermFromCvReference(CvTermReference cvTerm, String value){
        return new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), value, null, null, null);
    }

    public static CvParam getQuantTermFromQuantReference(QuantCvTermReference quantTerm, String value){
        return new CvParam(quantTerm.getAccession(), quantTerm.getName(), quantTerm.getCvLabel(), value, null, null, null);
    }

    public static String getMailFromCvParam(ParamGroup person){
        String mail = "";
        for(CvParam cv: person.getCvParams()){
            if(cv.getAccession().equals(CvTermReference.CONTACT_EMAIL.getAccession()) || cv.getValue().contains("@") ){
                mail = cv.getValue();
            }
        }
        if(mail.isEmpty()){
            for(uk.ac.ebi.pride.utilities.data.core.UserParam cv: person.getUserParams()){
                if(cv.getUnitAcc().equals(CvTermReference.CONTACT_EMAIL.getAccession()) || cv.getValue().contains("@") ){
                    mail = cv.getValue();
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
        if(paramGroup != null && paramGroup.getCvParams().size() > 0)
            for(CvParam param: paramGroup.getCvParams())
                if(CvTermReference.PRIDE_DECOY_HIT.getAccession().equalsIgnoreCase(param.getAccession()) ||
                        CvTermReference.MS_DECOY_PEPTIDE.getAccession().equalsIgnoreCase(param.getAccession()))
                    return true;
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
}

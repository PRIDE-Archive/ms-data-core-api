package uk.ac.ebi.pride.utilities.data.lightModel;

import java.util.List;

/**
 * This interface defines the presence of a List&lt;Cvparam&gt; getCvParam() method.
 * It is used in the Marshaller/Unmarshaller to update the CvParam containing
 * classes with the respective subclasses of CvParam.
 * Note: this interface is use together with the CvParamCapable interface,
 * to distinguish if a object has a CvParam or a List&lt;CvParam&gt;.
 *
 */
public interface CvParamListCapable {

    /**
     *
     * @return A List of CvParam objects.
     */
    public List<CvParam> getCvParam();
}

package uk.ac.ebi.pride.utilities.data.lightModel;

/**
 * This interface defines the presence of a Cvparam getCvParam() method. It is used
 * in the Marshaller/Unmarshaller to update the CvParam containing classes
 * with the respective subclasses of CvParam.
 * Note: this interface is use together with the CvParamListCapable interface,
 * to distinguish if a object has a CvParam or a List&lt;CvParam&gt;.
 *
 */
public interface CvParamCapable {

    /**
     * @return A single CvParam.
     */
    public CvParam getCvParam();

    public void setCvParam(CvParam param);
}

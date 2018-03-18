package uk.ac.ebi.pride.utilities.data.lightModel;

/**
 * @author Suresh Hewapathirana
 */
/**
 * This interface defines the presence of a Cvparam getCvParam() method. It is used
 * in the Marshaller/Unmarshaller to update the CvParam containing classes
 * with the respective subclasses of CvParam.
 * Note: this interface is use together with the CvParamListCapable interface,
 * to distinguish if a object has a CvParam or a List&lt;CvParam&gt;.
 *
 * @see uk.ac.ebi.jmzidml.model.CvParamListCapable
 * @author Florian Reisinger
 *         Date: 09-Nov-2010
 * @since 1.0
 */
public interface CvParamCapable {

    /**
     * @return A single CvParam.
     */
    public CvParam getCvParam();

    public void setCvParam(CvParam param);
}

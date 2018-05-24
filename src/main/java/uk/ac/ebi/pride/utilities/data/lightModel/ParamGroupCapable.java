package uk.ac.ebi.pride.utilities.data.lightModel;

import java.util.List;

/**
 * Defines the methods needed by the Marshal/Unmarshal listeners to split
 * a List&lt;Param&gt; into respective List&lt;CvParam&gt; and List&lt;UserParam&gt; and
 * methods used to replace the CvParam/UserParam classes with their
 * respective subclasses.
 */
public interface ParamGroupCapable extends CvParamListCapable {

    public List<CvParam> getCvParam();

    public List<UserParam> getUserParam();
}

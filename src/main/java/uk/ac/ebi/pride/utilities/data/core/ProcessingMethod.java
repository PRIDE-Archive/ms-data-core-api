package uk.ac.ebi.pride.utilities.data.core;

/**
 * Description of the default peak processing method.
 * Variable methods should be described in the appropriate acquisition section.
 * if no acquisition-specific details are found, then this information serves as
 * the default.
 * <p/>
 * In mzML 1.1.0.1, cvParams has the following semantic requirements:
 * 1. May have one or more "data processing parameter" (low intensity threshold,
 * high intensity threshold, completion time, inclusive low intensity threshold,
 * inclusive high intensity threshold)
 * <p/>
 * 2. Must have one or more "data transformation" (deisotoping, charge deconvolution,
 * Conversion to mzML, Conversion to mxXML, Conversion to mzData, baseline reduction,
 * low intensity data point removal, conversion to dta, retention time alignment,
 * high intensity data point removal and et al.
 * <p/>
 * <p/>
 * @author Rui Wang
 * Date: 04-Feb-2010
 * Time: 16:58:59
 */
public class ProcessingMethod extends ParamGroup {

    /**
     * order of steps
     */
    private int order;

    /**
     * software type
     */
    private Software software;

    /**
     * Constructor
     *
     * @param order    required.
     * @param software required.
     * @param params   optional.
     */
    public ProcessingMethod(int order, Software software, ParamGroup params) {
        super(params);
        this.order    = order;
        this.software = software;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProcessingMethod that = (ProcessingMethod) o;

        return order == that.order && !(software != null ? !software.equals(that.software) : that.software != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + order;
        result = 31 * result + (software != null ? software.hashCode() : 0);
        return result;
    }
}




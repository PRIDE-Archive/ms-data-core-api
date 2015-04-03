package uk.ac.ebi.pride.utilities.data.core;

/**
 * Assay In the mzTab experiment with the corresponding Reagent and sample
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 *
 */
public class Assay extends IdentifiableParamGroup {

    /** Sample reference by the assay */
    private Sample sample;

    /** Type of Reagent used in the Assay described by CVParam **/
    private CvParam reagent;

    public Assay(Comparable id, String name) {
        super(id, name);
    }

    public Assay(ParamGroup params, Comparable id, String name) {
        super(params, id, name);
    }

    public Assay(Comparable id, String name, Sample sample) {
        super(id, name);
        this.sample = sample;
    }

    public Assay(ParamGroup params, Comparable id, String name, Sample sample) {
        super(params, id, name);
        this.sample = sample;
    }

    public Assay(ParamGroup params, Comparable id, String name, Sample sample, CvParam reagent) {
        super(params, id, name);
        this.sample = sample;
        this.reagent = reagent;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public CvParam getReagent() {
        return reagent;
    }

    public void setReagent(CvParam reagent) {
        this.reagent = reagent;
    }
}

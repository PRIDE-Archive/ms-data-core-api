package uk.ac.ebi.pride.utilities.data.core;

/**
 * Abundance Assay is the Class to storage the abundance variables for each Protein. This is the value of Score Quantitation
 * like Score is for identification, but per assays.
 * @author ypriverol
 * @author rwang
 */
public class AbundanceAssay extends IdentifiableParamGroup {

    private Assay assay;

    private Double value;


    public AbundanceAssay(Comparable id, String name) {
        super(id, name);
    }

    public AbundanceAssay(ParamGroup params, Comparable id, String name) {
        super(params, id, name);
    }

    public AbundanceAssay(Comparable id, String name, Assay assay, Double value) {
        super(id, name);
        this.assay = assay;
    }

    public AbundanceAssay(ParamGroup params, Comparable id, String name, Assay assay, Double value) {
        super(params, id, name);
        this.assay = assay;
    }

    public Assay getAssay() {
        return assay;
    }

    public void setAssay(Assay assay) {
        this.assay = assay;
    }
}

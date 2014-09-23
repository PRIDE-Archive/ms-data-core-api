package uk.ac.ebi.pride.utilities.data.core;

/**
 * Created by yperez on 23/09/2014.
 */
public class Assay extends IdentifiableParamGroup {

    public Assay(Comparable id, String name) {
        super(id, name);
    }

    public Assay(ParamGroup params, Comparable id, String name) {
        super(params, id, name);
    }
}

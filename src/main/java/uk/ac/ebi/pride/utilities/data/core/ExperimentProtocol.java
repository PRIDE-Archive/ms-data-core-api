package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Protocol used to generate the dataset, added by PRIDE XML 2.0.
 * <p/>
 * mzIdentML protocols are arranged in a slightly different way
 * In the case of MzIdentMl the Protocol is specified at the level of ProteinDetection and SpectrumDetection.
 * but we can create an objetc of this type integrating the information from SpectrumidentificationObject and ProteinDetection Object
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class ExperimentProtocol extends IdentifiableParamGroup {

    /**
     * Global Protocol steps for a PRIDE Experiment
     */
    private List<ParamGroup> protocolSteps;

    public ExperimentProtocol(Comparable id, String name) {
        super(id, name);
    }

    public ExperimentProtocol(ParamGroup params, String id, String name, List<ParamGroup> protocolSteps) {
        super(params, id, name);
        this.protocolSteps = CollectionUtils.createListFromList(protocolSteps);
    }

    public List<ParamGroup> getProtocolSteps() {
        return protocolSteps;
    }

    public void setProtocolSteps(List<ParamGroup> protocolSteps) {
        CollectionUtils.replaceValuesInCollection(protocolSteps, this.protocolSteps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentProtocol)) return false;
        if (!super.equals(o)) return false;

        ExperimentProtocol that = (ExperimentProtocol) o;

        return protocolSteps.equals(that.protocolSteps);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + protocolSteps.hashCode();
        return result;
    }
}




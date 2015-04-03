package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Description of the way in which a List of Software were used. This structure is used by mzMl to store
 * the information of each Step of Data Processing. The list structure represent the relation between an
 * specific Software and a List of CvPrarams related with this software.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class DataProcessing extends Identifiable {

    /**
     * Description of the default peak processing method, this is a ordered List
     * processing Methods is the relation between a Software an a Group of Param.
     */
    private final List<ProcessingMethod> processingMethods;

    public DataProcessing(Comparable id, List<ProcessingMethod> processingMethods) {
        this(id, null, processingMethods);
    }

    public DataProcessing(Comparable id, String name, List<ProcessingMethod> processingMethods) {
        super(id, name);
        this.processingMethods = CollectionUtils.createListFromList(processingMethods);
    }

    public List<ProcessingMethod> getProcessingMethods() {
        return processingMethods;
    }

    public void setProcessingMethods(List<ProcessingMethod> processingMethods) {
        CollectionUtils.replaceValuesInCollection(processingMethods, this.processingMethods);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataProcessing)) return false;
        if (!super.equals(o)) return false;

        DataProcessing that = (DataProcessing) o;

        return processingMethods.equals(that.processingMethods);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + processingMethods.hashCode();
        return result;
    }
}




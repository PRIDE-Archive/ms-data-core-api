package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;
import uk.ac.ebi.pride.utilities.data.utils.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Description of the sample used to generate the Dataset.
 * This Sample class represent the basic information contained in the mzMl files.
 * If the Object instance is MzMl Sample the Sample parent and the contactRoles must be null;
 * <p/>
 * @author rwang, ypriverol
 * Date: 04-Feb-2011
 * Time: 15:50:55
 */
public class Sample extends IdentifiableParamGroup {

    /**
     * Contact Role could be defined as a Person and a specific role (CVTerms)
     */
    private final Map<Contact, CvParam> contactRoles;

    /**
     * Each sample could have a parent Sample, this relation is defined in the MzIdentMl Files.
     */
    private final List<Sample> subSamples;

    public Sample(ParamGroup params, String id, String name) {
        this(params, id, name, null, null);
    }

    public Sample(ParamGroup params, String id, String name, List<Sample> subSamples,
                  Map<Contact, CvParam> contactRoles) {
        super(params, id, name);
        this.subSamples = CollectionUtils.createListFromList(subSamples);
        this.contactRoles = MapUtils.createMapFromMap(contactRoles);
    }

    public List<Sample> getParentSample() {
        return subSamples;
    }

    public void setParentSample(List<Sample> subSamples) {
        CollectionUtils.replaceValuesInCollection(subSamples, this.subSamples);
    }

    public Map<Contact, CvParam> getContactRoles() {
        return contactRoles;
    }

    public void setContactRoles(Map<Contact, CvParam> contactRoles) {
        MapUtils.replaceValuesInMap(contactRoles, this.contactRoles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;
        if (!super.equals(o)) return false;

        Sample sample = (Sample) o;

        return contactRoles.equals(sample.contactRoles) && subSamples.equals(sample.subSamples);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + contactRoles.hashCode();
        result = 31 * result + subSamples.hashCode();
        return result;
    }
}




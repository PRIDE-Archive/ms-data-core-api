package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.MapUtils;

import java.util.Map;

/**
 * Referenceable param group stores a map of reference string to param group.
 * <p/>
 * @author Rui Wang
 */
public class ReferenceableParamGroup implements MassSpecObject {
    private final Map<String, ParamGroup> refMap;

    public ReferenceableParamGroup(Map<String, ParamGroup> refMap) {
        this.refMap = MapUtils.createMapFromMap(refMap);
    }

    public Map<String, ParamGroup> getRefMap() {
        return refMap;
    }

    public void setRefMap(Map<String, ParamGroup> refMap) {
        MapUtils.replaceValuesInMap(refMap, this.refMap);
    }

    public void addRefParamGroup(String ref, ParamGroup params) {
        refMap.put(ref, params);
    }

    public void removeRefParamGroup(String ref) {
        refMap.remove(ref);
    }

    public ParamGroup getRefParamGroup(String ref) {
        return refMap.get(ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceableParamGroup)) return false;

        ReferenceableParamGroup that = (ReferenceableParamGroup) o;

        return refMap.equals(that.refMap);

    }

    @Override
    public int hashCode() {
        return refMap.hashCode();
    }
}




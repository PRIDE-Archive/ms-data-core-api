package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;
import uk.ac.ebi.pride.utilities.data.utils.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol
 * Date: 05/08/11
 * Time: 16:40
 */
public class MassTable extends ParamGroup {

    private final List<Integer> msLevel;
    private final Map<String, Float> residues;
    private final Map<String, ParamGroup> ambiguousResidues;

    public MassTable(List<Integer> msLevel,
                     Map<String, Float> residues,
                     Map<String, ParamGroup> ambiguousResidues) {
        this.msLevel = CollectionUtils.createListFromList(msLevel);
        this.residues = MapUtils.createMapFromMap(residues);
        this.ambiguousResidues = MapUtils.createMapFromMap(ambiguousResidues);
    }

    public List<Integer> getMsLevel() {
        return msLevel;
    }

    public void setMsLevel(List<Integer> msLevel) {
        CollectionUtils.replaceValuesInCollection(msLevel, this.msLevel);
    }

    public Map<String, Float> getResidues() {
        return residues;
    }

    public void setResidues(Map<String, Float> residues) {
        MapUtils.replaceValuesInMap(residues, this.residues);
    }

    public Map<String, ParamGroup> getAmbiguousResidues() {
        return ambiguousResidues;
    }

    public void setAmbiguousResidues(Map<String, ParamGroup> ambiguousResidues) {
        MapUtils.replaceValuesInMap(ambiguousResidues, this.ambiguousResidues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MassTable)) return false;
        if (!super.equals(o)) return false;

        MassTable massTable = (MassTable) o;

        return ambiguousResidues.equals(massTable.ambiguousResidues) && msLevel.equals(massTable.msLevel) && residues.equals(massTable.residues);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + msLevel.hashCode();
        result = 31 * result + residues.hashCode();
        result = 31 * result + ambiguousResidues.hashCode();
        return result;
    }
}




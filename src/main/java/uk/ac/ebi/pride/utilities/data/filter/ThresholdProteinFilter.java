package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Protein;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 14:56
 */
public class ThresholdProteinFilter implements ProteinFilter {

    @Override
    public List<Protein> filter(List<Protein> proteins) {
        List<uk.ac.ebi.pride.utilities.data.core.Protein> passThresholdProteins = new ArrayList<Protein>();

        for (uk.ac.ebi.pride.utilities.data.core.Protein protein : proteins) {
            if(protein.isPassThreshold()){
                passThresholdProteins.add(protein);
            }
        }

        return passThresholdProteins;
    }
}

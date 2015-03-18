package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Protein;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 14:56
 */
public class NoProteinFilter implements ProteinFilter {

    @Override
    public List<Protein> filter(List<Protein> proteins) {
        List<Protein> filteredProteins = new ArrayList<Protein>();
        filteredProteins.addAll(proteins);

        return filteredProteins;
    }
}

package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Peptide;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 17:23
 */
public class NoPeptideFilter implements PeptideFilter {
    @Override
    public List<Peptide> filter(List<Peptide> peptides) {
        List<Peptide> filteredPeptides = new ArrayList<Peptide>();
        filteredPeptides.addAll(peptides);

        return filteredPeptides;
    }
}

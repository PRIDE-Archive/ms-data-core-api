package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Peptide;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 14:55
 */
public class RankOnePeptideFilter implements PeptideFilter {

    @Override
    public List<Peptide> filter(List<Peptide> peptides) {

        List<Peptide> filteredPeptides = new ArrayList<Peptide>();

        for (Peptide peptide : peptides) {
            if (peptide.getSpectrumIdentification() != null) {
                if( peptide.getSpectrumIdentification().getRank() == 1) {
                    filteredPeptides.add(peptide);
                }
            }
        }

        return filteredPeptides;
    }

}

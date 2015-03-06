package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Peptide;

import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 14:44
 */
public interface PeptideFilter {
    public List<Peptide> filter(List<Peptide> peptides);
}

package uk.ac.ebi.pride.utilities.data.filter;

import uk.ac.ebi.pride.utilities.data.core.Protein;

import java.util.List;

/**
 * @author ntoro
 * @since 25/02/15 14:44
 */
public interface ProteinFilter {
    public List<Protein> filter(List<Protein> proteins);
}

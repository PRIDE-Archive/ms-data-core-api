package uk.ac.ebi.pride.utilities.data.filter;

/**
 * Filter for a protein accession
 *
 * @author Rui Wang
 */
public interface AccessionFilter<T> {

    boolean apply(T proteinAccession);
}

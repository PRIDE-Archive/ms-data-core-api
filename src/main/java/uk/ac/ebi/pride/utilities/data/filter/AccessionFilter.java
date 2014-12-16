package uk.ac.ebi.pride.utilities.data.filter;

/**
 * Filter for a protein accession
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface AccessionFilter<T> {

    boolean apply(T proteinAccession);
}

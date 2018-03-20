package uk.ac.ebi.pride.utilities.data.controller.access;

/**
 * This interface define a set of functions that should be implemented for any of the ResultFiles to Validate the file at
 * ms-data-core-api level for the Pipeline.
 *
 * @author ypriverol
 * @version $Id$
 */
public interface ResultFileValidation {

    /**
     * Perform randomly spectra validation within the result file.
     * @param numberOfSpectra
     * @return
     */
    double checkRandomSpectraByDeltaMassThreshold(final int numberOfSpectra);

    /**
     * Check the spectra reference in the ResultFile and return a True if the reference is good and false if the
     * Peptide mass and spectra mz value do not pass the threshold.
     *
     * @param numberSpectra Number of spectra to Test
     * @param deltaThreshold threshold to be used in delta mass.
     * @return True if the difference between the theoretical and experimental mass is lower that the deltaThreshold
     */
    boolean checkRandomSpectraByDeltaMassThreshold(int numberSpectra, Double deltaThreshold);



}

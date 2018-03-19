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
     * This is needed in the Validation file to know how many spectra the current ResultFile Referenced
     * @return
     */
    Integer getNumberOfSpectra();

    /**
     * Perform randomly spectra validation within the result file.
     * @param numberOfRandomChecks
     */
    void doSpectraValidation(final int numberOfRandomChecks);
}

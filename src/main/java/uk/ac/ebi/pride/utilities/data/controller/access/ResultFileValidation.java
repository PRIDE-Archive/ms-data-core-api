package uk.ac.ebi.pride.utilities.data.controller.access;

/**
 * This interface defines a set of functions that should be implemented for any of the ResultFiles to Validate the file at the
 * ms-data-core-api level for the submission pipeline.
 *
 * @author ypriverol
 * @version $Id$
 */
public interface ResultFileValidation {

    /**
     * This is needed in the Validation file to know how many spectra the current ResultFile Referenced
     * @return the number of spectra
     */
    Integer getNumberOfSpectra();

    /**
     * Perform randomly spectra validation within the result file.
     * @param numberOfRandomChecks the number of random checks
     */
    void doSpectraValidation(final int numberOfRandomChecks);
}

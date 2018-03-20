package uk.ac.ebi.pride.utilities.data.controller.access;

import java.util.Set;

/**
 * This interface define a set of functions that should be implemented for any of the ResultFiles to Validate the file at
 * ms-data-core-api level for the Pipeline. ResultFiles could be mzIdentML, mzTab or PrideXML
 *
 * @author ypriverol
 * @version $Id$
 */
public interface ResultFileValidation {


    /**
     * Perform randomly spectra validation within the result file.
     *
     * @param numberOfRandomChecks
     */
    void doSpectraValidation(final int numberOfRandomChecks);

    /**
     * Number of proteins identified in the Result File
     *
     * @return
     */
    Integer getTotalNumberOfProteins();

    /**
     * Number of peptides identified in the Result File
     *
     * @return
     */
    Integer getTotalNumberOfPeptides();

    /**
     * This is needed in the Validation file to know how many spectra the current ResultFile Referenced
     *
     * @return
     */
    Integer getTotalNumberOfSpectra();

    /**
     * Number of unique peptides identified in the Result File
     *
     * @return
     */
    Integer getTotalNumberOfUniquePeptides();

    /**
     * Number of spectra identified/reported in the Result File
     *
     * @return
     */
    Integer getTotalNumberOfIdentifiedSpectra();

    /**
     * collection of spectra Ids referenced in the Result File, but not available in the spectra/peak list file(s)
     *
     * @return
     */
    Set<Comparable> getMissingIdentifiedSpectraIds();

    /**
     * This is an indication for how many PSMs do not pass Delta Mass threshold value.
     *
     * @return
     */
    Double getDeltaMzErrorRate();
}
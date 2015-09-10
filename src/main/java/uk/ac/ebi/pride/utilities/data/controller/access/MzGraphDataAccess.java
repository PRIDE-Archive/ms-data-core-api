package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.Chromatogram;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

import java.util.Collection;
import java.util.List;

/**
 * MzGraphDataAccess defines the interface for accessing mzgraph data.
 * It also defines mzgraph related property changing event messages.
 * <p/>
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public interface MzGraphDataAccess {

    /**
     * fired when a chromatogram object has changed
     */
    public static final String CHROMATOGRAM_TYPE = "Chromatogram";

    /**
     * fired when a mz graph object has changed
     */
    public static final String MZGRAPH_TYPE = "MzGraph";

    /**
     * fired when a spectrum object has changed
     */
    public static final String SPECTRUM_TYPE = "Spectrum";

    /**
     * Whether this controller contains spectra
     *
     * @return boolean return true if spectra exist
     */
    public boolean hasSpectrum();

    /**
     * Whether this controller contains chromatogram.
     *
     * @return boolean return true if chromatogram exist.
     */
    public boolean hasChromatogram();

    /**
     * For the identification files we provide the number of spectra that is missing because
     * not link to the original spectra files has been provided.
     * @return int number with the missing spectra
     */
    public int getNumberOfMissingSpectra();

    /**
     * Get the number of spectra.
     *
     * @return int  number of spectra.
     */
    public int getNumberOfSpectra();

    /**
     * Get the number of identified spectra.
     *
     * @return int  number of identified spectra.
     */
    public int getNumberOfIdentifiedSpectra();

    /**
     * Get a collection of spectrum ids
     *
     * @return Collection   a string collection of spectrum ids
     */
    public Collection<Comparable> getSpectrumIds();

    /**
     * Get the index of a spectrum using its id
     *
     * @param id spectrum id
     * @return int  spectrum index
     */
    public int getSpectrumIndex(Comparable id);

    /**
     * Get a Spectrum object via an spectrum id
     *
     * @param id Spectrum id
     * @return Spectrum an Spectrum object
     */
    public Spectrum getSpectrumById(Comparable id);

    /**
     * Get a collection of spectra by its index and offset.
     * Note: this method can be used for paging.
     *
     * @param index  the start index of the spectrum.
     * @param offset the max number of spectra to get.
     * @return Collection<Spectrum> a collection of spectra.
     */
    public Collection<Spectrum> getSpectraByIndex(int index, int offset);

    /**
     * Check whether the given spectrum is identified.
     *
     * @param specId spectrum id
     * @return boolean true is identified
     */
    public boolean isIdentifiedSpectrum(Comparable specId);

    /**
     * Retrieve the Identified Peptides related with one spectrum
      * @param specId Spectrum-Identification-Item Identifier
     * @return java.lang.List<Peptide> List of peptides identified for a current spectrum
     */
    public List<Peptide> getPeptidesBySpectrum(Comparable specId);

    /**
     * Convert the PSM id to spectrum ID
      * @param psmID Spectrum-Identification-Item Identifier
     * @return java.lang.Comparable Identifier from the Mapper
     */
    public Comparable getSpectrumIdForPeptide(Comparable psmID);

    /**
     * Get the number peaks of a spectrum
     *
     * @param specId spectrum id.
     * @return int  number of peaks.
     */
    public int getNumberOfSpectrumPeaks(Comparable specId);

    /**
     * Get the ms level of a spectrum.
     *
     * @param specId spectrum id.
     * @return int  ms level.
     */
    public int getSpectrumMsLevel(Comparable specId);

    /**
     * Get the precursor charge of a spectrum.
     *
     * @param specId spectrum id.
     * @return Integer  precursor charge, -1 means no charge.
     */
    public Integer getSpectrumPrecursorCharge(Comparable specId);

    /**
     * Get the precursor's m/z value of a spectrum.
     *
     * @param specId spectrum id.
     * @return double  precursor m/z value
     */
    public double getSpectrumPrecursorMz(Comparable specId);

    /**
     * Get the precursor's intensity of a spectrum.
     *
     * @param specId spectrum id.
     * @return double   precursor intensity.
     * @throws DataAccessException data access exception.
     */
    public double getSpectrumPrecursorIntensity(Comparable specId) throws DataAccessException;

    /**
     * Get the sum of intensity of a spectrum.
     *
     * @param specId spectrum id.
     * @return double   sum of intensity.
     * @throws DataAccessException data access exception.
     */
    public double getSumOfIntensity(Comparable specId) throws DataAccessException;

    /**
     * Get number of chromatograms
     *
     * @return int  number of chromatograms.
     * @throws DataAccessException throw a exception when there is an error accessing the data source.
     */
    public int getNumberOfChromatograms() throws DataAccessException;

    /**
     * Get a collection of chromatogram ids.
     *
     * @return Collection<Comparable>   a string collection of chromatogram ids
     * @throws DataAccessException throw a exception when there is an error accessing the data source
     */
    public Collection<Comparable> getChromatogramIds() throws DataAccessException;

    /**
     * Get the index of a chromatogram by its id
     *
     * @param chromaId chromatogram id
     * @return int  chromatogram index
     * @throws DataAccessException data access exception
     */
    public int getChromatogramIndex(Comparable chromaId) throws DataAccessException;

    /**
     * Get a Chromatogram object
     *
     * @param chromaId chromatogram string id
     * @return Chromatogram an chromatogram object
     * @throws DataAccessException throw a exception when there is an error accessing the data source
     */
    public Chromatogram getChromatogramById(Comparable chromaId) throws DataAccessException;

    /**
     * Get a collection of chromatograms by its index and offset.
     * Note: this method can be used for paging.
     *
     * @param index  index of the starting chromatogram.
     * @param offset the number of chromatogram to get.
     * @return Collection<Chromatogram> a collection of chromatograms.
     * @throws DataAccessException throw a exception when there is an error accessing the data source.
     */
    public Collection<Chromatogram> getChromatogramByIndex(int index, int offset) throws DataAccessException;
}




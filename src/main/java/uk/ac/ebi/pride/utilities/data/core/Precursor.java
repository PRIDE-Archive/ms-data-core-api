package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * The method of precursor ion selection and activation
 * @author Rui Wang
 * Date: 05-Feb-2010
 * Time: 14:03:21
 */
public class Precursor implements MassSpecObject {

    /**
     * the type and energy level used for activation
     */
    private ParamGroup activation;

    /**
     * For precursor spectra that are external to this document
     */
    private String externalSpectrumID;

    /**
     * the isolation window configured to isolate one or more ions
     */
    private ParamGroup isolationWindow;

    /**
     * a list of ions selected
     */
    private final List<ParamGroup> selectedIons;

    /**
     * source file
     */
    private SourceFile sourceFile;

    /**
     * precursor spectrum
     */
    private Spectrum spectrum;

    /**
     * Constructor
     *
     * @param spectrum           optional.
     * @param sourceFile         optional.
     * @param externalSpectrumID optional.
     * @param isolationWindow    optional.
     * @param selectedIon        optional.
     * @param activation         required.
     */
    public Precursor(Spectrum spectrum, SourceFile sourceFile,
                     String externalSpectrumID, ParamGroup isolationWindow,
                     List<ParamGroup> selectedIon, ParamGroup activation) {
        this.spectrum = spectrum;
        this.sourceFile = sourceFile;
        this.externalSpectrumID = externalSpectrumID;
        this.isolationWindow = isolationWindow;
        this.selectedIons = CollectionUtils.createListFromList(selectedIon);
        this.activation = activation;
    }

    public ParamGroup getActivation() {
        return activation;
    }

    public void setActivation(ParamGroup activation) {
        this.activation = activation;
    }

    public String getExternalSpectrumID() {
        return externalSpectrumID;
    }

    public void setExternalSpectrumID(String externalSpectrumID) {
        this.externalSpectrumID = externalSpectrumID;
    }

    public ParamGroup getIsolationWindow() {
        return isolationWindow;
    }

    public void setIsolationWindow(ParamGroup isolationWindow) {
        this.isolationWindow = isolationWindow;
    }

    public List<ParamGroup> getSelectedIons() {
        return selectedIons;
    }

    public void setSelectedIons(List<ParamGroup> selectedIon) {
        CollectionUtils.replaceValuesInCollection(selectedIon, this.selectedIons);
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Precursor)) return false;

        Precursor precursor = (Precursor) o;

        if (activation != null ? !activation.equals(precursor.activation) : precursor.activation != null) return false;
        if (externalSpectrumID != null ? !externalSpectrumID.equals(precursor.externalSpectrumID) : precursor.externalSpectrumID != null)
            return false;
        if (isolationWindow != null ? !isolationWindow.equals(precursor.isolationWindow) : precursor.isolationWindow != null)
            return false;
        return selectedIons.equals(precursor.selectedIons) && !(sourceFile != null ? !sourceFile.equals(precursor.sourceFile) : precursor.sourceFile != null) && !(spectrum != null ? !spectrum.equals(precursor.spectrum) : precursor.spectrum != null);

    }

    @Override
    public int hashCode() {
        int result = activation != null ? activation.hashCode() : 0;
        result = 31 * result + (externalSpectrumID != null ? externalSpectrumID.hashCode() : 0);
        result = 31 * result + (isolationWindow != null ? isolationWindow.hashCode() : 0);
        result = 31 * result + selectedIons.hashCode();
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0);
        result = 31 * result + (spectrum != null ? spectrum.hashCode() : 0);
        return result;
    }
}




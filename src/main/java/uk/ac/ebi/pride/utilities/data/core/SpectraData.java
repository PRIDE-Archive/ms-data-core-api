package uk.ac.ebi.pride.utilities.data.core;

/**
 * A data set containing spectra data (consisting of one or more spectra).
 * @author Yasset Perez-Riverol
 * Date: 08/08/11
 * Time: 12:07
 */
public class SpectraData extends ExternalData {

    private CvParam spectrumIdFormat;

    public SpectraData(String id, String name, String location, CvParam fileFormat,
                       String externalFormatDocumentationURI, CvParam spectrumIdFormat) {
        super(id, name, location, fileFormat, externalFormatDocumentationURI);
        this.spectrumIdFormat = spectrumIdFormat;
    }

    public CvParam getSpectrumIdFormat() {
        return spectrumIdFormat;
    }

    public void setSpectrumIdFormat(CvParam spectrumIdFormat) {
        this.spectrumIdFormat = spectrumIdFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SpectraData that = (SpectraData) o;

        return spectrumIdFormat.equals(that.spectrumIdFormat);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + spectrumIdFormat.hashCode();
        return result;
    }
}




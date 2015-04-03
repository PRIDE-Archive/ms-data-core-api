package uk.ac.ebi.pride.utilities.data.core;

/**
 * CVLookup is referenced in every CvParam.
 * It serves as a reference to the original controlled vocabulary source.
 * <p/>
 *
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public class CVLookup implements MassSpecObject {

    /**
     * The URI for the controlled vocabulary
     */
    private String address;

    /**
     * Cv Label name, it is also the Id for this CVLookup
     */
    private String cvLabel;

    /**
     * The full name of the controlled vocabulary
     */
    private String fullName;

    /**
     * The version of the controlled vocabulary
     */
    private String version;

    /**
     * Constructor
     *
     * @param cvLabel  required.
     * @param fullName required.
     * @param version  optional.
     * @param address  required.
     */
    public CVLookup(String cvLabel, String fullName, String version, String address) {
        setCvLabel(cvLabel);
        setFullName(fullName);
        setVersion(version);
        setAddress(address);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCvLabel() {
        return cvLabel;
    }

    public void setCvLabel(String cvLabel) {
        this.cvLabel = cvLabel;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CVLookup cvLookup = (CVLookup) o;

        return !(address != null ? !address.equals(cvLookup.address) : cvLookup.address != null) && !(cvLabel != null ? !cvLabel.equals(cvLookup.cvLabel) : cvLookup.cvLabel != null) && !(fullName != null ? !fullName.equals(cvLookup.fullName) : cvLookup.fullName != null) && !(version != null ? !version.equals(cvLookup.version) : cvLookup.version != null);

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (cvLabel != null ? cvLabel.hashCode() : 0);
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}




package uk.ac.ebi.pride.utilities.data.core;

/**
 * <p/>
 * Software details.
 * <p/>
 * In mzML 1.1.0.1, the follow cv terms must be included:
 * <p/>
 * 1. Must have one "software" term (Xcalbur, Bioworks, Masslynx and et al).
 * 2. It is important to know that the first term of the CVPAram list always will correspond with the Cvterm format of
 *    the name. For those cases that needs the name in CV Format like MzTab Converter.
 * <p/>
 * @author Yasset Perez-Riverol, Rui Wang
 * Date: 04-Feb-2010
 * Time: 16:06:45
 */

public class Software extends IdentifiableParamGroup {

    /**
     * A reference to the Contact person that provide the mzIdentMl File.
     * (mzIndetMl description: When a ContactRole is used, it specifies which Contact the role is associated with.
     */
    private Contact contact;

    /**
     * Any customizations to the software, such as alternative scoring mechanisms implemented,
     * should be documented here as free text. The is very important at the for MzIdentML
     */
    private String customization;

    /**
     * URI of the analysis software e.g. manufacturer's website
     */
    private String uri;

    /**
     * software version
     */
    private String version;

    /**
     * Create a Software object with all the attributes
     * @param id             Software Id
     * @param name           Software Name
     * @param contact        Contact Related with the Software
     * @param customization  Customizations
     * @param uri            URI related with the Software
     * @param version        Software version
     */

    public Software(Comparable id,
                    String name,
                    Contact contact,
                    String customization,
                    String uri,
                    String version) {
        super(id, name);
        this.contact = contact;
        this.customization = customization;
        this.uri = uri;
        this.version = version;
    }

    public Software(ParamGroup params,
                    Comparable id,
                    String name,
                    Contact contact,
                    String customization,
                    String uri, String version) {
        super(params, id, name);
        this.contact = contact;
        this.customization = customization;
        this.uri = uri;
        this.version = version;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getCustomization() {
        return customization;
    }

    public void setCustomization(String customization) {
        this.customization = customization;
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
        if (!super.equals(o)) return false;

        Software software = (Software) o;

        return !(contact != null ? !contact.equals(software.contact) : software.contact != null) && !(customization != null ? !customization.equals(software.customization) : software.customization != null) && !(uri != null ? !uri.equals(software.uri) : software.uri != null) && !(version != null ? !version.equals(software.version) : software.version != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (contact != null ? contact.hashCode() : 0);
        result = 31 * result + (customization != null ? customization.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}




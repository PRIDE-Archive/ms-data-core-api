package uk.ac.ebi.pride.utilities.data.core;

/**
 * Provider object.
 * <p/>
 * In mzIdentML 1.1.0., the following the description of this object:
 * <p/>
 * The Provider of the mzIdentML record in terms of the contact and software.
 * This object contains a reference to the last software used to generate the file
 * this software is called the provider.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 04/08/11
 * Time: 11:11
 */
public class Provider extends Identifiable {

    /**
     * A reference to the Contact person that provide the mzIdentMl File.
     * (mzIndetMl description: When a ContactRole is used, it specifies which Contact the role is associated with.
     */
    private Contact contact;

    /*
     * Role in CvParam
     */
    private CvParam role;

    /**
     * The Software that produced the document instance. mzIdentML
     */
    private Software software;

    /**
     * @param id  Generic Id of Provider Object
     * @param name Generic Name of Provider Object
     * @param software Provider software of the file or experiment
     * @param contact  Provider Contact
     * @param role     Role of the Provider Contact
     */
    public Provider(Comparable id, String name, Software software, Contact contact, CvParam role) {
        super(id, name);
        this.software = software;
        this.contact = contact;
        this.role = role;
    }

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public CvParam getRole() {
        return role;
    }

    public void setRole(CvParam role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Provider provider = (Provider) o;

        return !(contact != null ? !contact.equals(provider.contact) : provider.contact != null) && !(role != null ? !role.equals(provider.role) : provider.role != null) && !(software != null ? !software.equals(provider.software) : provider.software != null);

    }

    @Override
    public int hashCode() {
        int result = contact != null ? contact.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (software != null ? software.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Provider{" +
                "contact=" + contact +
                ", role=" + role +
                ", software=" + software +
                '}';
    }
}




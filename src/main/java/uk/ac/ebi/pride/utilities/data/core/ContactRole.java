package uk.ac.ebi.pride.utilities.data.core;

/**
 * Contact Role is a class to define the role of an Specific Contact (Organization or Person)in the context
 * of the Experiment a role is defined as CvParams ()
 * The role that a Contact plays in an organization or with respect to the associating class.
 * A Contact may have several Roles within scope, and as such, associations to ContactRole allow
 * the use of a Contact in a certain manner. Examples might include a provider, or a data analyst.
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */

public class ContactRole {

    /**
     * Could be an Organization or a Person
     */
    private Contact contact;

    /**
     * Role of an specific Contact
     */
    private CvParam role;

    public ContactRole(Contact contact, CvParam role) {
        this.contact = contact;
        this.role    = role;
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

        ContactRole that = (ContactRole) o;

        return contact.equals(that.contact) && !(role != null ? !role.equals(that.role) : that.role != null);

    }

    @Override
    public int hashCode() {
        int result = contact.hashCode();
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContactRole{" +
                "contact=" + contact +
                ", role=" + role +
                '}';
    }
}



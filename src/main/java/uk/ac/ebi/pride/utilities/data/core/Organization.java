package uk.ac.ebi.pride.utilities.data.core;

/**
 * Organizations are entities like companies, universities, government agencies. Any additional information such as the address,
 * email etc. should be supplied either as CV parameters or as user parameters.
 * @author Yasset Perez-Riverol, Rui Wang
 * Date: 04/08/11
 * Time: 11:46
 */

public class Organization extends Contact {

    private String mail;

    private Organization parentOrganization;

    public Organization(ParamGroup params, String name, String mail) {
        this(params, null, name, null, mail);
    }

    public Organization(Comparable id, String name, String mail, Organization parentOrganization) {
        this(null, id, name, parentOrganization, mail);
    }

    public Organization(ParamGroup params, Comparable id, String name, Organization parentOrganization, String mail) {
        super(params, id, name);
        this.parentOrganization = parentOrganization;
        this.mail = mail;
    }

    /**
     * The containing organization (the university or business which a lab belongs to, etc.)
     */
    public Organization getParentOrganization() {
        return parentOrganization;
    }

    public void setParentOrganization(Organization parentOrganization) {
        this.parentOrganization = parentOrganization;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        Organization that = (Organization) o;

        return !((mail != null) ? !mail.equals(that.mail) : that.mail != null) && !((parentOrganization != null) ? !parentOrganization.equals(that.parentOrganization) : that.parentOrganization != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + ((parentOrganization != null)
                ? parentOrganization.hashCode()
                : 0);
        result = 31 * result + ((mail != null)
                ? mail.hashCode()
                : 0);

        return result;
    }
}




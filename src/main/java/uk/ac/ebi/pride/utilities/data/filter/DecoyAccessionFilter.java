package uk.ac.ebi.pride.utilities.data.filter;

/**
 * Decoy filter for protein accession
 *
 * @author Rui Wang
 */
public class DecoyAccessionFilter implements AccessionFilter<String>{

    public enum Type {PREFIX, POSTFIX, CONTAIN}
    /**
     * Type of the matching mechanism
     */
    private Type type;

    /**
     * Matching criteria
     */
    private String criteria;

    public DecoyAccessionFilter(Type type, String criteria) {
        this.type = type;
        this.criteria = criteria;
    }

    public Type getType() {
        return type;
    }


    public String getCriteria() {
        return criteria;
    }

    @Override
    public boolean apply(String accession) {
        if (accession != null) {
            accession = accession.toLowerCase();
            switch (getType()) {
                case PREFIX:
                    return !accession.startsWith(getCriteria());
                case POSTFIX:
                    return !accession.endsWith(getCriteria());
                case CONTAIN:
                    return !accession.contains(getCriteria());
            }
        }
        return false;
    }
}

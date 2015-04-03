package uk.ac.ebi.pride.utilities.data.core;

/**
 * Substitution Modification is a Modification where One amino acid is replaced by another amino acid.
 * For example Deamidation Post-Translational Modification is a Substitution Modification where the amino
 * acid Asparagine (N) is replaced by Aspartic Acid (D)
 *
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 04/08/11
 * Time: 14:18
 */
public class SubstitutionModification {

    private double avgMassDelta;

    private int location;

    private double monoisotopicMassDelta;

    private String originalResidue;

    private String replacementResidue;

    public SubstitutionModification(String originalResidue, String replacementResidue, int location,
                                    double avgMassDelta, double monoisotopicMassDelta) {
        this.originalResidue = originalResidue;
        this.replacementResidue = replacementResidue;
        this.location = location;
        this.avgMassDelta = avgMassDelta;
        this.monoisotopicMassDelta = monoisotopicMassDelta;
    }

    public String getOriginalResidue() {
        return originalResidue;
    }

    public void setOriginalResidue(String originalResidue) {
        this.originalResidue = originalResidue;
    }

    public String getReplacementResidue() {
        return replacementResidue;
    }

    public void setReplacementResidue(String replacementResidue) {
        this.replacementResidue = replacementResidue;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public double getAvgMassDelta() {
        return avgMassDelta;
    }

    public void setAvgMassDelta(double avgMassDelta) {
        this.avgMassDelta = avgMassDelta;
    }

    public double getMonoisotopicMassDelta() {
        return monoisotopicMassDelta;
    }

    public void setMonoisotopicMassDelta(double monoisotopicMassDelta) {
        this.monoisotopicMassDelta = monoisotopicMassDelta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubstitutionModification that = (SubstitutionModification) o;

        return Double.compare(that.avgMassDelta, avgMassDelta) == 0 && location == that.location && Double.compare(that.monoisotopicMassDelta, monoisotopicMassDelta) == 0 && !(originalResidue != null ? !originalResidue.equals(that.originalResidue) : that.originalResidue != null) && !(replacementResidue != null ? !replacementResidue.equals(that.replacementResidue) : that.replacementResidue != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = avgMassDelta != +0.0d ? Double.doubleToLongBits(avgMassDelta) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + location;
        temp = monoisotopicMassDelta != +0.0d ? Double.doubleToLongBits(monoisotopicMassDelta) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (originalResidue != null ? originalResidue.hashCode() : 0);
        result = 31 * result + (replacementResidue != null ? replacementResidue.hashCode() : 0);
        return result;
    }
}




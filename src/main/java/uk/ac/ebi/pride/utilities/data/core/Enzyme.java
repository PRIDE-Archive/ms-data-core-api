package uk.ac.ebi.pride.utilities.data.core;

/**
 * Enzyme used during the search in Databases.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 05/08/11
 * Time: 16:16
 */
public class Enzyme extends Identifiable {
    private String cTermGain;
    private ParamGroup enzymeName;
    private int minDistance;
    private int missedCleavages;
    private String nTermGain;
    private boolean semiSpecific;
    private String siteRegExp;

    public Enzyme(String id, String name,
                  boolean semiSpecific, int missedCleavages, int minDistance,
                  ParamGroup enzymeName, String siteRegExp) {
        super(id, name);
        this.semiSpecific = semiSpecific;
        this.missedCleavages = missedCleavages;
        this.minDistance = minDistance;
        this.enzymeName = enzymeName;
        this.siteRegExp = siteRegExp;
    }

    public String getSiteRegExp() {
        return siteRegExp;
    }

    public void setSiteRegExp(String siteRegExp) {
        this.siteRegExp = siteRegExp;
    }

    public boolean isSemiSpecific() {
        return semiSpecific;
    }

    public void setSemiSpecific(boolean semiSpecific) {
        this.semiSpecific = semiSpecific;
    }

    public int getMissedCleavages() {
        return missedCleavages;
    }

    public void setMissedCleavages(int missedCleavages) {
        this.missedCleavages = missedCleavages;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public ParamGroup getEnzymeName() {
        return enzymeName;
    }

    public void setEnzymeName(ParamGroup enzymeName) {
        this.enzymeName = enzymeName;
    }

    public String getnTermGain() {
        return nTermGain;
    }

    public void setnTermGain(String nTermGain) {
        this.nTermGain = nTermGain;
    }

    public String getcTermGain() {
        return cTermGain;
    }

    public void setcTermGain(String cTermGain) {
        this.cTermGain = cTermGain;
    }

    @Override
    public String toString() {
        return "Enzyme{" +
                "cTermGain='" + cTermGain + '\'' +
                ", enzymeName=" + enzymeName +
                ", minDistance=" + minDistance +
                ", missedCleavages=" + missedCleavages +
                ", nTermGain='" + nTermGain + '\'' +
                ", semiSpecific=" + semiSpecific +
                ", siteRegExp='" + siteRegExp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enzyme)) return false;
        if (!super.equals(o)) return false;

        Enzyme enzyme = (Enzyme) o;

        if (minDistance != enzyme.minDistance) return false;
        if (missedCleavages != enzyme.missedCleavages) return false;
        if (semiSpecific != enzyme.semiSpecific) return false;
        if (cTermGain != null ? !cTermGain.equals(enzyme.cTermGain) : enzyme.cTermGain != null) return false;
        return !(enzymeName != null ? !enzymeName.equals(enzyme.enzymeName) : enzyme.enzymeName != null) && !(nTermGain != null ? !nTermGain.equals(enzyme.nTermGain) : enzyme.nTermGain != null) && !(siteRegExp != null ? !siteRegExp.equals(enzyme.siteRegExp) : enzyme.siteRegExp != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cTermGain != null ? cTermGain.hashCode() : 0);
        result = 31 * result + (enzymeName != null ? enzymeName.hashCode() : 0);
        result = 31 * result + minDistance;
        result = 31 * result + missedCleavages;
        result = 31 * result + (nTermGain != null ? nTermGain.hashCode() : 0);
        result = 31 * result + (semiSpecific ? 1 : 0);
        result = 31 * result + (siteRegExp != null ? siteRegExp.hashCode() : 0);
        return result;
    }
}




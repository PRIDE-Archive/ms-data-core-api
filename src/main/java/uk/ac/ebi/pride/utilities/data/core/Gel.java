package uk.ac.ebi.pride.utilities.data.core;

/**
 * Gel is a class to Describe the Gel Spot for each Protein. it contains the GelLink
 * and the X and Y coordinates for the protein identification.
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class Gel extends ParamGroup {

    /**
     * link to the image of a gel
     */
    private String gelLink;

    /**
     * molecular weight
     */
    private Double molecularWeight;

    /**
     * pI value
     */
    private Double pI;

    /**
     * x coordinate
     */
    private Double xCoordinate;

    /**
     * y coordinate
     */
    private Double yCoordinate;

    /**
     * Constructor
     *
     * @param params          optional.
     * @param gelLink         optional since gel is optional.
     * @param xCoordinate     optional.
     * @param yCoordinate     optional.
     * @param molecularWeight optional.
     * @param pI              optional.
     */
    public Gel(ParamGroup params, String gelLink,
               Double xCoordinate, Double yCoordinate,
               Double molecularWeight, Double pI) {
        super(params);
        this.gelLink = gelLink;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.molecularWeight = molecularWeight;
        this.pI = pI;
    }

    public String getGelLink() {
        return gelLink;
    }

    public void setGelLink(String gelLink) {
        this.gelLink = gelLink;
    }

    public Double getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(Double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Double getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(Double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Double getMolecularWeight() {
        return molecularWeight;
    }

    public void setMolecularWeight(Double molecularWeight) {
        this.molecularWeight = molecularWeight;
    }

    public Double getPI() {
        return pI;
    }

    public void setPI(Double pI) {
        this.pI = pI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Gel gel = (Gel) o;

        return Double.compare(gel.molecularWeight, molecularWeight) == 0 && Double.compare(gel.pI, pI) == 0 && Double.compare(gel.xCoordinate, xCoordinate) == 0 && Double.compare(gel.yCoordinate, yCoordinate) == 0 && !(gelLink != null ? !gelLink.equals(gel.gelLink) : gel.gelLink != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (gelLink != null ? gelLink.hashCode() : 0);
        temp = molecularWeight != +0.0d ? Double.doubleToLongBits(molecularWeight) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = pI != +0.0d ? Double.doubleToLongBits(pI) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = xCoordinate != +0.0d ? Double.doubleToLongBits(xCoordinate) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = yCoordinate != +0.0d ? Double.doubleToLongBits(yCoordinate) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}




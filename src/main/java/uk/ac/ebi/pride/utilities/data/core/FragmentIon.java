package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.NumberUtilities;

import java.util.List;

/**
 * FragmentIon stores details about peptide fragment ion information.
 * Note: FragmentIon represents a ParamGroup object, but it also provides
 * a list of convenient methods to access the values of m/z, intensity and etc.
 * For mzIdentMl, the SpectrumIdentificationItem have the information of
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public class FragmentIon extends ParamGroup {

    /**
     * charge of the fragment ion
     */
    private int charge = -1;

    /**
     * intensity of the fragment ion
     */
    private double intensity = -1;

    /**
     * ion type
     */
    private String ionType = null;

    /**
     * ion type accession
     */
    private String ionTypeAccession = null;

    /**
     * location
     */
    private int location = -1;

    /**
     * mass error margin of the fragment ion
     */
    private double massError = -1;

    /**
     * m/z value
     */
    private double mz = -1;

    /**
     * retention time error margin of the fragment ion
     */
    private double retentionTimeError = -1;

    /**
     * Constructor
     *
     * @param params required.
     */
    public FragmentIon(ParamGroup params) {
        super(params);
        init();
    }

    private void init() {
        List<CvParam> cvParams = this.getCvParams();

        for (CvParam cvParam : cvParams) {
            String accession = cvParam.getAccession();
            String value = cvParam.getValue();

            if (CvTermReference.PRODUCT_ION_MZ.getAccession().equals(accession)
                    || CvTermReference.PRODUCT_ION_MZ_PLGS.getAccession().equals(accession)
                    || CvTermReference.MS_PRODUCT_ION_MZ.getAccession().equals(accession)) {
                mz = NumberUtilities.isNumber(value)
                        ? Double.parseDouble(value)
                        : mz;
            } else if (CvTermReference.PRODUCT_ION_INTENSITY.getAccession().equals(accession)
                    || CvTermReference.PRODUCT_ION_INTENSITY_PLGS.getAccession().equals(accession)
                    || CvTermReference.MS_PRODUCT_ION_INTENSITY.getAccession().equals(accession)) {
                intensity = NumberUtilities.isNumber(value)
                        ? Double.parseDouble(value)
                        : intensity;
            } else if (CvTermReference.PRODUCT_ION_MASS_ERROR.getAccession().equals(accession)
                    || CvTermReference.PRODUCT_ION_MASS_ERROR_PLGS.getAccession().equals(accession)
                    || CvTermReference.MS_PRODUCT_ION_MASS_ERROR.getAccession().equals(accession)) {
                massError = NumberUtilities.isNumber(value)
                        ? Double.parseDouble(value)
                        : massError;
            } else if (CvTermReference.PRODUCT_ION_RETENTION_TIME_ERROR.getAccession().equals(accession)
                    || CvTermReference.PRODUCT_ION_RETENTION_TIME_ERROR_PLGS.getAccession().equals(accession)) {
                retentionTimeError = NumberUtilities.isNumber(value)
                        ? Double.parseDouble(value)
                        : retentionTimeError;
            } else if (CvTermReference.PRODUCT_ION_CHARGE.getAccession().equals(accession)) {
                charge = NumberUtilities.isInteger(value)
                        ? Integer.parseInt(value)
                        : charge;
            } else if (ionType == null && cvParam.getName().contains("ion")) {
                ionType = cvParam.getName();
                location = NumberUtilities.isInteger(value)
                        ? Integer.parseInt(value)
                        : location;
                ionTypeAccession = accession;
            }
        }
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getMassError() {
        return massError;
    }

    public void setMassError(double massError) {
        this.massError = massError;
    }

    public double getRetentionTimeError() {
        return retentionTimeError;
    }

    public void setRetentionTimeError(double retentionTimeError) {
        this.retentionTimeError = retentionTimeError;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public String getIonType() {
        return ionType;
    }

    public void setIonType(String ionType) {
        this.ionType = ionType;
    }

    public String getIonTypeAccession() {
        return ionTypeAccession;
    }

    public void setIonTypeAccession(String ionTypeAccession) {
        this.ionTypeAccession = ionTypeAccession;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FragmentIon that = (FragmentIon) o;

        return charge == that.charge && Double.compare(that.intensity, intensity) == 0 && location == that.location && Double.compare(that.massError, massError) == 0 && Double.compare(that.mz, mz) == 0 && Double.compare(that.retentionTimeError, retentionTimeError) == 0 && !(ionType != null ? !ionType.equals(that.ionType) : that.ionType != null) && !(ionTypeAccession != null ? !ionTypeAccession.equals(that.ionTypeAccession) : that.ionTypeAccession != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + charge;
        temp = intensity != +0.0d ? Double.doubleToLongBits(intensity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (ionType != null ? ionType.hashCode() : 0);
        result = 31 * result + (ionTypeAccession != null ? ionTypeAccession.hashCode() : 0);
        result = 31 * result + location;
        temp = massError != +0.0d ? Double.doubleToLongBits(massError) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = mz != +0.0d ? Double.doubleToLongBits(mz) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = retentionTimeError != +0.0d ? Double.doubleToLongBits(retentionTimeError) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}




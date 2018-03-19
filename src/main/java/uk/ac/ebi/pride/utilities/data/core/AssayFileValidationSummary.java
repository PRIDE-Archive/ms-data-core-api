package uk.ac.ebi.pride.utilities.data.core;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class describes the summary information of an assay file.
 */
public class AssayFileValidationSummary implements Serializable {
    private int id;
    private String accession;
    private String name;
    private String shortLabel;
    private int numberOfProteins;
    private int numberOfPeptides;
    private int numberOfSpectra;
    private int numberOfUniquePeptides;
    private int numberOfExistingIdentifiedSpectra;
    private int numberOfIdentifiedSpectra;
    private int numberofMissingSpectra;
    private boolean spectrumMatchFragmentIons;
    private final Set<Comparable> missingIdentifiedSpectraIds;
    private boolean ms2Annotation;
    private boolean chromatogram;
    private boolean proteinGroupPresent;
    private String exampleProteinAccession;
    private String searchDatabase;
    private double deltaMzErrorRate;
    private String experimentalFactor;


    /**
     * Default constructor, sets all the variables to default values.
     */
    public AssayFileValidationSummary() {
        this.id = -1;
        this.accession = null;
        this.name = null;
        this.shortLabel = null;
        this.numberOfProteins = 0;
        this.numberOfPeptides = 0;
        this.numberOfSpectra = 0;
        this.numberOfUniquePeptides = 0;
        this.numberOfExistingIdentifiedSpectra = 0;
        this.numberOfIdentifiedSpectra = 0;
        this.numberofMissingSpectra = 0;
        this.spectrumMatchFragmentIons = true;
        this.missingIdentifiedSpectraIds = new LinkedHashSet<>();
        this.ms2Annotation = false;
        this.chromatogram = false;
        this.proteinGroupPresent = false;
        this.experimentalFactor = null;
    }

    /**
     * Sets new numberofMissingSpectra.
     *
     * @param numberofMissingSpectra New value of numberofMissingSpectra.
     */
    public void setNumberofMissingSpectra(int numberofMissingSpectra) {
        this.numberofMissingSpectra = numberofMissingSpectra;
    }

    /**
     * Gets searchDatabase.
     *
     * @return Value of searchDatabase.
     */
    public String getSearchDatabase() {
        return searchDatabase;
    }

    /**
     * Gets numberOfUniquePeptides.
     *
     * @return Value of numberOfUniquePeptides.
     */
    public int getNumberOfUniquePeptides() {
        return numberOfUniquePeptides;
    }

    /**
     * Gets numberOfExistingIdentifiedSpectra.
     *
     * @return Value of numberOfExistingIdentifiedSpectra.
     */
    public int getNumberOfExistingIdentifiedSpectra() {
        return numberOfExistingIdentifiedSpectra;
    }

    /**
     * Sets new spectrumMatchFragmentIons.
     *
     * @param spectrumMatchFragmentIons New value of spectrumMatchFragmentIons.
     */
    public void setSpectrumMatchFragmentIons(boolean spectrumMatchFragmentIons) {
        this.spectrumMatchFragmentIons = spectrumMatchFragmentIons;
    }

    /**
     * Gets experimentalFactor.
     *
     * @return Value of experimentalFactor.
     */
    public String getExperimentalFactor() {
        return experimentalFactor;
    }

    /**
     * Sets new ms2Annotation.
     *
     * @param ms2Annotation New value of ms2Annotation.
     */
    public void setMs2Annotation(boolean ms2Annotation) {
        this.ms2Annotation = ms2Annotation;
    }

    /**
     * Sets new numberOfProteins.
     *
     * @param numberOfProteins New value of numberOfProteins.
     */
    public void setNumberOfProteins(int numberOfProteins) {
        this.numberOfProteins = numberOfProteins;
    }

    /**
     * Gets exampleProteinAccession.
     *
     * @return Value of exampleProteinAccession.
     */
    public String getExampleProteinAccession() {
        return exampleProteinAccession;
    }

    /**
     * Gets spectrumMatchFragmentIons.
     *
     * @return Value of spectrumMatchFragmentIons.
     */
    public boolean isSpectrumMatchFragmentIons() {
        return spectrumMatchFragmentIons;
    }

    /**
     * Sets new shortLabel.
     *
     * @param shortLabel New value of shortLabel.
     */
    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    /**
     * Gets missingIdentifiedSpectraIds.
     *
     * @return Value of missingIdentifiedSpectraIds.
     */
    public Set<Comparable> getMissingIdentifiedSpectraIds() {
        return missingIdentifiedSpectraIds;
    }

    /**
     * add missingIdentifiedSpectraId.
     */
    public void addMissingIdentifiedSpectraId(Comparable id) {
        this.missingIdentifiedSpectraIds.add(id);
    }

    /**
     * Sets new deltaMzErrorRate.
     *
     * @param deltaMzErrorRate New value of deltaMzErrorRate.
     */
    public void setDeltaMzErrorRate(double deltaMzErrorRate) {
        this.deltaMzErrorRate = deltaMzErrorRate;
    }

    /**
     * Gets numberOfIdentifiedSpectra.
     *
     * @return Value of numberOfIdentifiedSpectra.
     */
    public int getNumberOfIdentifiedSpectra() {
        return numberOfIdentifiedSpectra;
    }

    /**
     * Sets new exampleProteinAccession.
     *
     * @param exampleProteinAccession New value of exampleProteinAccession.
     */
    public void setExampleProteinAccession(String exampleProteinAccession) {
        this.exampleProteinAccession = exampleProteinAccession;
    }

    /**
     * Gets id.
     *
     * @return Value of id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets new numberOfPeptides.
     *
     * @param numberOfPeptides New value of numberOfPeptides.
     */
    public void setNumberOfPeptides(int numberOfPeptides) {
        this.numberOfPeptides = numberOfPeptides;
    }

    /**
     * Sets new experimentalFactor.
     *
     * @param experimentalFactor New value of experimentalFactor.
     */
    public void setExperimentalFactor(String experimentalFactor) {
        this.experimentalFactor = experimentalFactor;
    }

    /**
     * Gets numberOfProteins.
     *
     * @return Value of numberOfProteins.
     */
    public int getNumberOfProteins() {
        return numberOfProteins;
    }

    /**
     * Gets shortLabel.
     *
     * @return Value of shortLabel.
     */
    public String getShortLabel() {
        return shortLabel;
    }

    /**
     * Sets new numberOfIdentifiedSpectra.
     *
     * @param numberOfIdentifiedSpectra New value of numberOfIdentifiedSpectra.
     */
    public void setNumberOfIdentifiedSpectra(int numberOfIdentifiedSpectra) {
        this.numberOfIdentifiedSpectra = numberOfIdentifiedSpectra;
    }

    /**
     * Gets numberOfSpectra.
     *
     * @return Value of numberOfSpectra.
     */
    public int getNumberOfSpectra() {
        return numberOfSpectra;
    }

    /**
     * Gets deltaMzErrorRate.
     *
     * @return Value of deltaMzErrorRate.
     */
    public double getDeltaMzErrorRate() {
        return deltaMzErrorRate;
    }

    /**
     * Sets new searchDatabase.
     *
     * @param searchDatabase New value of searchDatabase.
     */
    public void setSearchDatabase(String searchDatabase) {
        this.searchDatabase = searchDatabase;
    }

    /**
     * Gets accession.
     *
     * @return Value of accession.
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Sets new numberOfUniquePeptides.
     *
     * @param numberOfUniquePeptides New value of numberOfUniquePeptides.
     */
    public void setNumberOfUniquePeptides(int numberOfUniquePeptides) {
        this.numberOfUniquePeptides = numberOfUniquePeptides;
    }

    /**
     * Sets new chromatogram.
     *
     * @param chromatogram New value of chromatogram.
     */
    public void setChromatogram(boolean chromatogram) {
        this.chromatogram = chromatogram;
    }

    /**
     * Sets new proteinGroupPresent.
     *
     * @param proteinGroupPresent New value of proteinGroupPresent.
     */
    public void setProteinGroupPresent(boolean proteinGroupPresent) {
        this.proteinGroupPresent = proteinGroupPresent;
    }

    /**
     * Gets name.
     *
     * @return Value of name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets proteinGroupPresent.
     *
     * @return Value of proteinGroupPresent.
     */
    public boolean isProteinGroupPresent() {
        return proteinGroupPresent;
    }

    /**
     * Gets ms2Annotation.
     *
     * @return Value of ms2Annotation.
     */
    public boolean isMs2Annotation() {
        return ms2Annotation;
    }

    /**
     * Gets numberOfPeptides.
     *
     * @return Value of numberOfPeptides.
     */
    public int getNumberOfPeptides() {
        return numberOfPeptides;
    }

    /**
     * Sets new name.
     *
     * @param name New value of name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets new numberOfExistingIdentifiedSpectra.
     *
     * @param numberOfExistingIdentifiedSpectra New value of numberOfExistingIdentifiedSpectra.
     */
    public void setNumberOfExistingIdentifiedSpectra(int numberOfExistingIdentifiedSpectra) {
        this.numberOfExistingIdentifiedSpectra = numberOfExistingIdentifiedSpectra;
    }

    /**
     * Gets numberofMissingSpectra.
     *
     * @return Value of numberofMissingSpectra.
     */
    public int getNumberofMissingSpectra() {
        return this.missingIdentifiedSpectraIds.size();
    }

    /**
     * Gets chromatogram.
     *
     * @return Value of chromatogram.
     */
    public boolean isChromatogram() {
        return chromatogram;
    }

    /**
     * Has chromatogram.
     *
     * @return Value of chromatogram.
     */
    public boolean hasChromatogram() {
        return chromatogram;
    }

    /**
     * Sets new accession.
     *
     * @param accession New value of accession.
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Sets new numberOfSpectra.
     *
     * @param numberOfSpectra New value of numberOfSpectra.
     */
    public void setNumberOfSpectra(int numberOfSpectra) {
        this.numberOfSpectra = numberOfSpectra;
    }

    /**
     * Sets new id.
     *
     * @param id New value of id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Print important fields that are useful for validations
     */
    public void printResults() {
        System.out.println("------------- Summary -------------");
        System.out.println("Number of Proteins: " + getNumberOfProteins());
        System.out.println("Number of Peptides: " + getNumberOfPeptides());
        System.out.println("Number of Spectra: " + getNumberOfSpectra());
        System.out.println("Number of Identified Spectra: " + getNumberOfIdentifiedSpectra());
        System.out.println("Number of Missing Spectra: " + getNumberofMissingSpectra());
        System.out.println("DeltaMzErrorRate: " + getDeltaMzErrorRate());
        System.out.println("Missing Identified Spectra Ids: " + getMissingIdentifiedSpectraIds());
    }

    @Override
    public String toString() {
        return "AssayFileValidationSummary{" +
                "id=" + id +
                ", accession='" + accession + '\'' +
                ", name='" + name + '\'' +
                ", shortLabel='" + shortLabel + '\'' +
                ", numberOfProteins=" + numberOfProteins +
                ", numberOfPeptides=" + numberOfPeptides +
                ", numberOfSpectra=" + numberOfSpectra +
                ", numberOfUniquePeptides=" + numberOfUniquePeptides +
                ", numberOfExistingIdentifiedSpectra=" + numberOfExistingIdentifiedSpectra +
                ", numberOfIdentifiedSpectra=" + numberOfIdentifiedSpectra +
                ", numberofMissingSpectra=" + numberofMissingSpectra +
                ", spectrumMatchFragmentIons=" + spectrumMatchFragmentIons +
                ", missingIdentifiedSpectraIds=" + missingIdentifiedSpectraIds +
                ", ms2Annotation=" + ms2Annotation +
                ", chromatogram=" + chromatogram +
                ", proteinGroupPresent=" + proteinGroupPresent +
                ", exampleProteinAccession='" + exampleProteinAccession + '\'' +
                ", searchDatabase='" + searchDatabase + '\'' +
                ", deltaMzErrorRate=" + deltaMzErrorRate +
                ", experimentalFactor='" + experimentalFactor + '\'' +
                '}';
    }
}

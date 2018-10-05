package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

import uk.ac.ebi.pride.archive.repo.assay.*;
import uk.ac.ebi.pride.archive.repo.assay.instrument.Instrument;
import uk.ac.ebi.pride.archive.repo.assay.software.Software;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class describes the summary information of an assay file.
 */
public class AssayFileSummary implements Serializable {
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
  private final Set<AssayPTM> ptms;
  private final Set<Instrument> instruments;
  private final Set<Software> softwares;
  private final Set<Contact> contacts;
  private boolean proteinGroupPresent;
  private String exampleProteinAccession;
  private String searchDatabase;
  private double deltaMzErrorRate;
  private final Set<AssaySampleCvParam> samples;
  private final Set<AssayQuantificationMethodCvParam> quantificationMethods;
  private String experimentalFactor;
  private final Set<PeakFileSummary> peakFileSummaries;
  private final Set<AssayGroupCvParam> cvParams;
  private final Set<AssayGroupUserParam> userParams;

  /**
   * Default constructor, sets all the variables to default values.
   */
  public AssayFileSummary() {
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
    this.numberofMissingSpectra = -1;
    this.spectrumMatchFragmentIons = true;
    this.missingIdentifiedSpectraIds = new LinkedHashSet<>();
    this.ms2Annotation = false;
    this.chromatogram = false;
    this.ptms = new LinkedHashSet<>();
    this.instruments = new LinkedHashSet<>();
    this.softwares = new LinkedHashSet<>();
    this.contacts = new LinkedHashSet<>();
    this.proteinGroupPresent = false;
    this.samples = new LinkedHashSet<>();
    this.quantificationMethods = new LinkedHashSet<>();
    this.experimentalFactor = null;
    this.peakFileSummaries = new LinkedHashSet<>();
    this.cvParams = new LinkedHashSet<>();
    this.userParams = new LinkedHashSet<>();
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
   * Gets cvParams.
   *
   * @return Value of cvParams.
   */
  public Set<AssayGroupCvParam> getCvParams() {
    return cvParams;
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
   * Gets ptms.
   *
   * @return Value of ptms.
   */
  public Set<AssayPTM> getPtms() {
    return ptms;
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
   * Gets quantificationMethods.
   *
   * @return Value of quantificationMethods.
   */
  public Set<AssayQuantificationMethodCvParam> getQuantificationMethods() {
    return quantificationMethods;
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
   * Gets samples.
   *
   * @return Value of samples.
   */
  public Set<AssaySampleCvParam> getSamples() {
    return samples;
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
   * Gets contacts.
   *
   * @return Value of contacts.
   */
  public Set<Contact> getContacts() {
    return contacts;
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
   * Gets peakFileSummaries.
   *
   * @return Value of peakFileSummaries.
   */
  public Set<PeakFileSummary> getPeakFileSummaries() {
    return peakFileSummaries;
  }

  /**
   * Gets userParams.
   *
   * @return Value of userParams.
   */
  public Set<AssayGroupUserParam> getUserParams() {
    return userParams;
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
   * Gets softwares.
   *
   * @return Value of softwares.
   */
  public Set<Software> getSoftwares() {
    return softwares;
  }

  /**
   * Gets instruments.
   *
   * @return Value of instruments.
   */
  public Set<Instrument> getInstruments() {
    return instruments;
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
    return numberofMissingSpectra;
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
   * Adds PTMs.
   * @param ptms New PTMs to add.
   */
  public void addPtms(Collection<AssayPTM> ptms) {
    this.ptms.addAll(ptms);
  }

  /**
   * Adds Instruments.
   * @param instruments New instruments to add.
   */
  public void addInstruments(Collection<Instrument> instruments) {
    this.instruments.addAll(instruments);
  }

  /**
   * Adds Software.
   * @param software New software to add.
   */
  public void addSoftwares(Collection<Software> software) {
    this.softwares.addAll(software);
  }

  /**
   * Adds Contacts.
   * @param contacts New contacts to add.
   */
  public void addContacts(Collection<Contact> contacts) {
    this.contacts.addAll(contacts);
  }

  /**
   * Adds AssaySampleCvParam samples.
   * @param samples New samples to add.
   */
  public void addSamples(Collection<AssaySampleCvParam> samples) {
    this.samples.addAll(samples);
  }

  /**
   * Adds AssayQuantificationMethodCvParam quantification methods.
   * @param quantificationMethods New quantification methods to add.
   */
  public void addQuantificationMethods(Collection<AssayQuantificationMethodCvParam> quantificationMethods) {
    this.quantificationMethods.addAll(quantificationMethods);
  }

  /**
   * Adds PeakFileSummaries.
   * @param peakFileSummary New peakFileSummary to add.
   */
  public void addPeakFileSummary(PeakFileSummary peakFileSummary) {
    this.peakFileSummaries.add(peakFileSummary);
  }

  /**
   * Adds PeakFileSummaries
   * @param peakFileSummaries New peak file summaries to add.
   */
  public void addPeakFileSummaries(Collection<PeakFileSummary> peakFileSummaries) {
    this.peakFileSummaries.addAll(peakFileSummaries);
  }

  /**
   * Adds AssayGroupCvParams cvParams.
   * @param cvParams New cv Params to add.
   */
  public void addCvParams(Collection<AssayGroupCvParam> cvParams) {
    this.cvParams.addAll(cvParams);
  }

  /**
   * Adds AssayGroupUserParam userParams.
   * @param userParams New userParams to add.
   */
  public void addUserParams(Collection<AssayGroupUserParam> userParams) {
    this.userParams.addAll(userParams);
  }
}

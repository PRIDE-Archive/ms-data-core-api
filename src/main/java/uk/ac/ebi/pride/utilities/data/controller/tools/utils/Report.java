package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.pride.archive.repo.assay.AssayGroupCvParam;
import uk.ac.ebi.pride.archive.repo.assay.AssayGroupUserParam;
import uk.ac.ebi.pride.archive.repo.assay.AssayPTM;
import uk.ac.ebi.pride.archive.repo.assay.Contact;
import uk.ac.ebi.pride.archive.repo.assay.instrument.Instrument;
import uk.ac.ebi.pride.archive.repo.assay.software.Software;
import uk.ac.ebi.pride.archive.repo.assay.software.SoftwareCvParam;
import uk.ac.ebi.pride.archive.repo.assay.software.SoftwareUserParam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides details of assay file(s) as a validation report for key information,
 * e.g. peptide and protein numbers.
 *
 * @author Tobias Ternent
 */
public class Report {
  private String status = "";
  private String fileName = "";
  private String name = "";
  private String shortLabel = "";
  private Set<Contact> contacts = new HashSet<>();
  private int totalProteins = 0;
  private int totalPeptides = 0;
  private int totalSpecra = 0;
  private Set<AssayPTM> uniquePTMs = new HashSet<>();
  private double deltaMzPercent = 0.0;
  private int identifiedSpectra = 0;
  private int missingIdSpectra = 0;
  private boolean matchFragIons = false;
  private int uniquePeptides = 0;
  private Set<Instrument> instruments = new HashSet<>();
  private Set<Software> softwareSet = new HashSet<>();
  private String searchDatabase = "";
  private String exampleProteinAccession = "";
  private boolean proteinGroupPresent = false;
  private Set<AssayGroupCvParam> cvParams = new HashSet<>();
  private Set<AssayGroupUserParam> userParams = new HashSet<>();
  private boolean chromatogram = false;

  /**
   * Default constructor. No variables are set.
   */
  public Report() {
  }

  /**
   * Outputs the report as a String object.
   * @return the report as a properly formatted String.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Status: ").append(status);
    sb.append('\n');
    sb.append("FileName: ").append(fileName);
    sb.append('\n');
    if (!org.apache.commons.lang3.StringUtils.isEmpty(name)) {
      sb.append("Name: ").append(name);
      sb.append('\n');
      sb.append("Shortlabel: ").append(shortLabel);
      sb.append('\n');
      sb.append("Contacts: ").append(contactsToString());
      sb.append('\n');
      sb.append("Instruments: ").append(instrumentsToString());
      sb.append('\n');
      sb.append("Software: ").append(softwareToString());
      sb.append('\n');
      sb.append("SearchDatabase: ").append(searchDatabase);
      sb.append('\n');
      sb.append("ExampleProteinAccession: ").append(exampleProteinAccession);
      sb.append('\n');
      sb.append("ProteinGroupPresent: ").append(proteinGroupPresent);
      sb.append('\n');
      sb.append("Assay Group CvParams: ").append(cvParamsToString());
      sb.append('\n');
      sb.append("Assay Group UserParams: ").append(userParamsToString());
      sb.append('\n');
      sb.append("Chromatogram: ").append(chromatogram);
      sb.append('\n');
      sb.append("Total proteins: ").append(totalProteins);
      sb.append('\n');
      sb.append("Total peptides: ").append(totalPeptides);
      sb.append('\n');
      sb.append("Total unique peptides: ").append(uniquePeptides);
      sb.append('\n');
      sb.append("Total spectra: ").append(totalSpecra);
      sb.append('\n');
      sb.append("Total identified spectra: ").append(identifiedSpectra);
      sb.append('\n');
      sb.append("Total missing spectra: ").append(missingIdSpectra);
      sb.append('\n');
      sb.append("Total unique PTMs: ").append(uniquePTMstoString());
      sb.append('\n');
      sb.append("Delta m/z: ").append(new DecimalFormat("#.0000").format(deltaMzPercent)).append("%");
      sb.append('\n');
      sb.append("Match fragment ions: ").append(matchFragIons);
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Sets the assay summary to be output, and converts this to a String.
   * @param assayFileSummary the assay summary to extract information from.
   * @return the report as a properly formatted String.
   */
  public String toString(AssayFileSummary assayFileSummary) {
    name = assayFileSummary.getName();
    shortLabel = assayFileSummary.getShortLabel();
    contacts = assayFileSummary.getContacts();
    instruments = assayFileSummary.getInstruments();
    softwareSet = assayFileSummary.getSoftwares();
    searchDatabase = assayFileSummary.getSearchDatabase();
    exampleProteinAccession = assayFileSummary.getExampleProteinAccession();
    proteinGroupPresent = assayFileSummary.isProteinGroupPresent();
    cvParams = assayFileSummary.getCvParams();
    userParams = assayFileSummary.getUserParams();
    chromatogram = assayFileSummary.hasChromatogram();
    totalProteins = assayFileSummary.getNumberOfProteins();
    totalPeptides = assayFileSummary.getNumberOfPeptides();
    uniquePeptides = assayFileSummary.getNumberOfUniquePeptides();
    totalSpecra = assayFileSummary.getNumberOfSpectra();
    identifiedSpectra = assayFileSummary.getNumberOfIdentifiedSpectra();
    missingIdSpectra = assayFileSummary.getNumberofMissingSpectra();
    uniquePTMs = assayFileSummary.getPtms();
    deltaMzPercent = new Double(assayFileSummary.getDeltaMzErrorRate()*100.0).intValue();
    matchFragIons = assayFileSummary.isSpectrumMatchFragmentIons();
    return this.toString();
  }

  /**
   * Converts all contacts to a String, joined by ',' separators.
   * @return a String of all the contacts.
   */
  private String contactsToString() {
    List<String> result = new ArrayList<>();
    contacts.forEach(person -> {
      String personToAdd = "[" +
          person.getTitle() +
          "," +
          person.getFirstName() +
          "," +
          person.getLastName() +
          "," +
          person.getAffiliation() +
          "," +
          person.getEmail() +
          "]";
      result.add(personToAdd);
    });
    return StringUtils.join(result, ",");
  }

  /**
   * Converts all instruments to a String, joined by ',' separators.
   * @return a String of all the instruments.
   */
  private String instrumentsToString() {
    List<String> result = new ArrayList<>();
    instruments.stream().forEachOrdered(instrument -> {
      String instrumentToAdd = "[" +
          instrument.getCvParam().getCvLabel() +
          "," +
          instrument.getCvParam().getName() +
          "," +
          instrument.getCvParam().getAccession() +
          "]";
      result.add(instrumentToAdd);
    });
    return StringUtils.join(result, ",");
  }

  /**
   * Converts all unique PTMs to a String, joined by ',' separators.
   * @return a String of all the unique PTMs.
   */
  private String uniquePTMstoString() {
    List<String> result = new ArrayList<>();
    uniquePTMs.stream().forEachOrdered(cvParam -> {
      String cvParamToAdd = "[" +
          cvParam.getCvLabel() +
          "," +
          cvParam.getName() +
          "," +
          cvParam.getAccession() +
          "," +
          cvParam.getValue() +
          "]";
      result.add(cvParamToAdd);
    });
    return StringUtils.join(result, ",");
  }

  /**
   * Converts all unique Softwares to a String, joined by ',' separators.
   * @return a String of all the software.
   */
  private String softwareToString() {
    List<String> result = new ArrayList<>();
    softwareSet.stream().forEachOrdered(software -> {
      StringBuilder softwareToAdd = new StringBuilder();
      softwareToAdd.append("[");
      softwareToAdd.append(software.getName());
      softwareToAdd.append(",");
      softwareToAdd.append(software.getOrder());
      softwareToAdd.append(",");
      softwareToAdd.append(software.getVersion());
      softwareToAdd.append(",");
      softwareToAdd.append(software.getCustomization());
      softwareToAdd.append(",");
      softwareToAdd.append("{");
      List<String> softwareCvPs = new ArrayList<>();
      for (SoftwareCvParam softwareCvParam : software.getSoftwareCvParams()) {
        String cvp = "[" +
            softwareCvParam.getCvParam().getCvLabel() +
            "," +
            softwareCvParam.getCvParam().getName() +
            "," +
            softwareCvParam.getCvParam().getAccession() +
            "," +
            softwareCvParam.getCvParam().getValue() +
            "]";
        softwareCvPs.add(cvp);
      }
      if (softwareCvPs.size()>0) {
        StringUtils.join(softwareCvPs, ",");
      }
      softwareToAdd.append("}");
      softwareToAdd.append(",");
      softwareToAdd.append("{");
      List<String> softwareUserPs = new ArrayList<>();
      for (SoftwareUserParam softwareUserParam : software.getSoftwareUserParams()) {
        String userp = "[" +
            softwareUserParam.getName() +
            "," +
            softwareUserParam.getValue() +
            "]";
        softwareUserPs.add(userp);
      }
      if (softwareUserPs.size()>0) {
        StringUtils.join(softwareUserPs, ",");
      }
      softwareToAdd.append("}");
      softwareToAdd.append("]");
      result.add(softwareToAdd.toString());
    });
    return StringUtils.join(result, ",");
  }

  /**
   * Converts all unique CV Params to a String, joined by ',' separators.
   * @return a String of all the CV Params.
   */
  private String cvParamsToString() {
      StringBuilder sb = new StringBuilder();
      List<String> assayCvPs = new ArrayList<>();
      sb.append("{");
      cvParams.stream().forEachOrdered(assayGroupCvParam -> {
        String assayGroupCvParamToAdd = "[" +
            assayGroupCvParam.getCvLabel() +
            "," +
            assayGroupCvParam.getName() +
            "," +
            assayGroupCvParam.getAccession() +
            "," +
            assayGroupCvParam.getValue() +
            "]";
        assayCvPs.add(assayGroupCvParamToAdd);
      });
      sb.append(StringUtils.join(assayCvPs, ","));
      sb.append("}");
      return sb.toString();
  }

  /**
   * Converts all unique User Params to a String, joined by ',' separators.
   * @return a String of all the User Params.
   */
  private String userParamsToString() {
    StringBuilder sb = new StringBuilder();
    List<String> assayCvPs = new ArrayList<>();
    sb.append("{");
    userParams.stream().forEachOrdered(assayGroupUserParam -> {
      String assayGroupUserParamToAdd = "[" +
          assayGroupUserParam.getName() +
          "," +
          assayGroupUserParam.getValue() +
          "]";
      assayCvPs.add(assayGroupUserParamToAdd);
    });
    sb.append(StringUtils.join(assayCvPs, ","));
    sb.append("}");
    return sb.toString();
  }


  /**
   * Sets new instruments.
   *
   * @param instruments New value of instruments.
   */
  public void setInstruments(Set<Instrument> instruments) {
    this.instruments = instruments;
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
   * Gets instruments.
   *
   * @return Value of instruments.
   */
  public Set<Instrument> getInstruments() {
    return instruments;
  }

  /**
   * Gets identifiedSpectra.
   *
   * @return Value of identifiedSpectra.
   */
  public int getIdentifiedSpectra() {
    return identifiedSpectra;
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
   * Sets new totalProteins.
   *
   * @param totalProteins New value of totalProteins.
   */
  public void setTotalProteins(int totalProteins) {
    this.totalProteins = totalProteins;
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
   * Gets softwareSet.
   *
   * @return Value of softwareSet.
   */
  public Set<Software> getSoftwareSet() {
    return softwareSet;
  }

  /**
   * Sets new fileName.
   *
   * @param fileName New value of fileName.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Sets new identifiedSpectra.
   *
   * @param identifiedSpectra New value of identifiedSpectra.
   */
  public void setIdentifiedSpectra(int identifiedSpectra) {
    this.identifiedSpectra = identifiedSpectra;
  }

  /**
   * Gets totalSpecra.
   *
   * @return Value of totalSpecra.
   */
  public int getTotalSpecra() {
    return totalSpecra;
  }

  /**
   * Gets totalProteins.
   *
   * @return Value of totalProteins.
   */
  public int getTotalProteins() {
    return totalProteins;
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
   * Sets new missingIdSpectra.
   *
   * @param missingIdSpectra New value of missingIdSpectra.
   */
  public void setMissingIdSpectra(int missingIdSpectra) {
    this.missingIdSpectra = missingIdSpectra;
  }

  /**
   * Sets new userParams.
   *
   * @param userParams New value of userParams.
   */
  public void setUserParams(Set<AssayGroupUserParam> userParams) {
    this.userParams = userParams;
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
   * Sets new chromatogram.
   *
   * @param chromatogram New value of chromatogram.
   */
  public void setChromatogram(boolean chromatogram) {
    this.chromatogram = chromatogram;
  }

  /**
   * Sets new deltaMzPercent.
   *
   * @param deltaMzPercent New value of deltaMzPercent.
   */
  public void setDeltaMzPercent(int deltaMzPercent) {
    this.deltaMzPercent = deltaMzPercent;
  }

  /**
   * Sets new status.
   *
   * @param status New value of status.
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Sets new OK status.
   **/
  public void setStatusOK() {
    this.status = "OK";
  }

  /**
   * Sets new OK status.
   **/
  public void setStatusError(String message) {
    this.status = "ERROR\n" + message;
  }

  /**
   * Sets new uniquePTMs.
   *
   * @param uniquePTMs New value of uniquePTMs.
   */
  public void setUniquePTMs(Set<AssayPTM> uniquePTMs) {
    this.uniquePTMs = uniquePTMs;
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
   * Gets missingIdSpectra.
   *
   * @return Value of missingIdSpectra.
   */
  public int getMissingIdSpectra() {
    return missingIdSpectra;
  }

  /**
   * Gets status.
   *
   * @return Value of status.
   */
  public String getStatus() {
    return status;
  }

  /**
   * Gets deltaMzPercent.
   *
   * @return Value of deltaMzPercent.
   */
  public double getDeltaMzPercent() {
    return deltaMzPercent;
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
   * Sets new totalPeptides.
   *
   * @param totalPeptides New value of totalPeptides.
   */
  public void setTotalPeptides(int totalPeptides) {
    this.totalPeptides = totalPeptides;
  }

  /**
   * Gets totalPeptides.
   *
   * @return Value of totalPeptides.
   */
  public int getTotalPeptides() {
    return totalPeptides;
  }

  /**
   * Sets new softwareSet.
   *
   * @param softwareSet New value of softwareSet.
   */
  public void setSoftwareSet(Set<Software> softwareSet) {
    this.softwareSet = softwareSet;
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
   * Sets new proteinGroupPresent.
   *
   * @param proteinGroupPresent New value of proteinGroupPresent.
   */
  public void setProteinGroupPresent(boolean proteinGroupPresent) {
    this.proteinGroupPresent = proteinGroupPresent;
  }

  /**
   * Gets matchFragIons.
   *
   * @return Value of matchFragIons.
   */
  public boolean isMatchFragIons() {
    return matchFragIons;
  }

  /**
   * Sets new cvParams.
   *
   * @param cvParams New value of cvParams.
   */
  public void setCvParams(Set<AssayGroupCvParam> cvParams) {
    this.cvParams = cvParams;
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
   * Sets new totalSpecra.
   *
   * @param totalSpecra New value of totalSpecra.
   */
  public void setTotalSpecra(int totalSpecra) {
    this.totalSpecra = totalSpecra;
  }

  /**
   * Sets new uniquePeptides.
   *
   * @param uniquePeptides New value of uniquePeptides.
   */
  public void setUniquePeptides(int uniquePeptides) {
    this.uniquePeptides = uniquePeptides;
  }

  /**
   * Sets new matchFragIons.
   *
   * @param matchFragIons New value of matchFragIons.
   */
  public void setMatchFragIons(boolean matchFragIons) {
    this.matchFragIons = matchFragIons;
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
   * Sets new contacts.
   *
   * @param contacts New value of contacts.
   */
  public void setContacts(Set<Contact> contacts) {
    this.contacts = contacts;
  }

  /**
   * Gets uniquePTMs.
   *
   * @return Value of uniquePTMs.
   */
  public Set<AssayPTM> getUniquePTMs() {
    return uniquePTMs;
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
   * Gets uniquePeptides.
   *
   * @return Value of uniquePeptides.
   */
  public int getUniquePeptides() {
    return uniquePeptides;
  }

  /**
   * Gets fileName.
   *
   * @return Value of fileName.
   */
  public String getFileName() {
    return fileName;
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
   * Sets new shortLabel.
   *
   * @param shortLabel New value of shortLabel.
   */
  public void setShortLabel(String shortLabel) {
    this.shortLabel = shortLabel;
  }
}

package uk.ac.ebi.pride.utilities.data.io.file;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.LightModelsTransformer;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.lightModel.*;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/** @author Suresh Hewapathirana */
@Slf4j
public class FastMzIdentMLUnmarshallerAdaptor {

  private FastMzIdentMLUnmarshaller fastMzIdentMLUnmarshaller;
  private ModReader modReader;

  /**
   * This method retrieves the MzIdentML object from the FastMzIdentMLUnmarshaller
   *
   * @param mzIdentMLFile MzIdentML file
   * @see FastMzIdentMLUnmarshaller
   */
  public FastMzIdentMLUnmarshallerAdaptor(File mzIdentMLFile) {
    fastMzIdentMLUnmarshaller = FastMzIdentMLUnmarshaller.getInstance(mzIdentMLFile);
    modReader = ModReader.getInstance();
  }

  /**
   * Get the complete MzIdentML object which contains the entire unmarshalled content
   *
   * @return MzIdentML object
   */
  public MzIdentML getMzIdentML() {
    return fastMzIdentMLUnmarshaller.getMzIdentML();
  }

  /**
   * Get all the Protein Ids reported in the MzIdentML
   *
   * @return Collection of Protein Ids
   */
  public Collection<Comparable> getProteinIds() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getSequenceCollection()
        .getDBSequence()
        .parallelStream()
        .map(DBSequence::getId)
        .collect(Collectors.toList());
  }

  /**
   * Get all the Peptide Ids reported in the MzIdentML
   *
   * @return Collection of Peptide Ids
   */
  public Collection<Comparable> getPeptideIds() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getSequenceCollection()
        .getPeptide()
        .parallelStream()
        .map(Peptide::getId)
        .collect(Collectors.toList());
  }

  /**
   * Get all unique Peptide Ids reported in the MzIdentML. Peptides that map uniquely to a Protein
   * ID are considered as unique peptides.
   *
   * @return Collection of Peptide Ids
   */
  public Collection<Comparable> getUniquePeptideIds() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getSequenceCollection()
        .getPeptide()
        .parallelStream()
        .map(Peptide::getId)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Get PeptidoForms. PeptidoForms are different peptide forms that a peptide can have due to
   * various modifications. and various position(s) of the modification(s).
   *
   * @return Number of PeptidoForms
   */
  public int getNumberOfPeptidoForms() {
    int peptideCounts = 0;
    int modificationCounts = 0;

    List<Peptide> peptides =
        fastMzIdentMLUnmarshaller.getMzIdentML().getSequenceCollection().getPeptide();
    for (Peptide peptide : peptides) {
      peptideCounts++;
      for (Modification modification : peptide.getModification()) {
        for (CvParam cvParam : modification.getCvParam()) {
          if (checkCvParam(cvParam)) {
            modificationCounts++;
          }
        }
      }
    }
    return peptideCounts + modificationCounts;
  }

  /**
   * Get Peptide by peptide ID
   *
   * @param peptideID Peptide Reference ID
   * @return Peptide if matching peptide exists, otherwise return a null
   */
  public Peptide getPeptideById(Comparable peptideID) {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getSequenceCollection()
        .getPeptide()
        .stream()
        .filter(p -> p.getId().equals(peptideID))
        .findFirst()
        .orElse(null);
  }

  /**
   * Get all the SpectrumIdentificationList from the MzIdentML
   *
   * @return List of SpectrumIdentificationList
   */
  public List<SpectrumIdentificationList> getSpectrumIdentificationList() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getDataCollection()
        .getAnalysisData()
        .getSpectrumIdentificationList();
  }

  /**
   * Get all the Peptide modifications actually identified. Not the one that was used as search
   * parameter(s) for the identification
   *
   * @return Set of unique PTMs
   */
  public Collection<CvParam> getIdentifiedUniquePTMs() {
    List<Peptide> peptideList =
        fastMzIdentMLUnmarshaller.getMzIdentML().getSequenceCollection().getPeptide();
    Map<String, CvParam> modifications = new HashMap<>();
    for (Peptide peptide : peptideList) {
      for (Modification modification : peptide.getModification()) {
        for (CvParam cvParam : modification.getCvParam()) {
          if (checkCvParam(cvParam)) {
            modifications.put(cvParam.getAccession(), cvParam);
          }
        }
      }
    }
    return modifications.values();
  }

  /**
   * Get all the Search modifications used as search parameter(s) for the identification, they may
   * differ than the identified peptide modifications
   *
   * @return Set of unique Search Modifications
   */
  public Collection<CvParam> getSearchModifications() {
    List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList =
        fastMzIdentMLUnmarshaller
            .getMzIdentML()
            .getAnalysisProtocolCollection()
            .getSpectrumIdentificationProtocol();
    Map<String, CvParam> modifications = new HashMap<>();
    for (SpectrumIdentificationProtocol spectrumIdentificationProtocol :
        spectrumIdentificationProtocolList) {
      for (SearchModification searchModification :
          spectrumIdentificationProtocol.getModificationParams().getSearchModification()) {
        for (CvParam cvParam : searchModification.getCvParam()) {
          if (checkCvParam(cvParam)) {
            modifications.put(cvParam.getAccession(), cvParam);
          }
        }
      }
    }
    return modifications.values();
  }

  /**
   * Given a peptide, this method extract peptide modifications and their Monoisotopic MassDelta
   *
   * @param peptide uk.ac.ebi.pride.utilities.data.lightModel.Peptide type object
   * @return List of PTM Masses
   */
  public List<Double> getPTMMassesFromPeptide(Peptide peptide) {
    List<Double> ptmMasses = new ArrayList<>();
    for (Modification modification : peptide.getModification()) {
      List<CvParam> cvParams = modification.getCvParam();
      for (CvParam cvParam : cvParams) {
        checkCvParam(cvParam);
      }
      double monoMasses = modification.getMonoisotopicMassDelta();
      ptmMasses.add(monoMasses);
    }
    return ptmMasses;
  }

  /**
   * Check if the CvParameter is valid. CvParam should be compliant with PSI-MOD or UNIMOD.
   *
   * @param cvParam Control vocabulary parameter
   * @return boolean value. If CvParam is valid, it returns true.
   */
  private boolean checkCvParam(CvParam cvParam) {
    if (modReader.getPTMbyAccession(cvParam.getAccession()) == null) {
      log.error("Found Invalid CV: " +  cvParam.toString());
      throw new IllegalStateException(
          "Invalid CV term "
              + cvParam.getAccession()
              + " found! Only PSI-MOD and UNIMOD are permitted!");
    }
    return true;
  }

  /**
   * This method extract the Inputs from the MzIdentML object and for each input file, it retrieves
   * the Spectra Data such as file location, file format etc.
   *
   * @return Map of SpectraData grouped by SpectraData ID
   */
  public Map<Comparable, SpectraData> getSpectraDataMap() {
    Inputs inputs = fastMzIdentMLUnmarshaller.getMzIdentML().getDataCollection().getInputs();
    List<SpectraData> spectraDataList = inputs.getSpectraData();
    Map<Comparable, SpectraData> spectraDataMap = null;
    if (spectraDataList != null && spectraDataList.size() > 0) {
      spectraDataMap = new HashMap<>();
      for (SpectraData spectraData : spectraDataList) {
        spectraDataMap.put(spectraData.getId(), spectraData);
      }
    }
    return spectraDataMap;
  }

  /**
   * Get List of ProteinAmbiguityGroup if available in the MzIdentML
   *
   * @return List of ProteinAmbiguityGroup
   */
  public List<ProteinAmbiguityGroup> getProteinAmbiguityGroups() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getDataCollection()
        .getAnalysisData()
        .getProteinDetectionList()
        .getProteinAmbiguityGroup();
  }

  /**
   * Get Id of the MzIdentML tag
   *
   * @return MzIdentML Id
   */
  public String getMzIdentMLId() {

    String id = fastMzIdentMLUnmarshaller.getMzIdentML().getId();
    if (!id.matches(Constants.MZIDENTML_ID_PATTERN)) {
      log.warn("MzIdentML ID contains special characters which may lead to errors!");
    }
    return id;
  }

  /**
   * Name of the MzIdentML file
   *
   * @return
   */
  public String getMzIdentMLName() {
    return fastMzIdentMLUnmarshaller.getMzIdentML().getName();
  }

  /**
   * Get MzIdentML version. This should be compatible with HUPO-PSI MzIdentML versions
   *
   * @return String
   * @see <a href="http://www.psidev.info/mzidentml">HUPO-PSI MzidentML</a>
   */
  public String getVersion() {

    String version = fastMzIdentMLUnmarshaller.getMzIdentML().getVersion();
    if (!version.matches(Constants.VERSION_NUMBER)) {
      try {
        throw new Exception("Invalid MzIdentML Version found!");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return version;
  }


  /**
   * Get all the Cv in the CvList in the MzIdentML
   *
   * @return Collection of Peptide Ids
   */
  public List<Cv> getCvList() {
    return fastMzIdentMLUnmarshaller.getMzIdentML().getCvList().getCv();
  }

  /**
   * Get creation date of the MzIdentML file. This is a optional attribute
   *
   * @return the date that the MzIdentML file created
   */
  public Date getCreationDate() throws Exception {
    Date dateCreation = null;
    String dateInString = fastMzIdentMLUnmarshaller.getMzIdentML().getCreationDate();
    try {
      if (dateInString != "") {
        Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateInString);
        dateCreation = calendar.getTime();
      }
    } catch (Exception ex) {
      throw new Exception("Failed to convert creationDate attribute of the MzIdentML Element", ex);
    }

    return dateCreation;
  }

  public List<SpectraData> getSpectraData() {
    Inputs inputs = fastMzIdentMLUnmarshaller.getMzIdentML().getDataCollection().getInputs();
    return (inputs != null) ? inputs.getSpectraData() : null;
  }

  /**
   * Get Software in MzIdentML
   *
   * @return List of Software
   */
  public List<uk.ac.ebi.pride.utilities.data.core.Software> getSoftwares() {
    List<uk.ac.ebi.pride.utilities.data.core.Software> softwareList = new ArrayList<>();
    for (AnalysisSoftware analysisSoftware :
        fastMzIdentMLUnmarshaller.getMzIdentML().getAnalysisSoftwareList().getAnalysisSoftware()) {
      softwareList.add(LightModelsTransformer.transformToSoftware(analysisSoftware));
    }
    return softwareList;
  }

  /**
   * Get List of person specified in the AuditCollection section of the MzIdentML
   *
   * @return list of person
   */
  public List<Person> getPersonContacts() {
    List<Organization> organizations = fastMzIdentMLUnmarshaller.getMzIdentML().getAuditCollection().getOrganization();
    Map<String, Organization> organizationMap = new HashMap<>(organizations.size());
    for (Organization organization: organizations) {
      organizationMap.put(organization.getId(), organization);
    }

    List<Person> personList = fastMzIdentMLUnmarshaller.getMzIdentML().getAuditCollection().getPerson();
    for (Person person : personList) {
      List<Affiliation> affiliations = person.getAffiliation();
      if(affiliations != null && affiliations.size() > 0){
        for (Affiliation affiliation : affiliations) {
          if((affiliation.getOrganization() == null) &&(organizationMap.containsKey(affiliation.getOrganizationRef()))){
            affiliation.setOrganization(organizationMap.get(affiliation.getOrganizationRef()));
          }
        }
      }
    }
    return personList;
  }

  /**
   * Get List of organization specified in the AuditCollection section of the MzIdentML
   *
   * @return list of organization
   */
  public List<Organization> getOrganizationContacts() {
    return fastMzIdentMLUnmarshaller.getMzIdentML().getAuditCollection().getOrganization();
  }

  /**
   * Get Provider
   *
   * @return Provider object
   */
  public Provider getProvider() {
    return fastMzIdentMLUnmarshaller.getMzIdentML().getProvider();
  }

  /**
   * Get list of Source files including spectra references(peak files) with the file format
   *
    * @return List of SourceFile objects
   */
  public List<SourceFile> getSourceFiles() {
    return LightModelsTransformer.transformToSourceFiles(
        fastMzIdentMLUnmarshaller.getMzIdentML().getDataCollection().getInputs().getSourceFile());
  }

  /**
   * Get the BibliographicReference
   * @return BibliographicReference object
   */
  public List<BibliographicReference> getReferences() {
    return fastMzIdentMLUnmarshaller.getMzIdentML().getBibliographicReference();
  }

  public boolean isSpectraDataReferencedByTitle(SpectraData spectraData) {
    return spectraData.getSpectrumIDFormat() != null
        && spectraData
            .getSpectrumIDFormat()
            .getCvParam()
            .getAccession()
            .equalsIgnoreCase(CvTermReference.MS_ID_FORMAT_WIFF.getAccession())
        && spectraData.getFileFormat() != null
        && spectraData
            .getFileFormat()
            .getCvParam()
            .getAccession()
            .equalsIgnoreCase(CvTermReference.MS_FILE_FORMAT_WIFF.getAccession());
  }

  public List<SearchDatabase> getSearchDatabases() {
    return fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getDataCollection()
        .getInputs()
        .getSearchDatabase();
  }

  public List<Sample> getSampleList() {
    List<Sample> samples = null;
    if (fastMzIdentMLUnmarshaller.getMzIdentML().getAnalysisSampleCollection() != null) {
      samples = fastMzIdentMLUnmarshaller.getMzIdentML().getAnalysisSampleCollection().getSample();
    }
    return samples;
  }

  public List<Enzyme> getEnzymes() {
    List<Enzyme> enzymesList = new ArrayList<>();
    fastMzIdentMLUnmarshaller
        .getMzIdentML()
        .getAnalysisProtocolCollection()
        .getSpectrumIdentificationProtocol()
        .forEach(sip -> sip.getEnzymes().getEnzyme().forEach(enzyme -> enzymesList.add(enzyme)));
    return enzymesList;
  }

  /**
   * Close FastMzIdentMLUnmarshallerAdaptor by clearing the entire mzIdentML Object.
   *
   * <p>Warning: This should be carefully used only after performing all the data access operations.
   * If not, fastMzIdentMLUnmarshaller has to unmarshall MzIdentML file again, which is
   * computationally expensive.
   */
  public void close() {
    fastMzIdentMLUnmarshaller.destroy();
  }

  /**
   * Get number of decoy proteins
   * @return Number of decoy proteins identified
   */
  public long getNumberOfDecoyProteins() {
    return fastMzIdentMLUnmarshaller
            .getMzIdentML().getSequenceCollection().getPeptideEvidence().stream()
            .filter(p -> p.getIsDecoy()).count();
  }
}

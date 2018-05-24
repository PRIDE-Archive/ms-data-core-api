package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.FastMzIdentMLCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.LightModelsTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Enzyme;
import uk.ac.ebi.pride.utilities.data.core.Organization;
import uk.ac.ebi.pride.utilities.data.core.Person;
import uk.ac.ebi.pride.utilities.data.core.Provider;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.io.file.FastMzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.lightModel.*;
import uk.ac.ebi.pride.utilities.data.lightModel.CvParam;
import uk.ac.ebi.pride.utilities.data.lightModel.Peptide;
import uk.ac.ebi.pride.utilities.data.lightModel.SpectraData;
import uk.ac.ebi.pride.utilities.data.lightModel.SpectrumIdentificationList;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * FastMzIdentMLController is a controller supporting for fast access of mzIdentML file designed for
 * EBI submission validation pipeline.
 *
 * @author Suresh Hewapathirana
 */
@Slf4j
public class FastMzIdentMLController extends ReferencedIdentificationController {
  private int numberOfIdentifiedSpectra = 0;
  private double deltaMzErrorRate = 0.0;
  private DataAccessController dataAccessController;
  private FastMzIdentMLUnmarshallerAdaptor unmarshaller;
  private Set<Comparable> missingIdentifiedSpectraIds;
  private Map<String, List<SpectrumIdentificationResult>> SpectrumIdentResultsGroupedBySpectraIDs;
  private static boolean hasProteinAmbiguityGroup;

  /**
   * This constructor forces to cache objects that are required for later use
   *
   * @param inputFile MzIdentML file
   */
  public FastMzIdentMLController(File inputFile) {
    super(inputFile, DataAccessMode.CACHE_AND_SOURCE);
    initialize();
  }

  /**
   * This method mainly instantiate the cache according to the logic specified in
   * FastMzIdentMLCachingStrategy
   */
  protected void initialize() {
    File file = (File) getSource();
    unmarshaller = new FastMzIdentMLUnmarshallerAdaptor(file);
    msDataAccessControllers = new HashMap<>();
    setName(file.getName());
    setType(Type.MZIDENTML);
    setContentCategories(ContentCategory.SPECTRUM);
    setCachingStrategy(new FastMzIdentMLCachingStrategy());
    populateCache();
    setCvlookupMap();
    setHasProteinAmbiguityGroup();
  }

  /**
   * This is an initial scan through all the spectrumIdentificationLists and
   * spectrumIdentificationLists, and cross check if the spectra are available in the peak file.
   * Number of calculations such as Number of missing spectra, identified spectra will be performed.
   */
  public void doSpectraValidation() {
    missingIdentifiedSpectraIds = new HashSet<>();
    SpectrumIdentResultsGroupedBySpectraIDs = new Hashtable<>();

    /* Spectra details extracted from MzIdentML -> DataCollection -> Inputs
    eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData> */
    Map<Comparable, SpectraData> spectraDataMap = unmarshaller.getSpectraDataMap();
    List<SpectrumIdentificationList> spectrumIdentificationLists =
        unmarshaller.getSpectrumIdentificationList();
    for (SpectrumIdentificationList spectrumIdentificationList : spectrumIdentificationLists) {
      // eg: <SpectrumIdentificationResult id="SIR_12" spectrumID="index=35"
      // spectraData_ref="SD_1">...</SpectrumIdentificationResult>
      for (SpectrumIdentificationResult spectrumIdentificationResult :
          spectrumIdentificationList.getSpectrumIdentificationResult()) {
        numberOfIdentifiedSpectra++;
        String spectrumDataRef =
            spectrumIdentificationResult.getSpectraDataRef(); // eg: spectraData_ref="SD_1"
        String spectrumID =
            spectrumIdentificationResult.getSpectrumID(); // eg: spectrumID="index=35"

        SpectraData spectraData =
            spectraDataMap.get(spectrumDataRef); // eg: mgf file location, file format etc
        String formattedSpectrumID =
            MzIdentMLUtils.getSpectrumId(spectraData, spectrumID); // eg: 35
        spectrumIdentificationResult.setFormattedSpectrumID(formattedSpectrumID);
        SpectrumIdentResultsGroupedBySpectraIDs.computeIfAbsent(
                spectrumDataRef, value -> new ArrayList<>())
            .add(spectrumIdentificationResult);
        // check the spectra referenced in the mzIdentML also available in the peak files
        dataAccessController = msDataAccessControllers.get(spectrumDataRef);
        if (!isSpectraInPeakFile(dataAccessController, formattedSpectrumID)) {
          missingIdentifiedSpectraIds.add(formattedSpectrumID);
        }
      }
    }
  }

  /** Load Cv from CvList from MzIdentML */
  private void setCvlookupMap() {
    List<CVLookup> cvLookupList = LightModelsTransformer.transformCVList(unmarshaller.getCvList());
    if (cvLookupList != null && !cvLookupList.isEmpty()) {
      Map<String, CVLookup> cvLookupMap = new HashMap<>();
      for (CVLookup cvLookup : cvLookupList) {
        cvLookupMap.put(cvLookup.getCvLabel(), cvLookup);
      }
      if (cvLookupMap.size() > 0) {
        LightModelsTransformer.setCvLookupMap(cvLookupMap);
      }
    }
  }

  /**
   * Get the FastMzIdentMLUnmarshallerAdaptor to be used by the CacheBuilder Implementation.
   *
   * @return MzIdentMLUnmarshallerAdaptor
   */
  public FastMzIdentMLUnmarshallerAdaptor getUnmarshaller() {
    return unmarshaller;
  }

  /**
   * Get the number of Proteins reported in the MzIdentML file
   *
   * @return Number of Proteins
   */
  @Override
  public int getNumberOfProteins() {
    return unmarshaller.getProteinIds().size();
  }

  /**
   * Get the total number of Peptide Sequences identified in the MzIdentML. In other words, it
   * counts how many <Peptide> elements are there in the MzIdentML file
   *
   * @return Number of Peptides
   */
  @Override
  public int getNumberOfPeptides() {
    return unmarshaller.getPeptideIds().size();
  }

  /**
   * Get PeptidoForms. PeptidoForms are different peptide forms that a peptide can have due to
   * various modifications. and various position(s) of the modification(s).
   *
   * @return Number of PeptidoForms
   */
  public int getNumberOfPeptidoForms() {
    return unmarshaller.getNumberOfPeptidoForms();
  }

  /**
   * Get the total number of unique Peptides identified in the MzIdentML.
   *
   * @return Number of Peptides
   */
  public int getNumberOfUniquePeptides() {
    return unmarshaller.getUniquePeptideIds().size();
  }

  /**
   * Get the number of Spectra in the DataAccessController
   *
   * @return The number of Spectra for DataAccessController
   */
  @Override
  public int getNumberOfSpectra() {
    return super.getNumberOfSpectra();
  }

  /**
   * Get number of Spectra referenced in the MzIdentML, but actually not available in the
   * Spectra/Peak list File
   *
   * @return Number of missing spectra
   */
  @Override
  public int getNumberOfMissingSpectra() {
    return missingIdentifiedSpectraIds.size();
  }

  /**
   * Get the number of MS/MS with at least one peptide identification in the MzIdentML file This is
   * equivalent to number of SpectrumIdentificationList elements in the mzIdentML
   *
   * @return Number of Spectra reported in the MzIdentML file
   */
  @Override
  public int getNumberOfIdentifiedSpectra() {
    return numberOfIdentifiedSpectra;
  }

  /**
   * Get list of Spectra Ids which have been referenced in the MzIdentML, but actually not available
   * in the Spectra/Peak list File
   *
   * @return List of UniquePTMs
   */
  public Set<CvParam> getIdentifiedUniquePTMs() {
    return new HashSet<>(unmarshaller.getIdentifiedUniquePTMs());
  }

  /**
   * Get all the Search modifications used as search parameter(s) for the identification, they may
   * differ than the identified peptide modifications
   *
   * @return Set of unique Search Modifications
   */
  public Collection<CvParam> getSearchModifications() {
    return unmarshaller.getSearchModifications();
  }

  /**
   * Get IDs of Spectra referenced in the MzIdentML, which are actually not available in the
   * Spectra/Peak list File
   *
   * @return List of Spectra Ids
   */
  public Set<Comparable> getMissingIdentifiedSpectraIds() {
    return missingIdentifiedSpectraIds;
  }

  /**
   * Get the List of File Spectra that the MzIdentML use to identified peptides
   *
   * @return List of SpectraData Files associated with MzIdentML.
   */
  @Override
  public List<uk.ac.ebi.pride.utilities.data.core.SpectraData> getSpectraDataFiles() {
    List<Comparable> basedOnTitle = new ArrayList<>();
    if (isSpectrumBasedOnTitle()) {
      basedOnTitle = getSpectraDataBasedOnTitle();
    }
    return LightModelsTransformer.transformToSpectraData(
        unmarshaller.getSpectraData(), basedOnTitle);
  }

  /**
   * Get anchor protein Ids from the Protein Ambiguity Groups
   *
   * @return collection of anchor protein Ids
   */
  public Collection<Comparable> getAnchorProteinIds() {
    Set<Comparable> anchorProteins = new HashSet<>();
    if (hasProteinAmbiguityGroup()) {
      for (ProteinAmbiguityGroup proteinAmbiguityGroup : unmarshaller.getProteinAmbiguityGroups()) {
        anchorProteins.add(getAnchorProteinId(proteinAmbiguityGroup));
      }
    }
    return anchorProteins;
  }

  /**
   * This method finds the anchor protein from the Protein Ambiguity groups by comparing the CV term
   * and returns the Id of the anchor protein
   *
   * @param proteinAmbiguityGroup Protein Ambiguity group
   * @return anchor protein Id.
   */
  private String getAnchorProteinId(ProteinAmbiguityGroup proteinAmbiguityGroup) {
    ProteinDetectionHypothesis anchorPDH = null;

    for (ProteinDetectionHypothesis proteinDetectionHypothesis :
        proteinAmbiguityGroup.getProteinDetectionHypothesis()) {
      for (CvParam cvParam : proteinDetectionHypothesis.getCvParam()) {
        if (cvParam.getAccession().equals(Constants.ANCHOR_PROTEIN)) {
          anchorPDH = proteinDetectionHypothesis;
        }
      }
    }
    return (anchorPDH != null) ? anchorPDH.getDBSequenceRef() : null;
  }

  /**
   * This method randomly selects PSM(but covers different peak files as much as possible), and
   * calculates the delta mass error rate. For more information about PSM random selection, please
   * refer {@link #getRandomPSMs}
   *
   * @param numberOfChecks non negative integer
   * @param deltaThreshold double value
   * @return boolean value. If the delta mass error rate within the threshold, it returns true,
   *     otherwise false
   */
  public double getSampleDeltaMzErrorRate(final int numberOfChecks, final Double deltaThreshold) {
    try {
      if (numberOfChecks > 0) {
        List<SpectrumIdentificationItem> PSMList = getRandomPSMs(numberOfChecks);
        int errorPSMCount = 0;

        for (SpectrumIdentificationItem SpectrumIdentificationItem : PSMList) {
          log.debug("SpectrumIdentificationItem  - " + SpectrumIdentificationItem.getId() + "has been selected for random checkup");
          Boolean isDeltaMassThresholdPassed = checkDeltaMassThreshold(SpectrumIdentificationItem, deltaThreshold);
          if (!isDeltaMassThresholdPassed) {
            errorPSMCount++;
          }
        }
        errorPSMCount++;
        // TODO: Format it to 4 or 6 decimal places
        deltaMzErrorRate =
            new BigDecimal(((double) errorPSMCount / numberOfChecks))
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
      }
    } catch (Exception e) {
      log.error("Error in calculating Sample DeltaMz Error Rate: " + e.getMessage());
    }
    return deltaMzErrorRate;
  }

  /**
   * This method randomly collects a single PSM from each Peak/spectra which were identified, and
   * repeatedly collecting PSM until it reaches the numberOfChecks limit. However, if number of
   * randomly selected PSMs getting exceeded the total number of PSMs reported in the MzIdentML,
   * then the collection process will be stopped and it returns the collected PSMs with a warning.
   *
   * @param numberOfChecks non negative integer
   * @return randomly selected SpectrumIdentificationItem list
   */
  @SuppressWarnings("unchecked")
  private List<SpectrumIdentificationItem> getRandomPSMs(final int numberOfChecks) {
    Random random = new Random();
    List<SpectrumIdentificationItem> selectedPSMs = new ArrayList<>(numberOfChecks);

    while (selectedPSMs.size() < numberOfChecks) {
      for (Map.Entry PSMList : SpectrumIdentResultsGroupedBySpectraIDs.entrySet()) {
        List<SpectrumIdentificationResult> psms =
            (List<SpectrumIdentificationResult>) PSMList.getValue();
        if (psms != null && !psms.isEmpty()) {
          SpectrumIdentificationResult SpectrumIdentificationResult =
              psms.get(random.nextInt(psms.size()));
          SpectrumIdentificationItem psm =
              SpectrumIdentificationResult.getSpectrumIdentificationItem().get(0);
          psm.setFormattedSpectrumID(SpectrumIdentificationResult.getFormattedSpectrumID());
          selectedPSMs.add(psm);
        }
      }
      if (selectedPSMs.size() >= numberOfIdentifiedSpectra) {
        log.warn(
            "Number of checks specified is higher than the number of PSMs! Only "
                + numberOfIdentifiedSpectra
                + " will be performed!");
        break;
      }
    }
    return selectedPSMs;
  }

  /**
   * Check if the Delta Mass is within the threshold value passed as a parameter. Peptide
   * modifications are also included for the calculations.
   *
   * @param spectrumIdentItem SpectrumIdentificationItem object from mzIdentML
   * @param deltaThreshold Non-negative Double value(eg: 4.0)
   * @return boolean
   */
  private boolean checkDeltaMassThreshold(SpectrumIdentificationItem spectrumIdentItem, Double deltaThreshold) {
    boolean isDeltaMassThresholdPassed = true;
    Integer charge = spectrumIdentItem.getChargeState();
    double mz = spectrumIdentItem.getExperimentalMassToCharge();
    String peptideRef = spectrumIdentItem.getPeptideRef();
    Peptide peptide = unmarshaller.getPeptideById(peptideRef);
    if (peptide == null) {
      log.error("Random peptide is null! peptideRef:" + peptideRef);
      isDeltaMassThresholdPassed = false;
    } else {
      List<Double> ptmMasses = unmarshaller.getPTMMassesFromPeptide(peptide);
      if ( mz == -1) {
        isDeltaMassThresholdPassed = false;
      } else {
        Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptide.getPeptideSequence(), mz, charge, ptmMasses);
        if (Math.abs(deltaMass) > deltaThreshold) {
          isDeltaMassThresholdPassed = false;
        }
      }
    }
    if(!isDeltaMassThresholdPassed){
      log.warn("Delta mass threshold failed :" + spectrumIdentItem.getId());
    }
    return isDeltaMassThresholdPassed;
  }

  /**
   * Checks if the spectra available in the peak list. If it is not available, missing spectrumIDs
   * will be collected to the assayFileValidationSummary.
   *
   * @param dataAccessController DataAccessController
   * @param formattedSpectrumID SpectrumID formatted based on the peak list file type
   * @return boolean value, false - if spectra cannot be found in the peak file
   */
  private boolean isSpectraInPeakFile(
      DataAccessController dataAccessController, String formattedSpectrumID) {
    boolean spectraFound = true;
    if (dataAccessController != null) {
      spectraFound = dataAccessController.getSpectrumIds().contains(formattedSpectrumID);
      if (!dataAccessController.getSpectrumIds().contains(formattedSpectrumID)) {
        spectraFound = false;
      }
    }
    return spectraFound;
  }

  /**
   * Get Version of the MzIdentML
   *
   * @return Version in the format of "Major.Minor.Revision"
   */
  public String getVersion() {
    return unmarshaller.getVersion();
  }

  /**
   * Get ID of the MzIdentML
   *
   * @return ID as a String
   */
  public String getMzIdentMLId() {
    return unmarshaller.getMzIdentMLId();
  }

  /**
   * Get the name attribute of the MzIdentML
   *
   * @return Name attribute of the MzIdentML tag
   */
  public String getMzIdentMLName() {
    return unmarshaller.getMzIdentMLName();
  }

  /**
   * In case of MzIdentML, this is not provided.
   *
   * @return "Not Available"
   */
  public String getShortLabel() {
    return "Not Available";
  }

  /**
   * Get the Software reported in the MzIdentML
   *
   * @return List of Software
   */
  public List<Software> getSoftwares() {
    return unmarshaller.getSoftwares();
  }

  /**
   * Get the Software reported in the MzIdentML
   *
   * @return List of Software
   */
  public List<SourceFile> getSourceFiles() {
    return unmarshaller.getSourceFiles();
  }

  /**
   * In case of MzIdentML Experiment Protocol is empty.
   *
   * @return ExperimentProtocol
   */
  public ExperimentProtocol getExperimentProtocol() {
    return null;
  }

  /**
   * Get a list of Organization Contacts
   *
   * @return List<Organization> A List of Organizations
   */
  public List<Organization> getOrganizationContacts() {
    List<Organization> organizations = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        if (unmarshaller.getOrganizationContacts() != null
            && unmarshaller.getOrganizationContacts().size() != 0) {
          organizations =
              LightModelsTransformer.transformToOrganization(
                  unmarshaller.getOrganizationContacts());
        }
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve organization contacts", ex);
      }
    } else {
      organizations = metaData.getOrganizations();
    }

    return organizations;
  }

  /**
   * Get a list of Person Contacts
   *
   * @return List<Person> A list of Persons
   */
  public List<Person> getPersonContacts() {
    List<Person> personList = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        if (unmarshaller.getPersonContacts() != null
            && unmarshaller.getPersonContacts().size() != 0) {
          personList = LightModelsTransformer.transformToPerson(unmarshaller.getPersonContacts());
        }
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve person contacts", ex);
      }
    } else {
      personList = metaData.getPersons();
    }
    return personList;
  }

  /**
   * Get provider of the experiment
   *
   * @return Provider - data provider
   */
  public Provider getProvider() {
    Provider provider = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        if (unmarshaller.getProvider() != null) {
          provider = LightModelsTransformer.transformToProvider(unmarshaller.getProvider());
        }
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve person contacts", ex);
      }
    } else {
      provider = metaData.getProvider();
    }
    return provider;
  }

  /**
   * Get a list of references
   *
   * @return List<Reference> a list of reference objects
   */
  public List<Reference> getReferences() {
    List<Reference> references = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        if (unmarshaller.getReferences() != null) {
          references = LightModelsTransformer.transformToReference(unmarshaller.getReferences());
        }
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve references", ex);
      }
    } else {
      references = metaData.getReferences();
    }
    return references;
  }

  /**
   * Additional is a concept that comes from PRIDE XML Files. In the mzidentml all the concepts of
   * the Additional comes inside different objects. This function construct an Additional Object a
   * relation of creationDate, Original Spectra Data Files and finally the Original software that
   * provide the MzIdentML file.
   *
   * @return ParamGroup a group of cv parameters and user parameters.
   */
  @Override
  public ParamGroup getAdditional() {
    ParamGroup additional = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      // Take information from provider !!!
      Provider provider = getProvider();
      Date creationDate = null;
      try {
        creationDate = unmarshaller.getCreationDate();
      } catch (Exception e) {
        e.printStackTrace();
      }
      List<uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraDataList = getSpectraDataFiles();

      if ((provider != null && provider.getSoftware() != null)
          || creationDate != null
          || !spectraDataList.isEmpty()) {
        additional = new ParamGroup();
        // Get information from last software that provide the file
        if (provider != null && provider.getSoftware() != null)
          additional.addCvParams(provider.getSoftware().getCvParams());

        // Get the information of the creation file
        if (creationDate != null) {
          additional.addCvParam(LightModelsTransformer.transformDateToCvParam(creationDate));
        }
        // Get spectra information as additional
        if (!spectraDataList.isEmpty()) {
          Set<uk.ac.ebi.pride.utilities.data.core.CvParam> cvParamList = new HashSet<>();
          for (uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData : spectraDataList) {
            if (spectraData.getSpectrumIdFormat() != null)
              cvParamList.add(spectraData.getSpectrumIdFormat());
            if (spectraData.getFileFormat() != null) cvParamList.add(spectraData.getFileFormat());
          }
          List<uk.ac.ebi.pride.utilities.data.core.CvParam> list = new ArrayList<>(cvParamList);
          additional.addCvParams(list);
        }
      }
    } else {
      additional = metaData.getAdditional();
    }
    return additional;
  }

  /**
   * Get a list of samples
   *
   * @return List<Sample> a list of sample objects.
   */
  @Override
  public List<Sample> getSamples() {
    List<Sample> samples = null;
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        if (unmarshaller.getSampleList() != null && unmarshaller.getSampleList().size() != 0) {
          samples = LightModelsTransformer.transformToSample(unmarshaller.getSampleList());
        }
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve samples", ex);
      }
    } else {
      samples = metaData.getSamples();
    }
    return samples;
  }

  /**
   * Get the List of Databases used in the Experiment
   *
   * @return List<SearchDataBase> List of SearchDatabases
   */
  public List<SearchDataBase> getSearchDataBases() {
    IdentificationMetaData identificationMetaData = super.getIdentificationMetaData();
    if (identificationMetaData == null) {
      return LightModelsTransformer.transformToSearchDataBase(unmarshaller.getSearchDatabases());
    }
    return identificationMetaData.getSearchDataBases();
  }

  /**
   * Get the List of Enzyme used in the Experiment
   *
   * @return List<Enzyme> List of Enzymes
   */
  public List<Enzyme> getEnzymes() {

    List<Enzyme> enzymes = null;
    if (unmarshaller.getEnzymes() != null && unmarshaller.getEnzymes().size() != 0) {
      enzymes = LightModelsTransformer.transformToEnzyme(unmarshaller.getEnzymes());
    }
    return enzymes;
  }

  /**
   * Get the List of Enzyme used in the Experiment formatted for printing purpose
   *
   * @return List<Enzyme> List of Enzymes
   */
  public String getFormattedEnzymes() {

    List<uk.ac.ebi.pride.utilities.data.lightModel.Enzyme> enzymes = unmarshaller.getEnzymes();
    StringBuilder enzymeBuilder = new StringBuilder();
    if (enzymes == null) {
      return "Information not available";
    }
    for (uk.ac.ebi.pride.utilities.data.lightModel.Enzyme enzymeList : enzymes) {
      ParamList enzymeName = enzymeList.getEnzymeName();
      if (enzymeName == null) {
        continue;
      }
      List<CvParam> enzymeParam = enzymeName.getCvParam();
      for (int i = 0; i < enzymeParam.size(); i++) {
        enzymeBuilder.append(enzymeParam.get(i).getName());
        enzymeBuilder.append(" ");
      }
    }
    if (enzymeBuilder.length() == 0) { // no enzyme name available
      return "Information not available";
    }
    enzymeBuilder.deleteCharAt(enzymeBuilder.length() - 1);
    return enzymeBuilder.toString();
  }

  /**
   * Extract the metadata of the MzIdentML file and make them available in the ExperimentMetaData
   * object and save in the cache. This is also computationally expensive method, therefore before
   * it extract data, it checks if the ExperimentMetaData object already available in the cache.
   *
   * @return ExperimentMetaData
   */
  @Override
  public ExperimentMetaData getExperimentMetaData() {
    ExperimentMetaData metaData = super.getExperimentMetaData();

    if (metaData == null) {
      try {
        String accession = getMzIdentMLId();
        String title = getMzIdentMLName();
        String version = getVersion();
        String shortLabel = getShortLabel();
        ExperimentProtocol protocol = getExperimentProtocol();
        List<SourceFile> sources = getSourceFiles();
        List<Software> software = getSoftwares();
        List<Person> persons = getPersonContacts();
        List<Organization> organizations = getOrganizationContacts();
        Provider provider = getProvider();
        Date creationDate = unmarshaller.getCreationDate();
        List<Reference> references = getReferences();
        List<uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraData = getSpectraDataFiles();
        List<Sample> samples = getSamples();
        ParamGroup additional =
            getAdditional(); // Get Additional Information Related with the Project
        metaData =
            new ExperimentMetaData(
                additional,
                accession,
                title,
                version,
                shortLabel,
                samples,
                software,
                persons,
                sources,
                provider,
                organizations,
                references,
                creationDate,
                null,
                protocol,
                spectraData);
        // store it in the cache
        getCache().store(CacheEntry.EXPERIMENT_METADATA, metaData);
      } catch (Exception ex) {
        throw new DataAccessException("Failed to retrieve meta data", ex);
      }
    }
    return metaData;
  }

  /**
   * MzIdentML files will support in the future Spectra MetaData if is present PRIDE Objects, also
   * by other file Formats.
   *
   * @return The metadata related with mz information
   */
  @Override
  public MzGraphMetaData getMzGraphMetaData() {
    return null;
  }

  /**
   * Returns false of mzIdentML object does not contains any protein ambiguity groups. This has been
   * pre-calculated with the initiation of the class and it is available as a static variable
   *
   * @return boolean value
   */
  @Override
  public boolean hasProteinAmbiguityGroup() {
    return hasProteinAmbiguityGroup;
  }

  /**
   * Check of the mzIdentML object contains any protein ambiguity groups and set the value to a
   * static variable
   */
  private void setHasProteinAmbiguityGroup() {
    AnalysisData analysisData = unmarshaller.getMzIdentML().getDataCollection().getAnalysisData();
    hasProteinAmbiguityGroup =
        analysisData != null
            && analysisData.getProteinDetectionList() != null
            && analysisData.getProteinDetectionList().getProteinAmbiguityGroup() != null
            && analysisData.getProteinDetectionList().getProteinAmbiguityGroup().size() > 0;
  }

  /**
   * Get number of decoy proteins
   *
   * @return Number of decoy proteins identified
   */
  public int getNumberOfDecoyProteins() {
    return (int) unmarshaller.getNumberOfDecoyProteins();
  }
}

package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.lightModel.BibliographicReference;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MapUtils;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.*;

/**
 * This class is responsible for converting light modules (that are used for fast validation) into
 * other model objects or vice versa.
 *
 * NOTE: There are some duplicate code found here because in some transformations of light models, same logic of MzIdentMLTransformer applies here as well.
 * To avoid that, best way to make functions generic, but since both jmzIdentML and light models are not inherited from same parent class.
 * This would be a major refactor, but it would reduce some duplicate code.
 *
 * <p>
 */
@Slf4j
public class LightModelsTransformer {

  private final static Map<String, CVLookup> cvLookupMap = new HashMap<>();

  public static void setCvLookupMap(Map<String, CVLookup> cvLookupList) {
    MapUtils.replaceValuesInMap(cvLookupList, cvLookupMap);
  }

  /**
   * This method converts the Software from
   * uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware to
   * uk.ac.ebi.pride.utilities.data.core.Software object
   *
   * @param analysisSoftware uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware object
   * @return uk.ac.ebi.pride.utilities.data.core.Software object
   */
  public static Software transformToSoftware(
      uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware analysisSoftware) {
    Software software = null;
    if (analysisSoftware != null) {
      try {
        Comparable id = analysisSoftware.getId();
        String nameFromCV = null;
        if (analysisSoftware.getSoftwareName() != null
            && analysisSoftware.getSoftwareName().getCvParam() != null) {
          nameFromCV = analysisSoftware.getSoftwareName().getCvParam().getName();
        }
        String name = (nameFromCV != null) ? nameFromCV : analysisSoftware.getName();
        analysisSoftware.getSoftwareName().getCvParam().getName();
        Contact contact = null;
        String customization = analysisSoftware.getCustomizations();
        String uri = analysisSoftware.getUri();
        String version = analysisSoftware.getVersion();
        software = new Software(id, name, contact, customization, uri, version);
      } catch (Exception ex) {
        log.error(
            "Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware "
                + "to uk.ac.ebi.pride.utilities.data.core.Software");
      }
    }
    return software;
  }

  /**
   * This method converts list of SourceFile from
   * uk.ac.ebi.pride.utilities.data.lightModel.SourceFile to
   * uk.ac.ebi.pride.utilities.data.core.SourceFile objects
   *
   * @param sourceFilesLight List of uk.ac.ebi.pride.utilities.data.lightModel.SourceFile objects
   * @return List of uk.ac.ebi.pride.utilities.data.core.SourceFile objects
   */
  public static List<SourceFile> transformToSourceFiles(
      List<uk.ac.ebi.pride.utilities.data.lightModel.SourceFile> sourceFilesLight) {
    List<SourceFile> sourceFiles = null;
    try {
      if (sourceFilesLight != null) {
        sourceFiles = new ArrayList<>();
        for (uk.ac.ebi.pride.utilities.data.lightModel.SourceFile sourceFileLight :
            sourceFilesLight) {
          String id = sourceFileLight.getId();
          String name = sourceFileLight.getName();
          String location = sourceFileLight.getLocation();
          uk.ac.ebi.pride.utilities.data.lightModel.CvParam fileFormat =
              (sourceFileLight.getFileFormat() != null)
                  ? sourceFileLight.getFileFormat().getCvParam()
                  : null;
          CvParam format = transformToCvParam(fileFormat);
          String formatDocumentation = sourceFileLight.getExternalFormatDocumentation();

          List<CvParam> cvParams = transformToCvParam(sourceFileLight.getCvParam());
          List<UserParam> userParams = transformToUserParam(sourceFileLight.getUserParam());
          sourceFiles.add(
              new SourceFile(
                  new ParamGroup(cvParams, userParams),
                  id,
                  name,
                  location,
                  format,
                  formatDocumentation));
        }
      }
    } catch (Exception ex) {
      log.error(
          "Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.SourceFile "
              + "to uk.ac.ebi.pride.utilities.data.core.SourceFile");
    }
    return sourceFiles;
  }

  /**
   * This method converts list of CvParams from uk.ac.ebi.pride.utilities.data.lightModel.CvParam to
   * uk.ac.ebi.pride.utilities.data.core.CvParam object
   *
   * @param cvParamsLight List of uk.ac.ebi.pride.utilities.data.lightModel.CvParam objects
   * @return List of uk.ac.ebi.pride.utilities.data.core.CvParam objects
   */
  public static List<CvParam> transformToCvParam(
      List<uk.ac.ebi.pride.utilities.data.lightModel.CvParam> cvParamsLight) {
    List<CvParam> cvParams = new ArrayList<>();
    try {
      if (cvParamsLight != null && cvParamsLight.size() != 0) {
        for (uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight : cvParamsLight) {
          cvParams.add(transformToCvParam(cvParamLight));
        }
      }
    } catch (Exception ex) {
      log.error(
          "Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.CvParam "
              + "to uk.ac.ebi.pride.utilities.data.core.CvParam");
    }
    return cvParams;
  }


  /**
   * This method converts set of CvParams from uk.ac.ebi.pride.utilities.data.lightModel.CvParam to
   * uk.ac.ebi.pride.utilities.data.core.CvParam object
   *
   * @param cvParamsLight Set of uk.ac.ebi.pride.utilities.data.lightModel.CvParam objects
   * @return Set of uk.ac.ebi.pride.utilities.data.core.CvParam objects
   */
  public static Set<CvParam> transformToCvParam(Set<uk.ac.ebi.pride.utilities.data.lightModel.CvParam> cvParamsLight) {
    Set<CvParam> cvParams = new HashSet<>();
    try {
      if (cvParamsLight != null && cvParamsLight.size() != 0) {
        for (uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight : cvParamsLight) {
          cvParams.add(transformToCvParam(cvParamLight));
        }
      }
    } catch (Exception ex) {
      log.error(
              "Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.CvParam "
                      + "to uk.ac.ebi.pride.utilities.data.core.CvParam");
    }
    return cvParams;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.CvParam object to
   * uk.ac.ebi.pride.utilities.data.core.CvParam object
   *
   * @param cvParamLight uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight
   * @return uk.ac.ebi.pride.utilities.data.core.CvParam object
   */
  public static CvParam transformToCvParam(
      uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight) {
    CvParam newParam = null;
    String unitCVLookupID = null;
    String cvLookupID = null;

    if (cvParamLight != null) {
      CVLookup cvLookup = cvLookupMap.get(cvParamLight.getCvRef());
      if (cvLookup != null) {
        cvLookupID = cvLookup.getCvLabel();
      }
      CVLookup unitCVLookup = cvLookupMap.get(cvParamLight.getUnitCvRef());
      if (unitCVLookup != null) {
        unitCVLookupID = unitCVLookup.getCvLabel();
      }
      newParam =
          new CvParam(
              cvParamLight.getAccession(),
              cvParamLight.getName(),
              cvLookupID,
              cvParamLight.getValue(),
              cvParamLight.getUnitAccession(),
              cvParamLight.getUnitName(),
              unitCVLookupID);
    }
    return newParam;
  }

  /**
   * This method converts list of UserParam from uk.ac.ebi.pride.utilities.data.lightModel.UserParam
   * to uk.ac.ebi.pride.utilities.data.core.UserParam object
   *
   * @param userParamsLight List of uk.ac.ebi.pride.utilities.data.lightModel.UserParam objects
   * @return List of uk.ac.ebi.pride.utilities.data.core.UserParam objects
   */
  public static List<UserParam> transformToUserParam(
      List<uk.ac.ebi.pride.utilities.data.lightModel.UserParam> userParamsLight) {
    List<UserParam> userParams = null;
    if (userParamsLight != null) {
      userParams = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.UserParam userParam : userParamsLight) {
        userParams.add(transformToUserParam(userParam));
      }
    }
    return userParams;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.UserParam object to
   * uk.ac.ebi.pride.utilities.data.core.UserParam object
   *
   * @param userParam uk.ac.ebi.pride.utilities.data.lightModel.UserParam object
   * @return uk.ac.ebi.pride.utilities.data.core.UserParam object
   */
  public static UserParam transformToUserParam(
      uk.ac.ebi.pride.utilities.data.lightModel.UserParam userParam) {
    UserParam newParam = null;
    if (userParam != null) {
      String unitCVLookupID = null;
      uk.ac.ebi.pride.utilities.data.lightModel.Cv cv = userParam.getUnitCv();
      if (cv != null) unitCVLookupID = cv.getId();
      newParam =
          new UserParam(
              userParam.getName(),
              userParam.getType(),
              userParam.getValue(),
              userParam.getUnitAccession(),
              userParam.getUnitName(),
              unitCVLookupID);
    }
    return newParam;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.Cv object to List of
   * uk.ac.ebi.pride.utilities.data.core.Cv object
   *
   * @param cvList list of uk.ac.ebi.pride.utilities.data.lightModel.Cv object
   * @return list of uk.ac.ebi.pride.utilities.data.core.Cv object
   */
  public static List<CVLookup> transformCVList(List<uk.ac.ebi.pride.utilities.data.lightModel.Cv> cvList) {
    List<CVLookup> cvLookups = null;
    if (cvList != null) {
      cvLookups = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Cv cv : cvList) {
        cvLookups.add(transformToCVLookup(cv));
      }
    }
    return cvLookups;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Cv object to
   * uk.ac.ebi.pride.utilities.data.core.Cv object
   *
   * @param CvLight uk.ac.ebi.pride.utilities.data.lightModel.Cv object
   * @return uk.ac.ebi.pride.utilities.data.core.Cv object
   */
  public static CVLookup transformToCVLookup(uk.ac.ebi.pride.utilities.data.lightModel.Cv CvLight) {
    CVLookup cvLookup = null;
    if (CvLight != null) {
      cvLookup = new CVLookup(CvLight.getId(), CvLight.getFullName(),
              CvLight.getVersion(), CvLight.getUri());
    }
    return cvLookup;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.Person object to List of
   * uk.ac.ebi.pride.utilities.data.core.Person object
   *
   * @param personsLight list of uk.ac.ebi.pride.utilities.data.lightModel.Person object
   * @return list of uk.ac.ebi.pride.utilities.data.core.Person object
   */
  public static List<Person> transformToPerson(
      List<uk.ac.ebi.pride.utilities.data.lightModel.Person> personsLight) {
    List<Person> persons = null;
    if (personsLight != null && personsLight.size() != 0) {
      persons = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Person personLight : personsLight) {
        persons.add(transformToPerson(personLight));
      }
    }
    return persons;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Person object to
   * uk.ac.ebi.pride.utilities.data.core.Person object
   *
   * @param lightPerson uk.ac.ebi.pride.utilities.data.lightModel.Person object
   * @return uk.ac.ebi.pride.utilities.data.core.Person object
   */
  public static Person transformToPerson(
      uk.ac.ebi.pride.utilities.data.lightModel.Person lightPerson) {

    if (lightPerson != null) {
      List<CvParam> cvParams = new ArrayList<>();
      // TODO: Person -> Affiliation -> Organization can be null while parsing MIdentML. As a workaround, they are
//      manually filled in FastMzIdentMLUnmarshellerAdapter
      List<Organization> affiliation =
          transformAffiliationToOrganization(lightPerson.getAffiliation());
      CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
      String firstName = (lightPerson.getFirstName() != null) ? lightPerson.getFirstName() : "";
      String lastName = (lightPerson.getLastName() != null) ? lightPerson.getLastName() : "";
      cvParams.add(
          new CvParam(
              contactTerm.getAccession(),
              contactTerm.getName(),
              contactTerm.getCvLabel(),
              firstName + " " + lastName,
              null,
              null,
              null));
      CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
      StringBuilder organizationStr = new StringBuilder();
      // TODO: Change this - may be there is a performance hit with Streams
      if (affiliation != null
          && affiliation.size() > 0
          && affiliation.stream().anyMatch(Objects::nonNull)) {
        for (Organization organization : affiliation) {
          organizationStr.append(
              (organization.getName() != null) ? organization.getName() + " " : "");
        }
      }
      if (organizationStr.length() != 0)
        cvParams.add(
            new CvParam(
                contactOrg.getAccession(),
                contactOrg.getName(),
                contactOrg.getCvLabel(),
                organizationStr.toString(),
                null,
                null,
                null));
      ParamGroup paramGroup =
          new ParamGroup(
              transformToCvParam(lightPerson.getCvParam()),
              transformToUserParam(lightPerson.getUserParam()));
      paramGroup.addCvParams(cvParams);
      return new Person(
          paramGroup,
          lightPerson.getId(),
          lightPerson.getName(),
          lightPerson.getLastName(),
          lightPerson.getFirstName(),
          lightPerson.getMidInitials(),
          affiliation,
          null);
    }
    return null;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.Organization object to
   * List of uk.ac.ebi.pride.utilities.data.core.Organization object
   *
   * @param lightOrganizations list of uk.ac.ebi.pride.utilities.data.lightModel.Organization object
   * @return list of uk.ac.ebi.pride.utilities.data.core.Organization object
   */
  public static List<Organization> transformToOrganization(
      List<uk.ac.ebi.pride.utilities.data.lightModel.Organization> lightOrganizations) {
    List<Organization> organizations = null;
    if (lightOrganizations != null && lightOrganizations.size() != 0) {
      organizations = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Organization lightOrganization :
          lightOrganizations) {
        // Todo: I need to solve the problem with mail and the parent organization
        organizations.add(transformToOrganization(lightOrganization));
      }
    }
    return organizations;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Organization object to
   * uk.ac.ebi.pride.utilities.data.core.Organization object
   *
   * @param lightOrganization uk.ac.ebi.pride.utilities.data.lightModel.Organization object
   * @return uk.ac.ebi.pride.utilities.data.core.Organization object
   */
  public static Organization transformToOrganization(
      uk.ac.ebi.pride.utilities.data.lightModel.Organization lightOrganization) {
    Organization organization = null;
    if (lightOrganization != null) {
      Organization parentOrganization = null;
      if (lightOrganization.getParent() != null) {
        parentOrganization =
            transformToOrganization(lightOrganization.getParent().getOrganization());
      }
      organization =
          new Organization(
              new ParamGroup(
                  transformToCvParam(lightOrganization.getCvParam()),
                  transformToUserParam(lightOrganization.getUserParam())),
              lightOrganization.getId(),
              lightOrganization.getName(),
              parentOrganization,
              null);
    }
    return organization;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.Affiliation object to
   * List of uk.ac.ebi.pride.utilities.data.core.Affiliation object
   *
   * @param lightAffiliations list of uk.ac.ebi.pride.utilities.data.lightModel.Affiliation object
   * @return list of uk.ac.ebi.pride.utilities.data.core.Affiliation object
   */
  public static List<Organization> transformAffiliationToOrganization(
      List<uk.ac.ebi.pride.utilities.data.lightModel.Affiliation> lightAffiliations) {
    List<Organization> organizations = null;
    if (lightAffiliations != null && lightAffiliations.size() != 0) {
      organizations = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Affiliation lightAffiliation :
          lightAffiliations) {
        uk.ac.ebi.pride.utilities.data.lightModel.Organization lightOrganization =
            lightAffiliation.getOrganization();
        organizations.add(transformToOrganization(lightOrganization));
      }
    }
    return organizations;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Provider object to
   * uk.ac.ebi.pride.utilities.data.core.Provider object
   *
   * @param lightProvider uk.ac.ebi.pride.utilities.data.lightModel.Provider object
   * @return uk.ac.ebi.pride.utilities.data.core.Provider object
   */
  public static Provider transformToProvider(
      uk.ac.ebi.pride.utilities.data.lightModel.Provider lightProvider) {
    Provider provider = null;
    Contact contact = null;
    CvParam role = null;
    if (lightProvider != null) {
      if (lightProvider.getContactRole() != null) {
        if (lightProvider.getContactRole().getOrganization() != null) {
          contact = transformToOrganization(lightProvider.getContactRole().getOrganization());
        } else if (lightProvider.getContactRole().getPerson() != null) {
          contact = transformToPerson(lightProvider.getContactRole().getPerson());
        }
        role = transformToCvParam(lightProvider.getContactRole().getRole().getCvParam());
      }
      Software software = transformToSoftware(lightProvider.getSoftware());
      provider =
          new Provider(lightProvider.getId(), lightProvider.getName(), software, contact, role);
    }
    return provider;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.BibliographicReference
   * object to List of uk.ac.ebi.pride.utilities.data.core.BibliographicReference object
   *
   * @param bibliographicReference list of
   *     uk.ac.ebi.pride.utilities.data.lightModel.BibliographicReference object
   * @return list of uk.ac.ebi.pride.utilities.data.core.BibliographicReference object
   */
  public static List<Reference> transformToReference(
      List<uk.ac.ebi.pride.utilities.data.lightModel.BibliographicReference>
          bibliographicReference) {
    List<Reference> references = new ArrayList<>();
    Iterator<BibliographicReference> iterator = bibliographicReference.iterator();
    while (iterator.hasNext()) {
      uk.ac.ebi.pride.utilities.data.lightModel.BibliographicReference ref = iterator.next();
      // RefLine Trying to use the same approach of pride converter
      String refLine =
          ((ref.getAuthors() != null) ? ref.getAuthors() + ". " : "")
              + ((ref.getYear() != null) ? "(" + ref.getYear().toString() + "). " : "")
              + ((ref.getTitle() != null) ? ref.getTitle() + " " : "")
              + ((ref.getPublication() != null) ? ref.getPublication() + " " : "")
              + ((ref.getVolume() != null) ? ref.getVolume() + "" : "")
              + ((ref.getIssue() != null) ? "(" + ref.getIssue() + ")" : "")
              + ((ref.getPages() != null) ? ":" + ref.getPages() + "." : "");
      // create the ref
      // Todo: Set the References ParamGroup for references
      String year = (ref.getYear() == null) ? null : ref.getYear().toString();
      Reference reference =
          new Reference(
              ref.getId(),
              ref.getName(),
              ref.getDoi(),
              ref.getTitle(),
              ref.getPages(),
              ref.getIssue(),
              ref.getVolume(),
              year,
              ref.getEditor(),
              ref.getPublisher(),
              ref.getPublication(),
              ref.getAuthors(),
              refLine);
      references.add(reference);
    }
    return references;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.SpectraData object to
   * uk.ac.ebi.pride.utilities.data.core.SpectraData object
   *
   * @param lightSpectraData uk.ac.ebi.pride.utilities.data.lightModel.SpectraData object
   * @param mgfTitle Boolean value - If the Spectra refer by title instead of referred by the index
   * @return uk.ac.ebi.pride.utilities.data.core.SpectraData object
   */
  public static SpectraData transformToSpectraData(
      uk.ac.ebi.pride.utilities.data.lightModel.SpectraData lightSpectraData, boolean mgfTitle) {
    SpectraData spectraData = null;
    CvParam fileFormat = null;
    CvParam spectrumId = null;
    String location = null;
    String name = null;

    if (lightSpectraData != null) {
      if (!mgfTitle) {
        if (lightSpectraData.getFileFormat() != null) {
          fileFormat = transformToCvParam(lightSpectraData.getFileFormat().getCvParam());
        }
        if (lightSpectraData.getSpectrumIDFormat().getCvParam() != null) {
          spectrumId = transformToCvParam(lightSpectraData.getSpectrumIDFormat().getCvParam());
        }
        spectraData =
            new SpectraData(
                lightSpectraData.getId(),
                lightSpectraData.getName(),
                lightSpectraData.getLocation(),
                fileFormat,
                lightSpectraData.getExternalFormatDocumentation(),
                spectrumId);
      } else {
        if (lightSpectraData.getFileFormat() != null) {
          fileFormat = MzIdentMLUtils.getFileFormatMGFTitle();
        }
        if (lightSpectraData.getSpectrumIDFormat().getCvParam() != null) {
          spectrumId = MzIdentMLUtils.getSpectrumIdFormatMGFTitle();
        }
        if (lightSpectraData.getLocation() != null) {
          location =
              lightSpectraData
                  .getLocation()
                  .replaceAll("(?i)" + Constants.WIFF_EXT, Constants.MGF_EXT);
        }
        if (lightSpectraData.getName() != null) {
          name =
              lightSpectraData.getName().replaceAll("(?i)" + Constants.WIFF_EXT, Constants.MGF_EXT);
        }
        spectraData =
            new SpectraData(
                lightSpectraData.getId(),
                name,
                location,
                fileFormat,
                lightSpectraData.getExternalFormatDocumentation(),
                spectrumId);
      }
    }
    return spectraData;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.SpectraData object to
   * uk.ac.ebi.pride.utilities.data.core.SpectraData object
   *
   * @param lightSpectraDataList list of uk.ac.ebi.pride.utilities.data.lightModel.SpectraData
   *     object
   * @param usedTitle Boolean value - If the Spectra refer by title instead of referred by the index
   * @return uk.ac.ebi.pride.utilities.data.core.SpectraData object
   */
  public static List<SpectraData> transformToSpectraData(
      List<uk.ac.ebi.pride.utilities.data.lightModel.SpectraData> lightSpectraDataList,
      List<Comparable> usedTitle) {
    List<SpectraData> spectraDatas = null;
    if (lightSpectraDataList != null && lightSpectraDataList.size() != 0) {
      spectraDatas = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.SpectraData lightSpectraData :
          lightSpectraDataList) {
        spectraDatas.add(
            transformToSpectraData(lightSpectraData, usedTitle.contains(lightSpectraData.getId())));
      }
    }
    return spectraDatas;
  }

  /**
   * This method converts Date to CVParameter
   *
   * @param creationDate Date
   * @return uk.ac.ebi.pride.utilities.data.core.CvParam object
   */
  public static CvParam transformDateToCvParam(Date creationDate) {
    CvTermReference cvTerm = CvTermReference.EXPERIMENT_GLOBAL_CREATIONDATE;
    return new CvParam(
        cvTerm.getAccession(),
        cvTerm.getName(),
        cvTerm.getCvLabel(),
        creationDate.toString(),
        null,
        null,
        null);
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase object to
   * List of uk.ac.ebi.pride.utilities.data.core.SearchDatabase object
   *
   * @param lightSearchDatabases list of uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase
   *     object
   * @return list of uk.ac.ebi.pride.utilities.data.core.SearchDatabase object
   */
  public static List<SearchDataBase> transformToSearchDataBase(
      List<uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase> lightSearchDatabases) {
    List<SearchDataBase> searchDataBases = null;
    if (lightSearchDatabases != null) {
      searchDataBases = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase lightSearchDatabase :
          lightSearchDatabases) {
        searchDataBases.add(transformToSeachDatabase(lightSearchDatabase));
      }
    }
    return searchDataBases;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase object to
   * uk.ac.ebi.pride.utilities.data.core.SearchDatabase object
   *
   * @param lightDatabase uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase object
   * @return uk.ac.ebi.pride.utilities.data.core.SearchDatabase object
   */
  public static SearchDataBase transformToSeachDatabase(
      uk.ac.ebi.pride.utilities.data.lightModel.SearchDatabase lightDatabase) {
    CvParam fileFormat = null;
    String releaseDate = null;
    int dataBaseSeq = -1;
    int dataBaseRes = -1;

    String name = (lightDatabase != null) ? lightDatabase.getName() : null;
    if ((lightDatabase != null ? lightDatabase.getFileFormat() : null) != null) {
      fileFormat = transformToCvParam(lightDatabase.getFileFormat().getCvParam());
    }
    if (lightDatabase.getReleaseDate() != null) {
      releaseDate = lightDatabase.getReleaseDate().toString();
    }
    if (lightDatabase.getNumDatabaseSequences() != null) {
      dataBaseSeq = lightDatabase.getNumDatabaseSequences().intValue();
    }
    if (lightDatabase.getNumResidues() != null) {
      dataBaseRes = lightDatabase.getNumResidues().intValue();
    }
    ParamGroup nameOfDatabase = null;
    if (lightDatabase.getDatabaseName() != null) {
      nameOfDatabase =
          new ParamGroup(
              transformToCvParam(lightDatabase.getDatabaseName().getCvParam()),
              transformToUserParam(lightDatabase.getDatabaseName().getUserParam()));
    }
    if (name == null) {
      if (!(nameOfDatabase != null && nameOfDatabase.getCvParams().isEmpty())) {
        name = getValueFromCvTerm(nameOfDatabase.getCvParams().get(0));
      }
      if (name == null && !nameOfDatabase.getUserParams().isEmpty()) {
        name = getValueFromCvTerm(nameOfDatabase.getUserParams().get(0));
      }
      if (name == null) {
        name = lightDatabase.getId();
      }
    }
    return new SearchDataBase(
        lightDatabase.getId(),
        name,
        lightDatabase.getLocation(),
        fileFormat,
        lightDatabase.getExternalFormatDocumentation(),
        lightDatabase.getVersion(),
        releaseDate,
        dataBaseSeq,
        dataBaseRes,
        nameOfDatabase,
        transformToCvParam(lightDatabase.getCvParam()));
  }

  /**
   * To get the information of a cvterm or user param and put in an String we normally take firstly
   * the value of the Parameter and if is not provided we take the name. This function is important
   * when information like the name of the object is not provide and the writers use only CvTerms.
   *
   * @param cvTerm The CVTerm
   * @return An String with the Value
   */
  public static String getValueFromCvTerm(Parameter cvTerm) {
    String originalValue = null;
    if (cvTerm.getValue() != null && cvTerm.getValue().length() > 0) {
      originalValue = cvTerm.getValue();
    } else if (cvTerm.getName() != null && cvTerm.getName().length() > 0) {
      originalValue = cvTerm.getName();
    }
    return originalValue;
  }

  /**
   * Transform light Samples to Samples
   *
   * @param lightSamples light uk.ac.ebi.pride.utilities.data.lightModel.Sample Objects
   * @return List of uk.ac.ebi.pride.utilities.data.core.Samples objects
   */
  public static List<Sample> transformToSample(
      List<uk.ac.ebi.pride.utilities.data.lightModel.Sample> lightSamples) {
    List<Sample> samples = null;
    if (lightSamples != null && lightSamples.size() != 0) {
      samples = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Sample lightSample : lightSamples) {
        samples.add(transformToSample(lightSample));
      }
    }
    return samples;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.SubSample object to List
   * of uk.ac.ebi.pride.utilities.data.core.SubSample object
   *
   * @param lightSamples list of uk.ac.ebi.pride.utilities.data.lightModel.SubSample object
   * @return list of uk.ac.ebi.pride.utilities.data.core.SubSample object
   */
  public static List<Sample> transformSubSampleToSample(
      List<uk.ac.ebi.pride.utilities.data.lightModel.SubSample> lightSamples) {
    List<Sample> samples = null;
    if (lightSamples != null && lightSamples.size() != 0) {
      samples = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.SubSample lightSubSample : lightSamples) {
        samples.add(transformToSample(lightSubSample.getSample()));
      }
    }
    return samples;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Sample object to
   * uk.ac.ebi.pride.utilities.data.core.Sample object
   *
   * @param lightSample uk.ac.ebi.pride.utilities.data.lightModel.Sample object
   * @return uk.ac.ebi.pride.utilities.data.core.Sample object
   */
  public static Sample transformToSample(
      uk.ac.ebi.pride.utilities.data.lightModel.Sample lightSample) {
    Sample sample = null;
    if (lightSample != null) {
      Map<Contact, CvParam> role = transformToRoleList(lightSample.getContactRole());
      List<Sample> subSamples = null;
      if ((lightSample.getSubSample() != null) && (!lightSample.getSubSample().isEmpty())) {
        subSamples = transformSubSampleToSample(lightSample.getSubSample());
      }
      sample =
          new Sample(
              new ParamGroup(
                  transformToCvParam(lightSample.getCvParam()),
                  transformToUserParam(lightSample.getUserParam())),
              lightSample.getId(),
              lightSample.getName(),
              subSamples,
              role);
    }
    return sample;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.ContactRole object to
   * List of uk.ac.ebi.pride.utilities.data.core.ContactRole object
   *
   * @param contactRoles list of uk.ac.ebi.pride.utilities.data.lightModel.ContactRole object
   * @return list of uk.ac.ebi.pride.utilities.data.core.ContactRole object
   */
  public static Map<Contact, CvParam> transformToRoleList(
      List<uk.ac.ebi.pride.utilities.data.lightModel.ContactRole> contactRoles) {
    Map<Contact, CvParam> contacts = null;
    if (contactRoles != null) {
      contacts = new HashMap<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.ContactRole lightRole : contactRoles) {
        Contact contact = null;
        if (lightRole.getOrganization() != null) {
          contact = transformToOrganization(lightRole.getOrganization());
        } else if (lightRole.getPerson() != null) {
          contact = transformToPerson(lightRole.getPerson());
        }
        CvParam role = transformToCvParam(lightRole.getRole().getCvParam());
        contacts.put(contact, role);
      }
    }
    return contacts;
  }

  /**
   * This method converts List of uk.ac.ebi.pride.utilities.data.lightModel.Enzyme object to List of
   * uk.ac.ebi.pride.utilities.data.core.Enzyme object
   *
   * @param oldEnzymes list of uk.ac.ebi.pride.utilities.data.lightModel.Enzyme object
   * @return list of uk.ac.ebi.pride.utilities.data.core.Enzyme object
   */
  public static List<Enzyme> transformToEnzyme(
      List<uk.ac.ebi.pride.utilities.data.lightModel.Enzyme> oldEnzymes) {
    List<Enzyme> enzymes = null;
    if (oldEnzymes != null) {
      enzymes = new ArrayList<>();
      for (uk.ac.ebi.pride.utilities.data.lightModel.Enzyme oldEnzyme : oldEnzymes) {
        enzymes.add(transformToEnzyme(oldEnzyme));
      }
    }
    return enzymes;
  }

  /**
   * This method converts uk.ac.ebi.pride.utilities.data.lightModel.Enzyme object to
   * uk.ac.ebi.pride.utilities.data.core.Enzyme object
   *
   * @param oldEnzyme uk.ac.ebi.pride.utilities.data.lightModel.Enzyme object
   * @return uk.ac.ebi.pride.utilities.data.core.Enzyme object
   */
  private static Enzyme transformToEnzyme(
      uk.ac.ebi.pride.utilities.data.lightModel.Enzyme oldEnzyme) {
    Enzyme newEnzyme = null;
    List<CvParam> cvParams = null;
    List<UserParam> userParams = null;

    if (oldEnzyme != null) {
      boolean specific = false;
      int misscleavage = 0;
      int mindistance = -1;
      if (oldEnzyme.getEnzymeName() != null) {
        cvParams = transformToCvParam(oldEnzyme.getEnzymeName().getCvParam());
      }
      if (oldEnzyme.getEnzymeName() != null) {
        userParams = transformToUserParam(oldEnzyme.getEnzymeName().getUserParam());
      }
      newEnzyme =
          new Enzyme(
              oldEnzyme.getId(),
              oldEnzyme.getName(),
              specific,
              misscleavage,
              mindistance,
              new ParamGroup(cvParams, userParams),
              null);
    }
    return newEnzyme;
  }
}

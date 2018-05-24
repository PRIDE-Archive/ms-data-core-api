package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.DBSequence;
import uk.ac.ebi.pride.utilities.data.core.Enzyme;
import uk.ac.ebi.pride.utilities.data.core.Filter;
import uk.ac.ebi.pride.utilities.data.core.MassTable;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Organization;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.PeptideEvidence;
import uk.ac.ebi.pride.utilities.data.core.Person;
import uk.ac.ebi.pride.utilities.data.core.Provider;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.SearchModification;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentification;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentificationProtocol;
import uk.ac.ebi.pride.utilities.data.core.SubstitutionModification;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MapUtils;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import java.util.*;

/**
 * This class is the Transformer class from a jmzidml object to a core object.
 * It is used by MzIdentMLTransformer to convert jmzidml native objects to the ms-core-api
 * Objects.
 * <p/>
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 19/09/11
 * Time: 16:08
 */
public final class MzIdentMLTransformer {

    /**
     * Private Constructor
     */
    private MzIdentMLTransformer() {

    }

    private final static Map<String, IdentifiableParamGroup> fragmentationTable = new HashMap<String, IdentifiableParamGroup>();

    private final static Map<String, CVLookup> cvLookupMap = new HashMap<String, CVLookup>();

    public static void setCvLookupMap(Map<String, CVLookup> cvLookupList) {
        MapUtils.replaceValuesInMap(cvLookupList, cvLookupMap);
    }

    public static void setFragmentationTable(Map<String, IdentifiableParamGroup> fragTable) {
        MapUtils.replaceValuesInMap(fragTable, fragmentationTable);
    }

    public static List<SourceFile> transformToSourceFile(List<uk.ac.ebi.jmzidml.model.mzidml.SourceFile> oldSourceFiles) {
        List<SourceFile> sourceFiles = null;

        if (oldSourceFiles != null) {
            sourceFiles = new ArrayList<SourceFile>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SourceFile oldsourcefile : oldSourceFiles) {
                String id = oldsourcefile.getId();
                String name = oldsourcefile.getName();
                String location = oldsourcefile.getLocation();
                uk.ac.ebi.jmzidml.model.mzidml.CvParam fileFormat = (oldsourcefile.getFileFormat() != null) ? oldsourcefile.getFileFormat().getCvParam() : null;
                CvParam format = transformToCvParam(fileFormat);
                String formatDocumentation = oldsourcefile.getExternalFormatDocumentation();
                List<CvParam> cvParams = transformToCvParam(oldsourcefile.getCvParam());
                List<UserParam> userParams = transformToUserParam(oldsourcefile.getUserParam());
                sourceFiles.add(new SourceFile(new ParamGroup(cvParams, userParams), id, name, location, format, formatDocumentation));
            }
        }
        return sourceFiles;
    }

    private static List<UserParam> transformToUserParam(List<uk.ac.ebi.jmzidml.model.mzidml.UserParam> oldUserParams) {
        List<UserParam> userParams = null;
        if (oldUserParams != null) {
            userParams = new ArrayList<UserParam>();
            for (uk.ac.ebi.jmzidml.model.mzidml.UserParam oldUserParam : oldUserParams) {
                userParams.add(transformToUserParam(oldUserParam));
            }
        }
        return userParams;
    }

    private static List<CvParam> transformToCvParam(List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> oldCvParams) {
        List<CvParam> cvParams = new ArrayList<CvParam>();
        if (oldCvParams != null && oldCvParams.size() != 0) {
            for (uk.ac.ebi.jmzidml.model.mzidml.CvParam oldCvParam : oldCvParams) {
                cvParams.add(transformToCvParam(oldCvParam));
            }
        }
        return cvParams;
    }

    public static List<Organization> transformToOrganization(List<uk.ac.ebi.jmzidml.model.mzidml.Organization> oldOrganizations) {
        List<Organization> organizations = null;
        if (oldOrganizations != null) {
            organizations = new ArrayList<Organization>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Organization oldOrganization : oldOrganizations) {
                //Todo: I need to solve the problem with mail and the parent organization
                organizations.add(transformToOrganization(oldOrganization));
            }
        }
        return organizations;
    }

    public static Organization transformToOrganization(uk.ac.ebi.jmzidml.model.mzidml.Organization oldOrganization) {
        Organization organization = null;
        if (oldOrganization != null) {
            Organization parentOrganization = null;
            if (oldOrganization.getParent() != null) {
                parentOrganization = transformToOrganization(oldOrganization.getParent().getOrganization());
            }
            organization = new Organization(new ParamGroup(transformToCvParam(oldOrganization.getCvParam()), transformToUserParam(oldOrganization.getUserParam())), oldOrganization.getId(), oldOrganization.getName(), parentOrganization, null);
        }
        return organization;
    }

    public static List<Organization> transformAffiliationToOrganization(List<uk.ac.ebi.jmzidml.model.mzidml.Affiliation> oldAffiliations) {
        List<Organization> organizations = null;
        if (oldAffiliations != null) {
            organizations = new ArrayList<Organization>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Affiliation oldAffiliation : oldAffiliations) {
                uk.ac.ebi.jmzidml.model.mzidml.Organization oldOrganization = oldAffiliation.getOrganization();
                organizations.add(transformToOrganization(oldOrganization));
            }
        }
        return organizations;
    }

    public static List<Person> transformToPerson(List<uk.ac.ebi.jmzidml.model.mzidml.Person> oldPersons) {
        List<Person> persons = null;
        if (oldPersons != null) {
            persons = new ArrayList<Person>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Person oldPerson : oldPersons) {
                persons.add(transformToPerson(oldPerson));
            }
        }
        return persons;
    }

    public static Person transformToPerson(uk.ac.ebi.jmzidml.model.mzidml.Person oldPerson) {

        if (oldPerson != null) {
            List<CvParam> cvParams = new ArrayList<CvParam>();
            List<Organization> affiliation = transformAffiliationToOrganization(oldPerson.getAffiliation());
            CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
            String firstName = (oldPerson.getFirstName() != null) ? oldPerson.getFirstName() : "";
            String lastName = (oldPerson.getLastName() != null) ? oldPerson.getLastName() : "";
            cvParams.add(new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), firstName + " " + lastName, null, null, null));
            CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
            String organizationStr = "";
            for (Organization organization : affiliation) {
                organizationStr += (organization.getName() != null) ? organization.getName() + " " : "";
            }
            if (organizationStr.length() != 0)
                cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), organizationStr, null, null, null));
            ParamGroup paramGroup = new ParamGroup(transformToCvParam(oldPerson.getCvParam()), transformToUserParam(oldPerson.getUserParam()));
            paramGroup.addCvParams(cvParams);
            return new Person(paramGroup, oldPerson.getId(), oldPerson.getName(), oldPerson.getLastName(), oldPerson.getFirstName(), oldPerson.getMidInitials(), affiliation, null);
        }

        return null;
    }

    /**
     * Transform old Samples to Samples
     * @param oldSamples old Samples Objects from jmzidentml
     * @return List of Samples
     */

    public static List<Sample> transformToSample(List<uk.ac.ebi.jmzidml.model.mzidml.Sample> oldSamples) {
        List<Sample> samples = null;
        if (oldSamples != null) {
            samples = new ArrayList<Sample>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Sample oldSample : oldSamples) {
                samples.add(transformToSample(oldSample));
            }
        }
        return samples;
    }

    public static List<Sample> transformSubSampleToSample(List<uk.ac.ebi.jmzidml.model.mzidml.SubSample> oldSamples) {
        List<Sample> samples = null;
        if (oldSamples != null) {
            samples = new ArrayList<Sample>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SubSample oldSubSample : oldSamples) {
                samples.add(transformToSample(oldSubSample.getSample()));
            }
        }
        return samples;
    }

    public static Sample transformToSample(uk.ac.ebi.jmzidml.model.mzidml.Sample oldSample) {
        Sample sample = null;
        if (oldSample != null) {
            Map<Contact, CvParam> role = transformToRoleList(oldSample.getContactRole());
            List<Sample> subSamples = null;
            if ((oldSample.getSubSample() != null) && (!oldSample.getSubSample().isEmpty())) {
                subSamples = transformSubSampleToSample(oldSample.getSubSample());
            }
            sample = new Sample(new ParamGroup(transformToCvParam(oldSample.getCvParam()), transformToUserParam(oldSample.getUserParam())), oldSample.getId(), oldSample.getName(), subSamples, role);
        }
        return sample;
    }

    private static Map<Contact, CvParam> transformToRoleList(List<uk.ac.ebi.jmzidml.model.mzidml.ContactRole> contactRoles) {
        Map<Contact, CvParam> contacts = null;
        if (contactRoles != null) {
            contacts = new HashMap<Contact, CvParam>();
            for (uk.ac.ebi.jmzidml.model.mzidml.ContactRole oldRole : contactRoles) {
                Contact contact = null;
                if (oldRole.getOrganization() != null) {
                    contact = transformToOrganization(oldRole.getOrganization());
                } else if (oldRole.getPerson() != null) {
                    contact = transformToPerson(oldRole.getPerson());
                }
                CvParam role = transformToCvParam(oldRole.getRole().getCvParam());
                contacts.put(contact, role);
            }
        }
        return contacts;
    }

    private static CvParam transformToCvParam(uk.ac.ebi.jmzidml.model.mzidml.CvParam oldCvParam) {
        CvParam newParam = null;
        if (oldCvParam != null) {
            String cvLookupID = null;
            CVLookup cvLookup = cvLookupMap.get(oldCvParam.getCvRef());
            if (cvLookup != null) {
                cvLookupID = cvLookup.getCvLabel();
            }
            String unitCVLookupID = null;
            CVLookup unitCVLookup = cvLookupMap.get(oldCvParam.getUnitCvRef());
            if (unitCVLookup != null) {
                unitCVLookupID = unitCVLookup.getCvLabel();
            }
            newParam = new CvParam(oldCvParam.getAccession(),
                    oldCvParam.getName(),
                    cvLookupID,
                    oldCvParam.getValue(),
                    oldCvParam.getUnitAccession(),
                    oldCvParam.getUnitName(), unitCVLookupID);
        }
        return newParam;
    }

    private static UserParam transformToUserParam(uk.ac.ebi.jmzidml.model.mzidml.UserParam oldUserParam) {
        UserParam newParam = null;
        if (oldUserParam != null) {
            String unitCVLookupID = null;
            uk.ac.ebi.jmzidml.model.mzidml.Cv cv = oldUserParam.getUnitCv();
            if (cv != null) unitCVLookupID = cv.getId();
            newParam = new UserParam(oldUserParam.getName(),
                    oldUserParam.getType(),
                    oldUserParam.getValue(),
                    oldUserParam.getUnitAccession(),
                    oldUserParam.getUnitName(),
                    unitCVLookupID);
        }
        return newParam;
    }

    public static List<Software> transformToSoftware(List<uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftware> oldSoftwares) {
        List<Software> softwares = null;
        if (oldSoftwares != null) {
            softwares = new ArrayList<Software>();
            for (uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftware oldSoftware : oldSoftwares) {
                softwares.add(transformToSoftware(oldSoftware));
            }
        }
        return softwares;
    }

    public static Software transformToSoftware(uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftware oldSoftware) {


        /** The name of the software sometime is not annotated from writers and exporters
         * in those cases we use the name in CvParams or User Params or Id in cases that no information
         * is provided*/

         if (oldSoftware != null) {
            String name = oldSoftware.getName();
            Contact contact = null;
            if (oldSoftware.getContactRole() != null && oldSoftware.getContactRole().getOrganization() != null) {
                contact = transformToOrganization(oldSoftware.getContactRole().getOrganization());
            } else if (oldSoftware.getContactRole() != null && oldSoftware.getContactRole().getPerson() != null) {
                contact = transformToPerson(oldSoftware.getContactRole().getPerson());
            }
            ParamGroup cvName = null;
            if (oldSoftware.getSoftwareName() != null) {
                cvName = new ParamGroup();
                if (oldSoftware.getSoftwareName().getCvParam() != null) {
                    CvParam cvParam = transformToCvParam(oldSoftware.getSoftwareName().getCvParam());
                    cvName.addCvParam(cvParam);
                    if(name == null) name = getValueFromCvTerm(null, cvParam);
                }
                if (oldSoftware.getSoftwareName().getUserParam() != null) {
                    UserParam userParam = transformToUserParam(oldSoftware.getSoftwareName().getUserParam());
                    cvName.addUserParam(userParam);
                    if(name == null) name = getValueFromCvTerm(name, userParam);
                }
            }
            if(name == null && oldSoftware.getId() != null) name = oldSoftware.getId();

            return new Software(cvName, oldSoftware.getId(), name, contact, oldSoftware.getCustomizations(), oldSoftware.getUri(), oldSoftware.getVersion());
        }
        return null;
    }

    public static List<Reference> transformToReference(Iterator<uk.ac.ebi.jmzidml.model.mzidml.BibliographicReference> it) {
        List<Reference> references = new ArrayList<Reference>();
        while (it.hasNext()) {
            uk.ac.ebi.jmzidml.model.mzidml.BibliographicReference ref = it.next();
            // RefLine Trying to use the same approach of pride converter
            String refLine = ((ref.getAuthors() != null) ? ref.getAuthors() + ". " : "") +
                    ((ref.getYear() != null) ? "(" + ref.getYear().toString() + "). " : "") +
                    ((ref.getTitle() != null) ? ref.getTitle() + " " : "") +
                    ((ref.getPublication() != null) ? ref.getPublication() + " " : "") +
                    ((ref.getVolume() != null) ? ref.getVolume() + "" : "") +
                    ((ref.getIssue() != null) ? "(" + ref.getIssue() + ")" : "") +
                    ((ref.getPages() != null) ? ":" + ref.getPages() + "." : "");
            // create the ref
            //Todo: Set the References ParamGroup for references
            String year = (ref.getYear() == null) ? null : ref.getYear().toString();
            Reference reference = new Reference(ref.getId(), ref.getName(), ref.getDoi(), ref.getTitle(), ref.getPages(), ref.getIssue(), ref.getVolume(), year, ref.getEditor(), ref.getPublisher(), ref.getPublication(), ref.getAuthors(), refLine);
            references.add(reference);
        }
        return references;
    }

    public static Protein transformProteinHypothesisToIdentification(uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionHypothesis oldIdent) {

        DBSequence dbSequence = transformToDBSequence(oldIdent.getDBSequence());

        ParamGroup paramGroup = new ParamGroup(transformToCvParam(oldIdent.getCvParam()), transformToUserParam(oldIdent.getUserParam()));
        Score score = DataAccessUtilities.getScore(paramGroup);
        String name = oldIdent.getName();
        boolean passThreshold = oldIdent.isPassThreshold();

        // get all the spectrum identification items
        List<Peptide> peptides = new ArrayList<Peptide>();
        for (PeptideHypothesis peptideHypothesis : oldIdent.getPeptideHypothesis()) {
            uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence oldPeptideEvidence = peptideHypothesis.getPeptideEvidence();
            for (SpectrumIdentificationItemRef spectrumIdentificationItemRef : peptideHypothesis.getSpectrumIdentificationItemRef()) {
                SpectrumIdentificationItem oldSpectrumIdentificationItem = spectrumIdentificationItemRef.getSpectrumIdentificationItem();
                Peptide peptide = transformToPeptideFromSpectrumItemAndPeptideEvidence(oldSpectrumIdentificationItem, oldPeptideEvidence, peptides.size());
                peptides.add(peptide);
            }
        }

        return new Protein(paramGroup, oldIdent.getId(), name, dbSequence, passThreshold, peptides, score, -1, -1, null);
    }

    public static Protein transformProteinHypothesisToIdentification(uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionHypothesis oldIdent, List<Peptide> peptides) {

        DBSequence dbSequence = transformToDBSequence(oldIdent.getDBSequence());

        ParamGroup paramGroup = new ParamGroup(transformToCvParam(oldIdent.getCvParam()), transformToUserParam(oldIdent.getUserParam()));
        Score score = DataAccessUtilities.getScore(paramGroup);
        String name = oldIdent.getName();
        boolean passThreshold = oldIdent.isPassThreshold();

        return new Protein(paramGroup, oldIdent.getId(), name, dbSequence, passThreshold, peptides, score, -1, -1, null);
    }

    public static Protein transformDBSequenceToIdentification(uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence, List<Peptide> peptides) {

        DBSequence sequence = transformToDBSequence(dbSequence);
        String name = dbSequence.getName();
        return new Protein(null, dbSequence.getId(), name, sequence, false, peptides, null, -1, -1, null);
    }

    public static Peptide transformToPeptideFromSpectrumItemAndPeptideEvidence(SpectrumIdentificationItem oldSpectrumidentification,
                                                                                uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence oldPeptideEvidence, int id) {
        SpectrumIdentification spectrumIdent = transformToPeptideIdentification(oldSpectrumidentification);
        PeptideEvidence peptideEvidence = transformToPeptideEvidence(oldPeptideEvidence);
        List<PeptideEvidence> peptideEvidences = new ArrayList<PeptideEvidence>();
        peptideEvidences.add(peptideEvidence);
        spectrumIdent.setPeptideEvidenceList(peptideEvidences);
        return new Peptide(peptideEvidence, spectrumIdent, id);
    }

    public static Protein transformSpectrumIdentificationItemToIdentification(uk.ac.ebi.jmzidml.model.mzidml.DBSequence oldDbSequence,
                                                                              List<uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem> spectrumIdentificationItemList) {

        DBSequence dbSequence = transformToDBSequence(oldDbSequence);

        List<Peptide> peptides = transformToPeptideIdentificationsFromSpectrumItems(spectrumIdentificationItemList);
        //Todo: threshold and sequence coverage
        return new Protein(null, dbSequence.getId(), null, dbSequence, false, peptides, null, -1, -1, null);
    }

    public static Map<String, IdentifiableParamGroup> transformToFragmentationTable(uk.ac.ebi.jmzidml.model.mzidml.FragmentationTable oldFragmentationTable) {
        Map<String, IdentifiableParamGroup> fragmentationTable = new HashMap<String, IdentifiableParamGroup>();

        if (oldFragmentationTable != null) {
            for (uk.ac.ebi.jmzidml.model.mzidml.Measure oldMeasure : oldFragmentationTable.getMeasure()) {
                fragmentationTable.put(oldMeasure.getId(), new IdentifiableParamGroup(new ParamGroup(transformToCvParam(oldMeasure.getCvParam()), null), oldMeasure.getId(), oldMeasure.getName()));
            }
        }

        return fragmentationTable;
    }

    public static List<Peptide> transformToPeptideIdentificationsFromSpectrumItems(List<uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem> spectrumIdentificationItems) {
        List<Peptide> peptides = new ArrayList<Peptide>();
        if (spectrumIdentificationItems != null && !spectrumIdentificationItems.isEmpty()) {
            for (uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem oldSpectrumidentification : spectrumIdentificationItems) {
                SpectrumIdentification spectrumIdent = transformToPeptideIdentification(oldSpectrumidentification);
                for (uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef evidence : oldSpectrumidentification.getPeptideEvidenceRef()) {
                    PeptideEvidence peptideEvidence = transformToPeptideEvidence(evidence.getPeptideEvidence());
                    peptides.add(new Peptide(peptideEvidence, spectrumIdent));
                }
            }
        }
        return peptides;
    }

    public static SpectrumIdentification transformToPeptideIdentification(uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem oldSpectrumIdentification) {
        SpectrumIdentification peptide = null;
        if (oldSpectrumIdentification != null) {
            String id = oldSpectrumIdentification.getId();
            String name = oldSpectrumIdentification.getName();
            int chargeState = oldSpectrumIdentification.getChargeState();
            double massToCharge = oldSpectrumIdentification.getExperimentalMassToCharge();
            double calcMassToCharge = (oldSpectrumIdentification.getCalculatedMassToCharge() == null)?-1:oldSpectrumIdentification.getCalculatedMassToCharge();
            float pI = (float) 0.0;
            if (oldSpectrumIdentification.getCalculatedPI() != null) {
                pI = oldSpectrumIdentification.getCalculatedPI();
            }
            uk.ac.ebi.jmzidml.model.mzidml.Peptide peptideSeq = oldSpectrumIdentification.getPeptide();
            int rank = oldSpectrumIdentification.getRank();
            boolean passThrehold = oldSpectrumIdentification.isPassThreshold();

            String retentionTime = DataAccessUtilities.getRetentionTime(oldSpectrumIdentification);
            uk.ac.ebi.jmzidml.model.mzidml.MassTable massTable = oldSpectrumIdentification.getMassTable();
            uk.ac.ebi.jmzidml.model.mzidml.Sample sample = oldSpectrumIdentification.getSample();
            List<uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef> peptideEvidence = oldSpectrumIdentification.getPeptideEvidenceRef();
            uk.ac.ebi.jmzidml.model.mzidml.Fragmentation fragmentation = oldSpectrumIdentification.getFragmentation();
            ParamGroup scoreParamGroup = new ParamGroup(transformToCvParam(oldSpectrumIdentification.getCvParam()), transformToUserParam(oldSpectrumIdentification.getUserParam()));
            Score score = DataAccessUtilities.getScore(scoreParamGroup);
            peptide = new SpectrumIdentification(scoreParamGroup, id, name, chargeState, massToCharge, calcMassToCharge, pI,
                    transformToPeptide(peptideSeq), rank, passThrehold, transformToMassTable(massTable),
                    transformToSample(sample), transformToPeptideEvidence(peptideEvidence),
                    transformToFragmentationIon(fragmentation), score, null, null);
            peptide.setRetentionTime(retentionTime);

        }
        return peptide;
    }

    private static List<FragmentIon> transformToFragmentationIon(uk.ac.ebi.jmzidml.model.mzidml.Fragmentation fragmentation) {
        List<FragmentIon> fragmentIons = null;
        if (fragmentation != null) {
            fragmentIons = new ArrayList<FragmentIon>();
            for (uk.ac.ebi.jmzidml.model.mzidml.IonType ionType : fragmentation.getIonType()) {

                // ignore not supported iontypes
                // TODO: Once the MS ontology is adapted for the new fragment ion params, adapt this code
                // TODO DOC Converter only consider - only a-c, x-z ions are reported
                String ionTypeChar = null;
                if (ionType.getCvParam().getName().contains("a ion")) ionTypeChar = "a";
                if (ionType.getCvParam().getName().contains("b ion")) ionTypeChar = "b";
                if (ionType.getCvParam().getName().contains("c ion")) ionTypeChar = "c";
                if (ionType.getCvParam().getName().contains("x ion")) ionTypeChar = "x";
                if (ionType.getCvParam().getName().contains("y ion")) ionTypeChar = "y";
                if (ionType.getCvParam().getName().contains("z ion")) ionTypeChar = "z";

                if (ionTypeChar == null) continue;
                // ignore IonTypes with no index set
                //if (ionType.getIndex() == null)  continue;

                // iterate over the ion type indexes
                List<Integer> listIds = new ArrayList<Integer>();
                listIds.add(ionType.getIndex().get(0));
                for (int i = 1; i < ionType.getIndex().size(); i++) {
                    boolean found = false;
                    int j = 0;
                    while ((!found) && (j < listIds.size())) {
                        if (ionType.getIndex().get(i).equals(listIds.get(j))) {
                            found = true;
                        }
                        j++;
                    }
                    if (!found) {
                        listIds.add(ionType.getIndex().get(i));
                    }
                }

                for (Integer index = 0; index < listIds.size(); index++) {
                    //FragmentIon fragmentIon = new FragmentIon();
                    List<CvParam> cvParams = new ArrayList<CvParam>();
                    // charge
                    CvTermReference cvCharge = CvTermReference.PRODUCT_ION_CHARGE;
                    cvParams.add(new CvParam(cvCharge.getAccession(), cvCharge.getName(), cvCharge.getCvLabel(), String.valueOf(ionType.getCharge()), null, null, null));
                    //ion type
                    cvParams.add(new CvParam(ionType.getCvParam().getAccession(), ionType.getCvParam().getName(), ionType.getCvParam().getCvRef(), listIds.get(index).toString(), null, null, null));
                    //mz
                    for (uk.ac.ebi.jmzidml.model.mzidml.FragmentArray fragArr : ionType.getFragmentArray()) {
                        String measureRef = fragArr.getMeasureRef();
                        //uk.ac.ebi.jmzidml.model.mzidml.Measure oldMeasure = fragArr.getMeasure();
                        IdentifiableParamGroup oldMeasure = fragmentationTable.get(measureRef);
                        CvParam cvParam;
                        CvTermReference cvMz;
                        cvMz = CvTermReference.PRODUCT_ION_MZ;
                        cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                        if (cvParam == null) {
                            cvMz = CvTermReference.MS_PRODUCT_ION_MZ;
                            cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                            if (cvParam == null) {
                                cvMz = CvTermReference.PRODUCT_ION_INTENSITY;
                                cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                                if (cvParam == null) {
                                    cvMz = CvTermReference.MS_PRODUCT_ION_INTENSITY;
                                    cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                                    if (cvParam == null) {
                                        cvMz = CvTermReference.PRODUCT_ION_MASS_ERROR;
                                        cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                                        if (cvParam == null) {
                                            cvMz = CvTermReference.MS_PRODUCT_ION_MASS_ERROR;
                                            cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                                            if (cvParam == null) {
                                                cvMz = CvTermReference.PRODUCT_ION_RETENTION_TIME_ERROR;
                                                cvParam = getCvParamByID(oldMeasure.getCvParams(), cvMz.getAccession(), fragArr.getValues().get(index).toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (cvParam != null) {
                            cvParams.add(cvParam);
                        }
                    }
                    fragmentIons.add(new FragmentIon(new ParamGroup(cvParams, null)));
                }
            }
        }
        return fragmentIons;
    }

    private static CvParam getCvParamByID(List<CvParam> oldCvParams, String accession, String newValue) {
        for (CvParam oldCvParam : oldCvParams) {
            if (oldCvParam.getAccession().equalsIgnoreCase(accession)) {
                oldCvParam.setValue(newValue);
                return oldCvParam;
            }
        }
        return null;
    }

    private static List<MassTable> transformToMassTable(List<uk.ac.ebi.jmzidml.model.mzidml.MassTable> oldMassTables) {
        List<MassTable> massTables = null;
        if (oldMassTables != null) {
            massTables = new ArrayList<MassTable>();
            for (uk.ac.ebi.jmzidml.model.mzidml.MassTable oldMassTable : oldMassTables) {
                massTables.add(transformToMassTable(oldMassTable));
            }
        }
        return massTables;
    }

    private static MassTable transformToMassTable(uk.ac.ebi.jmzidml.model.mzidml.MassTable oldMassTable) {
        MassTable massTable = null;
        if (oldMassTable != null) {
            Map<String, Float> residues = new HashMap<String, Float>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Residue residue : oldMassTable.getResidue()) {
                residues.put(residue.getCode(), residue.getMass());
            }
            Map<String, ParamGroup> ambiguousResidues = new HashMap<String, ParamGroup>();
            for (uk.ac.ebi.jmzidml.model.mzidml.AmbiguousResidue residue : oldMassTable.getAmbiguousResidue()) {
                ambiguousResidues.put(residue.getCode(), new ParamGroup(transformToCvParam(residue.getCvParam()), transformToUserParam(residue.getUserParam())));
            }
            massTable = new MassTable(oldMassTable.getMsLevel(), residues, ambiguousResidues);
        }
        return massTable;
    }

    private static PeptideEvidence transformToPeptideEvidence(uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence oldPeptideEvidence) {
        PeptideEvidence evidence = null;
        if (oldPeptideEvidence != null) {
            int start = (oldPeptideEvidence.getStart() != null) ? oldPeptideEvidence.getStart() : -1;
            int end = (oldPeptideEvidence.getEnd() != null) ? oldPeptideEvidence.getEnd() : -1;
            //System.out.println("Peptide Evidence: " + oldPeptideEvidence.getId());
            evidence = new PeptideEvidence(oldPeptideEvidence.getId(), oldPeptideEvidence.getName(), start, end, oldPeptideEvidence.isIsDecoy(), transformToPeptide(oldPeptideEvidence.getPeptide()), transformToDBSequence(oldPeptideEvidence.getDBSequence()));
            ArrayList<UserParam> userParams = new ArrayList<>();
            ArrayList<CvParam> cvParams= new ArrayList<>();
            for (uk.ac.ebi.jmzidml.model.mzidml.UserParam userParam : oldPeptideEvidence.getUserParam()) {
                userParams.add( new UserParam(userParam.getName(),null,userParam.getValue(),null,null,null));
            }
            for (uk.ac.ebi.jmzidml.model.mzidml.CvParam cvParam : oldPeptideEvidence.getCvParam()) {
                cvParams.add( new CvParam(cvParam.getAccession(), cvParam.getName(), cvParam.getCvRef(), cvParam.getValue(), cvParam.getUnitAccession(), cvParam.getUnitName(), cvParam.getUnitCvRef()));
            }
            evidence.setUserParams(userParams);
            evidence.setCvParams(cvParams);

        }
        return evidence;
    }

    private static List<PeptideEvidence> transformToPeptideEvidence(List<uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef> oldPeptideEvidenceRefs) {
        List<PeptideEvidence> peptideEvidences = null;
        if (oldPeptideEvidenceRefs != null) {
            peptideEvidences = new ArrayList<PeptideEvidence>();
            for (uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef oldPeptideEvidenceRef : oldPeptideEvidenceRefs) {
                peptideEvidences.add(transformToPeptideEvidence(oldPeptideEvidenceRef.getPeptideEvidence()));
            }
        }
        return peptideEvidences;
    }

    private static PeptideSequence transformToPeptide(uk.ac.ebi.jmzidml.model.mzidml.Peptide oldPeptide) {
        PeptideSequence peptideSequence = null;
        if (oldPeptide != null) {
            peptideSequence = new PeptideSequence(oldPeptide.getId(), oldPeptide.getName(), oldPeptide.getPeptideSequence(), transformToModification(oldPeptide.getModification()), transformToSubstitutionMod(oldPeptide.getSubstitutionModification()));
        }
        return peptideSequence;
    }

    private static List<Modification> transformToModification(List<uk.ac.ebi.jmzidml.model.mzidml.Modification> oldModifications) {
        List<Modification> modifications = null;
        if (oldModifications != null) {
            modifications = new ArrayList<Modification>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Modification oldModification : oldModifications) {
                modifications.add(transformToModification(oldModification));
            }
        }
        return modifications;
    }

    private static Modification transformToModification(uk.ac.ebi.jmzidml.model.mzidml.Modification oldModification) {
        Modification modification = null;

        if (oldModification != null) {
            List<Double> monoMasses = null;
            List<Double> avgMasses = null;
            if (oldModification.getMonoisotopicMassDelta() != null) {
                monoMasses = new ArrayList<Double>();
                monoMasses.add(oldModification.getMonoisotopicMassDelta());
            }
            if (oldModification.getAvgMassDelta() != null) {
                avgMasses = new ArrayList<Double>();
                avgMasses.add(oldModification.getAvgMassDelta());
            }
            List<CvParam> cvParams = transformToCvParam(oldModification.getCvParam());
            String id = null;
            String name = null;
            String dataBaseName = null;
            //Todo: Try to make this function more flexible, we can define default Mod Databases
            for (CvParam cvParam : cvParams) {

                if (cvParam.getCvLookupID() == null) {
                    id = "Unknown";
                    name = cvParam.getName();
                    dataBaseName = "Unknown";
                } else if (cvParam.getCvLookupID().compareToIgnoreCase("MOD") == 0) {
                    id = cvParam.getAccession();
                    name = cvParam.getName();
                    dataBaseName = (cvParam.getCvLookupID() == null) ? "MOD" : cvParam.getCvLookupID();
                    break;
                } else if (cvParam.getCvLookupID().compareToIgnoreCase("UNIMOD") == 0) {
                    id = cvParam.getAccession();
                    name = cvParam.getName();
                    dataBaseName = (cvParam.getCvLookupID() == null) ? "UNIMOD" : cvParam.getCvLookupID();
                } else {
                    id = cvParam.getAccession();
                    name = cvParam.getName();
                    dataBaseName = cvParam.getCvLookupID();
                }
            }
            ParamGroup param = new ParamGroup(cvParams, null);
            modification = new Modification(param, id, name, oldModification.getLocation(), oldModification.getResidues(), avgMasses, monoMasses, dataBaseName, null);
        }
        return modification;
    }

    private static List<SubstitutionModification> transformToSubstitutionMod(List<uk.ac.ebi.jmzidml.model.mzidml.SubstitutionModification> oldSubstitutionModifications) {
        List<SubstitutionModification> modifications = null;
        if (oldSubstitutionModifications != null) {
            modifications = new ArrayList<SubstitutionModification>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SubstitutionModification oldModification : oldSubstitutionModifications) {
                modifications.add(transformToSubstitutionMod(oldModification));
            }
        }
        return modifications;
    }

    private static SubstitutionModification transformToSubstitutionMod(uk.ac.ebi.jmzidml.model.mzidml.SubstitutionModification oldModification) {
        SubstitutionModification modification = null;
        if (oldModification != null) {
            double avgMass = (oldModification.getAvgMassDelta() != null) ? oldModification.getAvgMassDelta() : -1.0;
            double monoMass = (oldModification.getMonoisotopicMassDelta() != null) ? oldModification.getMonoisotopicMassDelta() : -1.0;
            int location = (oldModification.getLocation() != null) ? oldModification.getLocation() : -1;
            modification = new SubstitutionModification(oldModification.getOriginalResidue(), oldModification.getReplacementResidue(), location, avgMass, monoMass);
        }
        return modification;
    }

    private static DBSequence transformToDBSequence(uk.ac.ebi.jmzidml.model.mzidml.DBSequence oldDbSequence) {
        DBSequence dbSequence = null;
        if (oldDbSequence != null) {
            String id = oldDbSequence.getId();
            String name = oldDbSequence.getName();
            int length = (oldDbSequence.getLength() != null) ? oldDbSequence.getLength() : -1;
            String accession = oldDbSequence.getAccession();
            ParamGroup params = new ParamGroup(transformToCvParam(oldDbSequence.getCvParam()), transformToUserParam(oldDbSequence.getUserParam()));
            dbSequence = new DBSequence(params, id, name, length, accession, transformToSeachDatabase(oldDbSequence.getSearchDatabase()), oldDbSequence.getSeq(), null, null);
        }
        return dbSequence;
    }

    private static SearchDataBase transformToSeachDatabase(uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase oldDatabase) {

        String name = (oldDatabase != null)? oldDatabase.getName():null;
        /** Software writers sometimes don't annotate the software name and
         * we construct this value using the CVparams or Userparams. In case
         * that no CvParams or Userparms is present we use the Id information*/

        CvParam fileFormat = ((oldDatabase != null ? oldDatabase.getFileFormat() : null) == null) ? null : transformToCvParam(oldDatabase.getFileFormat().getCvParam());
        String releaseDate = (oldDatabase.getReleaseDate() == null) ? null : oldDatabase.getReleaseDate().toString();
        int dataBaseSeq = (oldDatabase.getNumDatabaseSequences() == null) ? -1 : oldDatabase.getNumDatabaseSequences().intValue();
        int dataBaseRes = (oldDatabase.getNumResidues() == null) ? -1 : oldDatabase.getNumResidues().intValue();
        ParamGroup nameOfDatabase = null;
        if (oldDatabase.getDatabaseName() != null) {
            nameOfDatabase = new ParamGroup(transformToCvParam(oldDatabase.getDatabaseName().getCvParam()), transformToUserParam(oldDatabase.getDatabaseName().getUserParam()));
        }
        if(name == null){
            if(!(nameOfDatabase != null && nameOfDatabase.getCvParams().isEmpty())){
                name = getValueFromCvTerm(null, nameOfDatabase.getCvParams().get(0));
            }if(name == null && !nameOfDatabase.getUserParams().isEmpty()){
                name = getValueFromCvTerm(null, nameOfDatabase.getUserParams().get(0));
            }if(name == null){
                name = oldDatabase.getId();
            }
        }
        return new SearchDataBase(oldDatabase.getId(),
                name, oldDatabase.getLocation(), fileFormat, oldDatabase.getExternalFormatDocumentation(), oldDatabase.getVersion(), releaseDate, dataBaseSeq, dataBaseRes, nameOfDatabase, transformToCvParam(oldDatabase.getCvParam()));
    }

    public static List<CVLookup> transformCVList(List<uk.ac.ebi.jmzidml.model.mzidml.Cv> cvList) {
        List<CVLookup> cvLookups = null;
        if (cvList != null) {
            cvLookups = new ArrayList<CVLookup>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Cv cv : cvList) {
                cvLookups.add(transformToCVLookup(cv));
            }
        }
        return cvLookups;
    }

    public static CVLookup transformToCVLookup(uk.ac.ebi.jmzidml.model.mzidml.Cv oldCv) {
        CVLookup cvLookup = null;
        if (oldCv != null) {
            cvLookup = new CVLookup(oldCv.getId(), oldCv.getFullName(),
                    oldCv.getVersion(), oldCv.getUri());
        }
        return cvLookup;
    }

    public static Provider transformToProvider(uk.ac.ebi.jmzidml.model.mzidml.Provider oldProvider) {
        Provider provider = null;
        if (oldProvider != null) {
            Contact contact = null;
            CvParam role = null;
            if(oldProvider.getContactRole() != null) {
                if (oldProvider.getContactRole().getOrganization() != null) {
                    contact = transformToOrganization(oldProvider.getContactRole().getOrganization());
                } else if (oldProvider.getContactRole().getPerson() != null) {
                    contact = transformToPerson(oldProvider.getContactRole().getPerson());
                }
                role = transformToCvParam(oldProvider.getContactRole().getRole().getCvParam());
            }
            Software software = transformToSoftware(oldProvider.getSoftware());
            provider = new Provider(oldProvider.getId(), oldProvider.getName(), software, contact, role);
        }
        return provider;
    }

    public static List<SpectrumIdentificationProtocol> transformToSpectrumIdentificationProtocol(List<uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol> oldSpecProtocol) {
        List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList = null;
        if (oldSpecProtocol != null) {
            spectrumIdentificationProtocolList = new ArrayList<SpectrumIdentificationProtocol>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol oldProtocol : oldSpecProtocol) {
                spectrumIdentificationProtocolList.add(transformToSpectrumIdentificationProtocol(oldProtocol));
            }
        }
        return spectrumIdentificationProtocolList;

    }

    public static SpectrumIdentificationProtocol transformToSpectrumIdentificationProtocol(uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol oldProtocol) {
        SpectrumIdentificationProtocol spectrumIdentificationProtocol = null;
        if (oldProtocol != null) {
            Software analysisSoftware = transformToSoftware(oldProtocol.getAnalysisSoftware());
            ParamGroup threshold = new ParamGroup(transformToCvParam(oldProtocol.getThreshold().getCvParam()), transformToUserParam(oldProtocol.getThreshold().getUserParam()));
            ParamGroup searchType = new ParamGroup(transformToCvParam(oldProtocol.getSearchType().getCvParam()), transformToUserParam(oldProtocol.getSearchType().getUserParam()));
            boolean enzymeIndependent = (oldProtocol.getEnzymes() == null || oldProtocol.getEnzymes().isIndependent() == null) ? false : oldProtocol.getEnzymes().isIndependent();
            List<uk.ac.ebi.jmzidml.model.mzidml.Enzyme> enzymes = (oldProtocol.getEnzymes() != null) ? oldProtocol.getEnzymes().getEnzyme() : null;
            List<Enzyme> enzymeList = transformToEnzyme(enzymes);
            List<CvParam> fragmentTolerance = (oldProtocol.getFragmentTolerance() != null) ? transformToCvParam(oldProtocol.getFragmentTolerance().getCvParam()) : null;
            List<CvParam> parentTolerance = (oldProtocol.getParentTolerance() != null) ? transformToCvParam(oldProtocol.getParentTolerance().getCvParam()) : null;
            List<Filter> filterList = (oldProtocol.getDatabaseFilters() != null) ? transformToFilter(oldProtocol.getDatabaseFilters().getFilter()) : null;
            DataBaseTranslation dataBaseTranslation = transformToDataBaseTranslation(oldProtocol.getDatabaseTranslation());
            List<uk.ac.ebi.jmzidml.model.mzidml.SearchModification> modifications = (oldProtocol.getModificationParams() != null) ? oldProtocol.getModificationParams().getSearchModification() : null;
            List<SearchModification> searchModificationList = transformToSearchModification(modifications);
            List<MassTable> massTableList = transformToMassTable(oldProtocol.getMassTable());
            List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> oldCvParam = (oldProtocol.getAdditionalSearchParams() != null) ? oldProtocol.getAdditionalSearchParams().getCvParam() : null;
            List<uk.ac.ebi.jmzidml.model.mzidml.UserParam> oldUserParam = (oldProtocol.getAdditionalSearchParams() != null) ? oldProtocol.getAdditionalSearchParams().getUserParam() : null;
            spectrumIdentificationProtocol = new SpectrumIdentificationProtocol(new ParamGroup(transformToCvParam(oldCvParam), transformToUserParam(oldUserParam)), oldProtocol.getId(), oldProtocol.getName(), analysisSoftware, threshold, searchType, searchModificationList, enzymeIndependent, enzymeList, massTableList, fragmentTolerance, parentTolerance, filterList, dataBaseTranslation);
        }
        return spectrumIdentificationProtocol;
    }

    private static SearchModification transformToSearchModification(uk.ac.ebi.jmzidml.model.mzidml.SearchModification oldModification) {
        SearchModification searchModification = null;
        if (oldModification != null) {
            List<CvParam> rules = null;
            if (oldModification.getSpecificityRules() != null) {
                List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> cvParamRules = new ArrayList<uk.ac.ebi.jmzidml.model.mzidml.CvParam>();
                for (uk.ac.ebi.jmzidml.model.mzidml.SpecificityRules paramRules : oldModification.getSpecificityRules()) {
                    cvParamRules.addAll(paramRules.getCvParam());
                }
                rules = transformToCvParam(cvParamRules);
            }

            searchModification = new SearchModification(oldModification.isFixedMod(), oldModification.getMassDelta(), oldModification.getResidues(), rules, transformToCvParam(oldModification.getCvParam()));
        }
        return searchModification;
    }

    private static List<SearchModification> transformToSearchModification(List<uk.ac.ebi.jmzidml.model.mzidml.SearchModification> oldSearchModifications) {
        List<SearchModification> searchModifications = null;
        if (oldSearchModifications != null) {
            searchModifications = new ArrayList<SearchModification>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SearchModification oldSearchModification : oldSearchModifications) {
                searchModifications.add(transformToSearchModification(oldSearchModification));
            }
        }
        return searchModifications;
    }

    private static DataBaseTranslation transformToDataBaseTranslation(uk.ac.ebi.jmzidml.model.mzidml.DatabaseTranslation oldDatabaseTranslation) {
        DataBaseTranslation dataBaseTranslation = null;
        if (oldDatabaseTranslation != null) {
            List<IdentifiableParamGroup> translationTable = new ArrayList<IdentifiableParamGroup>();
            for (uk.ac.ebi.jmzidml.model.mzidml.TranslationTable oldTranslationTable : oldDatabaseTranslation.getTranslationTable()) {
                translationTable.add(new IdentifiableParamGroup(new ParamGroup(transformToCvParam(oldTranslationTable.getCvParam()), null), oldTranslationTable.getId(), oldTranslationTable.getName()));
            }
            dataBaseTranslation = new DataBaseTranslation(oldDatabaseTranslation.getFrames(), translationTable);
        }
        return dataBaseTranslation;
    }

    private static List<Filter> transformToFilter(List<uk.ac.ebi.jmzidml.model.mzidml.Filter> oldFilters) {
        List<Filter> filters = null;
        if (oldFilters != null) {
            filters = new ArrayList<Filter>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Filter oldFilter : oldFilters) {
                ParamGroup filterType = null;
                if (oldFilter.getFilterType() != null) {
                    filterType = new ParamGroup(transformToCvParam(oldFilter.getFilterType().getCvParam()),
                            transformToUserParam(oldFilter.getFilterType().getUserParam()));
                }
                ParamGroup include = null;
                if (oldFilter.getInclude() != null) {
                    include = new ParamGroup(transformToCvParam(oldFilter.getInclude().getCvParam()),
                            transformToUserParam(oldFilter.getInclude().getUserParam()));
                }
                ParamGroup exclude = null;
                if (oldFilter.getExclude() != null) {
                    exclude = new ParamGroup(transformToCvParam(oldFilter.getExclude().getCvParam()),
                            transformToUserParam(oldFilter.getExclude().getUserParam()));
                }
                filters.add(new Filter(filterType, include, exclude));
            }
        }
        return filters;
    }

    private static List<Enzyme> transformToEnzyme(List<uk.ac.ebi.jmzidml.model.mzidml.Enzyme> oldEnzymes) {
        List<Enzyme> enzymes = null;
        if (oldEnzymes != null) {
            enzymes = new ArrayList<Enzyme>();
            for (uk.ac.ebi.jmzidml.model.mzidml.Enzyme oldEnzyme : oldEnzymes) {
                enzymes.add(transformToEnzyme(oldEnzyme));
            }
        }
        return enzymes;
    }

    private static Enzyme transformToEnzyme(uk.ac.ebi.jmzidml.model.mzidml.Enzyme oldEnzyme) {
        Enzyme newEnzyme = null;
        if (oldEnzyme != null) {
            boolean specific = (oldEnzyme.isSemiSpecific() == null) ? false : oldEnzyme.isSemiSpecific();
            int misscleavage = (oldEnzyme.getMissedCleavages() == null) ? 0 : oldEnzyme.getMissedCleavages();
            int mindistance = (oldEnzyme.getMinDistance() == null) ? -1 : oldEnzyme.getMinDistance();
            List<CvParam> cvParams = (oldEnzyme.getEnzymeName() != null) ? transformToCvParam(oldEnzyme.getEnzymeName().getCvParam()) : null;
            List<UserParam> userParams = (oldEnzyme.getEnzymeName() != null) ? transformToUserParam(oldEnzyme.getEnzymeName().getUserParam()) : null;
            newEnzyme = new Enzyme(oldEnzyme.getId(),
                    oldEnzyme.getName(),
                    specific,
                    misscleavage,
                    mindistance,
                    new ParamGroup(cvParams, userParams),
                    oldEnzyme.getSiteRegexp());
        }
        return newEnzyme;
    }

    public static Protocol transformToProteinDetectionProtocol(uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionProtocol oldProteinDetectionProtocol) {
        Protocol proteinDetectionProtocol = null;
        if (oldProteinDetectionProtocol != null) {
            ParamGroup analysisParam = (oldProteinDetectionProtocol.getAnalysisParams() != null) ? new ParamGroup(transformToCvParam(oldProteinDetectionProtocol.getAnalysisParams().getCvParam()), transformToUserParam(oldProteinDetectionProtocol.getAnalysisParams().getUserParam())) : new ParamGroup();
            proteinDetectionProtocol = new Protocol(analysisParam,
                    oldProteinDetectionProtocol.getId(),
                    oldProteinDetectionProtocol.getName(),
                    transformToSoftware(oldProteinDetectionProtocol.getAnalysisSoftware()),
                    new ParamGroup(transformToCvParam(oldProteinDetectionProtocol.getThreshold().getCvParam()), transformToUserParam(oldProteinDetectionProtocol.getThreshold().getUserParam())));

        }
        return proteinDetectionProtocol;
    }

    public static List<SearchDataBase> transformToSearchDataBase(List<uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase> oldSearchDatabases) {
        List<SearchDataBase> searchDataBases = null;
        if (oldSearchDatabases != null) {
            searchDataBases = new ArrayList<SearchDataBase>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase oldSearchDatabase : oldSearchDatabases) {
                searchDataBases.add(transformToSeachDatabase(oldSearchDatabase));
            }
        }
        return searchDataBases;
    }

    public static SpectraData transformToSpectraData(uk.ac.ebi.jmzidml.model.mzidml.SpectraData oldSpectraData, boolean mgfTitle) {
        SpectraData spectraData = null;
        if (oldSpectraData != null) {
            if(!mgfTitle){
                CvParam fileFormat = (oldSpectraData.getFileFormat() == null) ? null : transformToCvParam(oldSpectraData.getFileFormat().getCvParam());
                CvParam spectrumId = (oldSpectraData.getSpectrumIDFormat().getCvParam() == null) ? null : transformToCvParam(oldSpectraData.getSpectrumIDFormat().getCvParam());
                spectraData = new SpectraData(oldSpectraData.getId(), oldSpectraData.getName(), oldSpectraData.getLocation(), fileFormat, oldSpectraData.getExternalFormatDocumentation(), spectrumId);
            }else{
                CvParam fileFormat = (oldSpectraData.getFileFormat() == null) ? null : MzIdentMLUtils.getFileFormatMGFTitle();
                CvParam spectrumId = (oldSpectraData.getSpectrumIDFormat().getCvParam() == null) ? null : MzIdentMLUtils.getSpectrumIdFormatMGFTitle();
                String location = (oldSpectraData.getLocation() != null)? oldSpectraData.getLocation().replaceAll("(?i)" + Constants.WIFF_EXT, Constants.MGF_EXT): null;
                String name     = (oldSpectraData.getName() != null)? oldSpectraData.getName().replaceAll("(?i)" + Constants.WIFF_EXT, Constants.MGF_EXT): null;
                spectraData = new SpectraData(oldSpectraData.getId(), name, location, fileFormat, oldSpectraData.getExternalFormatDocumentation(), spectrumId);
            }


        }
        return spectraData;
    }

    public static List<SpectraData> transformToSpectraData(List<uk.ac.ebi.jmzidml.model.mzidml.SpectraData> oldSpectraDatas, List<Comparable> usedTitle) {
        List<SpectraData> spectraDatas = null;
        if (oldSpectraDatas != null) {
            spectraDatas = new ArrayList<SpectraData>();
            for (uk.ac.ebi.jmzidml.model.mzidml.SpectraData oldSpectraData : oldSpectraDatas) {
                spectraDatas.add(transformToSpectraData(oldSpectraData, usedTitle.contains(oldSpectraData.getId())));
            }
        }
        return spectraDatas;
    }

    public static CvParam transformDateToCvParam(Date creationDate) {
        CvTermReference cvTerm = CvTermReference.EXPERIMENT_GLOBAL_CREATIONDATE;
        return new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), creationDate.toString(), null, null, null);
    }

    public static ProteinGroup transformProteinAmbiguityGroupToProteinGroup(ProteinAmbiguityGroup proteinAmbiguityGroup) {

        ParamGroup paramGroup = new ParamGroup(transformToCvParam(proteinAmbiguityGroup.getCvParam()), transformToUserParam(proteinAmbiguityGroup.getUserParam()));

        List<ProteinDetectionHypothesis> proteinDetectionHypothesis = proteinAmbiguityGroup.getProteinDetectionHypothesis();
        List<Protein> proteins = new ArrayList<Protein>();
        for (ProteinDetectionHypothesis proteinDetectionHypothesi : proteinDetectionHypothesis) {
            Protein protein = transformProteinHypothesisToIdentification(proteinDetectionHypothesi);
            proteins.add(protein);
        }

        return new ProteinGroup(paramGroup, proteinAmbiguityGroup.getId(), proteinAmbiguityGroup.getName(), proteins);
    }

    public static ProteinGroup transformProteinAmbiguityGroupToProteinGroup(ProteinAmbiguityGroup proteinAmbiguityGroup, List<Protein> proteins) {

        ParamGroup paramGroup = new ParamGroup(transformToCvParam(proteinAmbiguityGroup.getCvParam()), transformToUserParam(proteinAmbiguityGroup.getUserParam()));

        return new ProteinGroup(paramGroup, proteinAmbiguityGroup.getId(), proteinAmbiguityGroup.getName(), proteins);
    }

    /**
     * To get the information of a cvterm or user param and put in an String we normally take firstly
     * the value of the Parameter and if is not provided we take the name. This function is important when
     * information like the name of the object is not provide and the writers use only CvTerms.
     * @param originalValue The Original String value of the CvTerm
     * @param cvTerm The CVTerm
     * @return An String with the Value
     */
    private static String getValueFromCvTerm(String originalValue, Parameter cvTerm){

       if(cvTerm.getValue() != null && cvTerm.getValue().length() > 0){
           originalValue = cvTerm.getValue();
       }else if(cvTerm.getName() != null && cvTerm.getName().length() > 0){
           originalValue = cvTerm.getName();
       }
       return originalValue;
    }
}

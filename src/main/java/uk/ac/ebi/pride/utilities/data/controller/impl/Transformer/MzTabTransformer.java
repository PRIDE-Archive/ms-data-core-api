package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;


import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.model.Contact;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.Assay;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.StudyVariable;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.CvUtilities;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.NumberUtilities;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MzTab Transformer to ms-data-core-api Objects. We are only converting proteins and psm without the peptide information in the first release. In the near future we will
 * also convert peptide information.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class MzTabTransformer {

    private final static String PROTOCOL_ID = "protocol1";

    public static List<SourceFile> transformSourceFiles(Map<Integer, uk.ac.ebi.pride.jmztab.model.MsRun> msRunMap) {
        List<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        if (msRunMap != null && msRunMap.size() > 0) {
            for (Map.Entry entry : msRunMap.entrySet())
                sourceFiles.add(transformSourceFile((MsRun) entry.getValue()));
        }
        return sourceFiles;
    }

    /**
     * Convert an msRun to SourceFile
     *
     * @param msRun msRun
     * @return SourceFile in in ms-data-core api
     */
    public static SourceFile transformSourceFile(MsRun msRun) {
        SourceFile sourceFile = null;
        if (msRun != null) {
            ParamGroup paramGroup = new ParamGroup();
            //Adding fragmetation type as a CVParameter
            if (msRun.getFragmentationMethod() != null) {
                CvParam fragmentationType = MzTabUtils.convertParamToCvParam(msRun.getFragmentationMethod());
                paramGroup.addCvParam(fragmentationType);
            }
            //Adding hash method to CvParam
            if (msRun.getHashMethod() != null) {
                CvParam hashType = MzTabUtils.convertParamToCvParam(msRun.getHashMethod());
                hashType.setValue(msRun.getHash());
                paramGroup.addCvParam(hashType);
            }
            // Add CvFormat
            CvParam format = null;
            if (msRun.getFormat() != null)
                format = MzTabUtils.convertParamToCvParam(msRun.getFormat());

            sourceFile = new SourceFile(paramGroup, msRun.getId().toString(), msRun.getReference(), msRun.getLocation().getPath(), format, null);

        }
        return sourceFile;
    }

    public static Collection<Person> transformContactToPersons(Map<Integer, uk.ac.ebi.pride.jmztab.model.Contact> contacts) {
        List<Person> persons = new ArrayList<Person>();
        if (contacts != null & (contacts != null ? contacts.size() : 0) > 0) {
            for (Map.Entry contact : contacts.entrySet()) {
                persons.add(transformContactToPerson((uk.ac.ebi.pride.jmztab.model.Contact) contact.getValue()));
            }
        }
        return persons;
    }

    public static Person transformContactToPerson(Contact contact) {
        Person person = null;
        if (contact != null) {
            CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
            List<CvParam> cvParams = DataAccessUtilities.getCvParam(contactTerm.getName(), contactTerm.getCvLabel(), contactTerm.getAccession(), contact.getName());
            CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
            cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), contact.getAffiliation(), null, null, null));
            CvTermReference contactMail = CvTermReference.CONTACT_EMAIL;
            cvParams.add(new CvParam(contactMail.getAccession(), contactMail.getName(), contactMail.getCvLabel(), contact.getEmail(), null, null, null));
            List<UserParam> userParams = null;
            List<Organization> affiliation = new ArrayList<Organization>();
            affiliation.add(new Organization(null, contact.getAffiliation(), null, null));
            ParamGroup paramGroup = new ParamGroup(cvParams, userParams);
            person = new Person(paramGroup, contact.getName(), contact.getName(), null, null, null, affiliation, contact.getEmail());
        }
        return person;
    }

    public static List<Organization> transformContactToOrganization(Map<Integer, uk.ac.ebi.pride.jmztab.model.Contact> contacts) {
        List<Organization> organizations = new ArrayList<Organization>();
        if (contacts != null & (contacts != null ? contacts.size() : 0) > 0) {
            for (Map.Entry contact : contacts.entrySet()) {
                organizations.add(transformContactToOrganization((uk.ac.ebi.pride.jmztab.model.Contact) contact.getValue()));
            }
        }
        return organizations;
    }

    public static Organization transformContactToOrganization(Contact contact) {
        Organization organization = null;
        if (contact != null) {
            CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
            List<CvParam> cvParams = new ArrayList<CvParam>();
            cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), contact.getAffiliation(), null, null, null));
            List<UserParam> userParams = null;
            ParamGroup paramGroup = new ParamGroup(cvParams, userParams);
            organization = new Organization(paramGroup, null, contact.getAffiliation(), null, null);
        }
        return organization;
    }

    public static List<Sample> transformSamples(Map<Integer, uk.ac.ebi.pride.jmztab.model.Sample> oldSamples, Metadata metadata, boolean hasQuantitation) {
        List<Sample> samples = new ArrayList<Sample>();
        if (oldSamples != null && oldSamples.size() > 0) {
            for (uk.ac.ebi.pride.jmztab.model.Sample oldSample : oldSamples.values()) {
                samples.add(transformSample(oldSample));
            }
        }
        return samples;
    }

    private static Sample transformSample(uk.ac.ebi.pride.jmztab.model.Sample oldSample) {
        if (oldSample != null) {
            ParamGroup paramGroup = new ParamGroup();
            if (oldSample.getCellTypeList() != null && oldSample.getCellTypeList().size() > 0) {
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getCellTypeList()));
            }
            if (oldSample.getDiseaseList() != null && oldSample.getDiseaseList().size() > 0) {
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getDiseaseList()));
            }
            if (oldSample.getSpeciesList() != null && oldSample.getSpeciesList().size() > 0) {
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getSpeciesList()));
            }
            if (oldSample.getTissueList() != null && oldSample.getTissueList().size() > 0) {
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getTissueList()));
            }

            if (oldSample.getCustomList() != null && oldSample.getCustomList().size() > 0) {
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getCustomList()));
            }
            return new Sample(paramGroup, oldSample.getId().toString(), oldSample.getDescription());
        }
        return null;
    }

    /**
     * Transform a List of softwares
     *
     * @param dataSoftwares mzTab softwares
     * @return List of Softwares
     */
    public static List<Software> transformSoftwares(Map<Integer, uk.ac.ebi.pride.jmztab.model.Software> dataSoftwares) {
        List<Software> softwares = new ArrayList<Software>();
        if (dataSoftwares != null && dataSoftwares.size() > 0) {
            for (Map.Entry entry : dataSoftwares.entrySet()) {
                softwares.add(transformSoftware((uk.ac.ebi.pride.jmztab.model.Software) entry.getValue()));
            }
        }
        return softwares;
    }

    /**
     * Transform a Software Object
     *
     * @param oldSoftware The mzTab Software
     * @return Software
     */
    public static Software transformSoftware(uk.ac.ebi.pride.jmztab.model.Software oldSoftware) {
        if (oldSoftware != null) {
            ParamGroup paramGroup = new ParamGroup();
            String version = null;
            String name = null;
            if (oldSoftware.getParam() != null) {
                paramGroup.addCvParam(MzTabUtils.convertParamToCvParam(oldSoftware.getParam()));
                version = oldSoftware.getParam().getValue();
                name = oldSoftware.getParam().getName();
            }
            if (oldSoftware.getSettingList() != null && oldSoftware.getSettingList().size() > 0) {
                paramGroup.addUserParams(MzTabUtils.convertStringListToUserParam(oldSoftware.getSettingList()));

            }
            return new Software(paramGroup, oldSoftware.getId().toString(), name, null, null, null, version);
        }
        return null;
    }

    public static Collection<Reference> transformReferences(Map<Integer, uk.ac.ebi.pride.jmztab.model.Publication> publications) {
        Collection<Reference> references = new ArrayList<Reference>();
        if (publications != null && publications.size() > 0) {
            for (Map.Entry entry : publications.entrySet()) {
                Publication publication = (Publication) entry.getValue();
                Reference newReference = null;
                if (publication != null) {
                    ParamGroup paramGroup = new ParamGroup();
                    CvTermReference term = CvTermReference.MS_PUBLICATION_DOI;
                    paramGroup.addCvParam(new CvParam(term.getAccession(), term.getName(), term.getCvLabel(), publication.toString(), null, null, null));
                    newReference = new Reference(paramGroup, publication.toString());
                }
                references.add(newReference);
            }
        }
        return references;
    }

    public static ParamGroup transformAdditional(List<Param> additionalParams) {
        if (additionalParams != null && additionalParams.size() > 0) {
            ParamGroup paramGroup = new ParamGroup();
            for (Param param : additionalParams)
                paramGroup.addCvParam(MzTabUtils.convertParamToCvParam(param));
            return paramGroup;
        }
        return null;
    }

    /**
     * Convert gel free protein identification
     *
     * @param rawIdent mzTab protein identification
     * @return GelFreeIdentification   gel free identification
     */

    public static Protein transformIdentification(uk.ac.ebi.pride.jmztab.model.Protein rawIdent,
                                                  Integer rawIndex,
                                                  Map<String, uk.ac.ebi.pride.jmztab.model.PSM> rawPsms,
                                                  Map<String, uk.ac.ebi.pride.jmztab.model.Peptide> rawPeptides,
                                                  uk.ac.ebi.pride.jmztab.model.Metadata metadata,
                                                  boolean hasQuantitation) {
        return transformIdent(rawIdent, rawIndex, rawPsms, rawPeptides, metadata, hasQuantitation);
    }

    public static Protein transformIdent(uk.ac.ebi.pride.jmztab.model.Protein rawIdent,
                                         Integer rawIndex,
                                         Map<String, uk.ac.ebi.pride.jmztab.model.PSM> rawPsms,
                                         Map<String, uk.ac.ebi.pride.jmztab.model.Peptide> rawPeptides,
                                         uk.ac.ebi.pride.jmztab.model.Metadata metadata,
                                         boolean hasQuantitation
    ) {

        Protein ident = null;

        if (rawIdent != null) {
            //Database
            CvParam cvParam = CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DATABASE, rawIdent.getDatabase());
            //CvParams
            ParamGroup paramGroup = new ParamGroup(cvParam, null);
            SearchDataBase searchDataBase = new SearchDataBase(rawIdent.getDatabase(), rawIdent.getDatabaseVersion(), paramGroup);
            DBSequence dbSequence = new DBSequence(rawIdent.getAccession(), searchDataBase, null, null);
            //DbSequence Object
            dbSequence.setId(rawIdent.getAccession());

            // add Protein scores as parameters
            List<CvParam> proteinScores = transformSearchEngineProteinScores(rawIdent, metadata);
            paramGroup.addCvParams(proteinScores);

            Double seqConverage = rawIdent.getProteinCoverage();
            double seqConverageVal = seqConverage == null ? -1 : seqConverage;
            Double threshold = null;
            double thresholdVal = threshold == null ? -1 : threshold;
            Score score = DataAccessUtilities.getScore(paramGroup);

            if (rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN) != null && !rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN).isEmpty())
                dbSequence.setSequence(rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN));

            if (rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN) != null && !rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN).isEmpty())
                paramGroup.addCvParam(CvUtilities.getCVTermFromCvReference(CvTermReference.PRIDE_DECOY_HIT, rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN)));

            if (rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEIN_ACC_COLUMN) != null && !rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEIN_ACC_COLUMN).isEmpty())
                dbSequence.setName(rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEIN_ACC_COLUMN));

            if (rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEOGROUPER) != null && !rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEOGROUPER).isEmpty())
                paramGroup.addCvParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_PROTEOGROUPER_PDHSCORE, rawIdent.getOptionColumnValue(MzTabUtils.OPTIONAL_PROTEOGROUPER)));

            //Quantitation Scores
            QuantScore quantScore = null;
            if (hasQuantitation)
                quantScore = transformQuantitationParams(rawIdent, metadata);

            //Convert Peptide Identifications
            List<Peptide> peptides = null;
            if (rawPsms != null) {
                peptides = new ArrayList<Peptide>();
                for (Map.Entry rawPeptide : rawPsms.entrySet()) {
                    String rawPsmIndex = rawPeptide.getKey().toString();
                    uk.ac.ebi.pride.jmztab.model.PSM rawPSM = (uk.ac.ebi.pride.jmztab.model.PSM) rawPeptide.getValue();
                    peptides.add(transformPeptide(rawPSM, dbSequence, rawPsmIndex, metadata, peptides.size()));
                }
            }

            //Convert Quantitation Peptides
            List<QuantPeptide> quantPeptides = null;
            if (rawPeptides != null && !rawPeptides.isEmpty()) {
                quantPeptides = new ArrayList<QuantPeptide>();
                for (Map.Entry entry : rawPeptides.entrySet()) {
                    String rawPeptideIndex = entry.getKey().toString();
                    uk.ac.ebi.pride.jmztab.model.Peptide rawPeptide = (uk.ac.ebi.pride.jmztab.model.Peptide) entry.getValue();
                    quantPeptides.add(transformQuantPeptide(rawPeptide, dbSequence, rawPeptideIndex, metadata, quantPeptides.size()));
                }
            }
            return new Protein(paramGroup, rawIndex.toString(), null, dbSequence, false, peptides, score, thresholdVal, seqConverageVal, null, quantScore, quantPeptides);
        }
        return ident;
    }

    private static QuantScore transformQuantitationParams(uk.ac.ebi.pride.jmztab.model.Protein rawIdent, Metadata metadata) {

        Map<Comparable, Double> studyVariable = new HashMap<Comparable, Double>();
        Map<Comparable, Double> assayAbundance = new HashMap<Comparable, Double>();
        for (Map.Entry entry : metadata.getStudyVariableMap().entrySet()) {
            String key = entry.getKey().toString();
            uk.ac.ebi.pride.jmztab.model.StudyVariable oldStudyVariable = (uk.ac.ebi.pride.jmztab.model.StudyVariable) entry.getValue();
            studyVariable.put(key, rawIdent.getAbundanceColumnValue(oldStudyVariable));
        }
        for (Map.Entry entry : metadata.getAssayMap().entrySet()) {
            String key = entry.getKey().toString();
            uk.ac.ebi.pride.jmztab.model.Assay oldAssay = (uk.ac.ebi.pride.jmztab.model.Assay) entry.getValue();
            assayAbundance.put(key, rawIdent.getAbundanceColumnValue(oldAssay));
        }
        //Apart of the scores the the method should be also added to the protein: like ITRAQ
        return new QuantScore(studyVariable, assayAbundance);
    }

    private static QuantScore transformQuantitationParams(uk.ac.ebi.pride.jmztab.model.Peptide rawIdent, Metadata metadata) {

        Map<Comparable, Double> studyVariable = new HashMap<Comparable, Double>();
        Map<Comparable, Double> assayAbundance = new HashMap<Comparable, Double>();
        for (Map.Entry entry : metadata.getStudyVariableMap().entrySet()) {
            String key = entry.getKey().toString();
            uk.ac.ebi.pride.jmztab.model.StudyVariable oldStudyVariable = (uk.ac.ebi.pride.jmztab.model.StudyVariable) entry.getValue();
            studyVariable.put(key, rawIdent.getAbundanceColumnValue(oldStudyVariable));
        }
        for (Map.Entry entry : metadata.getAssayMap().entrySet()) {
            String key = entry.getKey().toString();
            uk.ac.ebi.pride.jmztab.model.Assay oldAssay = (uk.ac.ebi.pride.jmztab.model.Assay) entry.getValue();
            assayAbundance.put(key, rawIdent.getAbundanceColumnValue(oldAssay));
        }
        //Apart of the scores the the method should be also added to the protein: like ITRAQ
        return new QuantScore(studyVariable, assayAbundance);
    }

    /**
     * Transform peptide from pride xml to core data model.
     *
     * @param rawPeptide peptide in pride xml format.
     * @return Peptide  peptide in core data model.
     */
    public static Peptide transformPeptide(uk.ac.ebi.pride.jmztab.model.PSM rawPeptide,
                                           DBSequence dbSequence,
                                           Comparable index,
                                           Metadata metadata, int indexPeptide) {

        // spectrum is reference from external

        Spectrum spectrum = null;

        // modifications

        List<uk.ac.ebi.pride.jmztab.model.Modification> rawMods = rawPeptide.getModifications();
        List<Modification> modifications = transformModification(rawMods, metadata);

        // fragmentIons information is not supported in mzTab

        List<FragmentIon> fragmentIons = null;

        // retrieve the scores
        ParamGroup params = new ParamGroup();
        List<CvParam> cvparams = transformPSMSearchEngineScoreCvTerm(rawPeptide, metadata);
        // Separate CVParams
        params.addCvParams(cvparams.stream().filter(cvParam -> cvParam.getAccession() != null).collect(Collectors.toList()));
        // From UserParams
        params.addUserParams(cvparams.stream()
                .filter(cvParam -> cvParam.getAccession() != null).collect(Collectors.toList())
                .stream()
                .map(cvParam -> new UserParam(cvParam.getName(), null, cvParam.getValue(), cvParam.getUnitAcc(), cvParam.getUnitName(), cvParam.getUnitCVLookupID())).collect(Collectors.toList()));
        params.addCvParam(transformPSMPrecursorMZ(rawPeptide, metadata));

        // start and stop position
        int startPos = -1;
        int stopPos = -1;
        Integer start = rawPeptide.getStart();
        if (start != null) {
            startPos = start;
        }

        Integer stop = rawPeptide.getEnd();
        if (stop != null) {
            stopPos = stop;
        }
        PeptideSequence peptideSequence = new PeptideSequence(null, null, rawPeptide.getSequence(), modifications);
        List<PeptideEvidence> peptideEvidences = new ArrayList<PeptideEvidence>();
        PeptideEvidence peptideEvidence = new PeptideEvidence(null, null, startPos, stopPos, false, peptideSequence, dbSequence);
        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002637", "chromosome name", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_STRAND_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_STRAND_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002638", "chromosome strand", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_STRAND_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROMEND_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROMEND_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002640", "peptide end on chromosome", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROMEND_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_COUNT_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_COUNT_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002641", "peptide exon count", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_COUNT_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_SIZES_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_SIZES_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002642", "peptide exon nucleotide sizes", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_SIZES_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_STARTS_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_STARTS_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002643", "peptide start positions on chromosome", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_EXON_STARTS_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_GENOME_REF_VERSION_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_GENOME_REF_VERSION_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002644", "genome reference version", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_GENOME_REF_VERSION_COLUMN), null, null, null));

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN).isEmpty())
            peptideEvidence.addCvParam(new CvParam("MS:1002356", "PSM-level combined FDRScore", "PSI-MS", rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN), null, null, null));

        peptideEvidences.add(peptideEvidence);

        //Retrieve Experimental Mass and Charge.
        // todo: need to review this bit of code to set charge
        Integer charge = rawPeptide.getCharge();
        double mz = DataAccessUtilities.getPrecursorMz(params);
        if (charge == null && spectrum != null) {
            charge = DataAccessUtilities.getPrecursorChargeParamGroup(spectrum);
            if (charge == null) {
                charge = DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors());
            }
        }
        // Retrieve Score
        Score score = DataAccessUtilities.getScore(params);

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN).isEmpty())
            params.addCvParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DECOY_PEPTIDE, rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN)));

        int rank = -1;
        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN).isEmpty() && NumberUtilities.isInteger(rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN)))
            rank = Integer.parseInt(rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN));

        //Each PSM is associated in mzTab with more than one spectrum in ms-data-core-api is only one.
        SpectrumIdentification spectrumIdentification = new SpectrumIdentification(params, index, null, (charge == null ? -1 : charge), mz, rawPeptide.getCalcMassToCharge(), -1, peptideSequence, rank, false, null, null, peptideEvidences, fragmentIons, score, spectrum, null);
        return new Peptide(peptideEvidence, spectrumIdentification, indexPeptide);
    }

    private static CvParam transformPSMPrecursorMZ(PSM rawPeptide, Metadata metadata) {
        if (rawPeptide.getExpMassToCharge() != null) {
            return CvUtilities.getCVTermFromCvReference(CvTermReference.PSI_ION_SELECTION_MZ, rawPeptide.getExpMassToCharge().toString());
        }
        return null;
    }

    /**
     * Transform quantitative peptide from pride xml to core data model.
     *
     * @param rawPeptide peptide in pride xml format.
     * @return Peptide  peptide in core data model.
     */
    public static QuantPeptide transformQuantPeptide(uk.ac.ebi.pride.jmztab.model.Peptide rawPeptide,
                                                     DBSequence dbSequence,
                                                     Comparable index,
                                                     Metadata metadata, int indexPeptide) {

        // spectrum is reference from external
        Spectrum spectrum = null;
        // modifications

        int indexRef = Integer.parseInt(index.toString().split("!")[1]);

        List<uk.ac.ebi.pride.jmztab.model.Modification> rawMods = rawPeptide.getModifications();
        List<Modification> modifications = transformModification(rawMods, metadata);

        // fragmentIons information is not supported in mzTab
        List<FragmentIon> fragmentIons = null;

        // retrieve the scores
        ParamGroup params = new ParamGroup();
        params.addCvParams(transformPeptideSearchEngineScoreCvTerm(rawPeptide, rawPeptide.getSpectraRef().get(indexRef - 1).getMsRun(), metadata));

        // start and stop position
        int startPos = -1;
        int stopPos = -1;

        PeptideSequence peptideSequence = new PeptideSequence(null, null, rawPeptide.getSequence(), modifications);
        List<PeptideEvidence> peptideEvidences = new ArrayList<PeptideEvidence>();
        PeptideEvidence peptideEvidence = new PeptideEvidence(null, null, startPos, stopPos, false, peptideSequence, dbSequence);
        peptideEvidences.add(peptideEvidence);

        //Retrieve Experimental Mass and Charge.
        // todo: need to review this bit of code to set charge
        Integer charge = rawPeptide.getCharge();
        double mz = DataAccessUtilities.getPrecursorMz(params);
        if (charge == null && spectrum != null) {
            charge = DataAccessUtilities.getPrecursorChargeParamGroup(spectrum);
            if (charge == null) {
                charge = DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors());
            }
        }
        // Retrieve Score
        Score score = DataAccessUtilities.getScore(params);

        QuantScore quantScore = transformQuantitationParams(rawPeptide, metadata);

        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN).isEmpty())
            params.addCvParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DECOY_PEPTIDE, rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN)));

        int rank = -1;
        if (rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN) != null && !rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN).isEmpty() && NumberUtilities.isInteger(rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN)))
            rank = Integer.parseInt(rawPeptide.getOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN));

        SpectrumIdentification spectrumIdentification = new SpectrumIdentification(params, index, null, (charge == null ? -1 : charge), mz, -1, -1, peptideSequence, rank, false, null, null, peptideEvidences, fragmentIons, score, spectrum, null);

        return new QuantPeptide(peptideEvidence, spectrumIdentification, quantScore, indexPeptide);

    }

    private static List<CvParam> transformPSMSearchEngineScoreCvTerm(PSM rawPeptide, Metadata metadata) {
        List<CvParam> scoreParams = new ArrayList<CvParam>();
        for (PSMSearchEngineScore psmSearchEngineScore : metadata.getPsmSearchEngineScoreMap().values()) {
            CvParam cvParam = MzTabUtils.convertParamToCvParam(psmSearchEngineScore.getParam());
            Double searchEngineScore = rawPeptide.getSearchEngineScore(psmSearchEngineScore.getId());
            if (searchEngineScore != null) {
                cvParam.setValue(searchEngineScore.toString());
                scoreParams.add(cvParam);
            }
        }
        return scoreParams;
    }

    private static List<CvParam> transformPeptideSearchEngineScoreCvTerm(uk.ac.ebi.pride.jmztab.model.Peptide rawPeptide, MsRun msRun, Metadata metadata) {
        List<CvParam> scoreParams = new ArrayList<CvParam>();
        for (PeptideSearchEngineScore peptideSearchEngineScore : metadata.getPeptideSearchEngineScoreMap().values()) {
            CvParam cvParam = MzTabUtils.convertParamToCvParam(peptideSearchEngineScore.getParam());
            Double searchEngineScore = rawPeptide.getSearchEngineScore(peptideSearchEngineScore.getId(), msRun);
            if (searchEngineScore != null) {
                cvParam.setValue(searchEngineScore.toString());
                scoreParams.add(cvParam);
            }
        }
        return scoreParams;
    }

    /**
     * Transform modification from pride xml to core data model
     *
     * @param rawMods a java.utils.List<uk.ac.ebi.pride.jmztab.model.Modification> modification in mzTab format.
     * @return Modification modification in core data model.
     */
    private static List<Modification> transformModification(List<uk.ac.ebi.pride.jmztab.model.Modification> rawMods, Metadata metadata) {

        List<Modification> modifications = new ArrayList<Modification>();

        List<FixedMod> fixedMods = new ArrayList<FixedMod>();
        for (FixedMod fixedmod : metadata.getFixedModMap().values()) {
            boolean contained = false;
            for (FixedMod mod : fixedMods)
                if (mod.getParam().getAccession().equalsIgnoreCase(fixedmod.getParam().getAccession()))
                    contained = true;
            if (!contained)
                fixedMods.add(fixedmod);
        }

        List<VariableMod> varaibleMods = new ArrayList<VariableMod>();
        for (VariableMod variableMod : metadata.getVariableModMap().values()) {
            boolean contained = false;
            for (VariableMod mod : varaibleMods)
                if (mod.getParam().getAccession().equalsIgnoreCase(variableMod.getParam().getAccession()))
                    contained = true;
            if (!contained)
                varaibleMods.add(variableMod);
        }

        //Look in Fixed Modifications
        for (FixedMod modFixed : fixedMods) {
            for (uk.ac.ebi.pride.jmztab.model.Modification rawMod : rawMods) {
                String rawModAccession = getAccesion(rawMod);
                if (rawModAccession.equalsIgnoreCase(modFixed.getParam().getAccession())) {
                    List<Double> monoDelta = null;
                    if (modFixed.getParam().getValue() != null && NumberUtilities.isNumber(modFixed.getParam().getValue())) {
                        monoDelta = new ArrayList<Double>();
                        monoDelta.add(new Double(modFixed.getParam().getValue()));
                    }
                    List<Double> avgDelta = null;
                    //Add the name of the modification
                    ParamGroup params = new ParamGroup();
                    if (modFixed.getParam() != null)
                        params.addCvParam(MzTabUtils.convertParamToCvParam(modFixed.getParam()));

                    String name = getModificationName(params, rawModAccession);

                    //If the modification is annotated in more than one aminoacid (ambiguity modification we will replicate the modification in more than one aminoacid
                    //add all the scores in the CVTerm)

                    Map<Integer, CVParam> rawLocation = rawMod.getPositionMap();

                    if (rawLocation.size() == 1) {
                        int location = rawLocation.keySet().iterator().next();
                        if (rawLocation.values().iterator().next() != null)
                            params.addCvParam(MzTabUtils.convertParamToCvParam(rawLocation.values().iterator().next()));
                        Modification modification = new Modification(params, rawModAccession, name, location, null, avgDelta, monoDelta, null, null);
                        modifications.add(modification);
                    } else if (rawLocation.size() > 1) {
                        for (Map.Entry entry : rawLocation.entrySet()) {
                            int location = (Integer) entry.getKey();
                            Param param = (Param) entry.getValue();
                            ParamGroup paramGroup = new ParamGroup();
                            paramGroup.addCvParams(params.getCvParams());
                            if (param != null)
                                paramGroup.addCvParam(MzTabUtils.convertParamToCvParam(param));
                            Modification modification = new Modification(paramGroup, rawModAccession, name, location, null, avgDelta, monoDelta, null, null);
                            modifications.add(modification);
                        }
                    }
                }
            }
        }
        // Look in variable modifications
        for (VariableMod modVariable : varaibleMods) {

            for (uk.ac.ebi.pride.jmztab.model.Modification rawMod : rawMods) {
                String rawModAccession = getAccesion(rawMod);
                if (rawModAccession.equalsIgnoreCase(modVariable.getParam().getAccession())) {
                    List<Double> monoDelta = null;
                    if (modVariable.getParam().getValue() != null && NumberUtilities.isNumber(modVariable.getParam().getValue())) {
                        monoDelta = new ArrayList<Double>();
                        monoDelta.add(new Double(modVariable.getParam().getValue()));
                    }
                    List<Double> avgDelta = null;
                    //Add the name of the modification
                    ParamGroup params = new ParamGroup();
                    if (modVariable.getParam() != null)
                        params.addCvParam(MzTabUtils.convertParamToCvParam(modVariable.getParam()));

                    String name = getModificationName(params, rawModAccession);

                    //If the modification is annotated in more than one aminoacid (ambiguity modification we will replicate the modification in more than one aminoacid
                    //add all the scores in the CVTerm)

                    Map<Integer, CVParam> rawLocation = rawMod.getPositionMap();

                    if (rawLocation.size() == 1) {
                        int location = rawLocation.keySet().iterator().next();
                        if (rawLocation.values().iterator().next() != null)
                            params.addCvParam(MzTabUtils.convertParamToCvParam(rawLocation.values().iterator().next()));
                        Modification modification = new Modification(params, rawModAccession, name, location, null, avgDelta, monoDelta, null, null);
                        modifications.add(modification);
                    } else if (rawLocation.size() > 1) {
                        for (Map.Entry entry : rawLocation.entrySet()) {
                            int location = (Integer) entry.getKey();
                            Param param = (Param) entry.getValue();
                            ParamGroup paramGroup = new ParamGroup();
                            paramGroup.addCvParams(params.getCvParams());
                            if (param != null)
                                paramGroup.addCvParam(MzTabUtils.convertParamToCvParam(param));
                            Modification modification = new Modification(paramGroup, rawModAccession, name, location, null, avgDelta, monoDelta, null, null);
                            modifications.add(modification);
                        }
                    }
                }
            }
        }
        return modifications;
    }

    private static String getAccesion(uk.ac.ebi.pride.jmztab.model.Modification rawMod) {
        if (rawMod != null && !rawMod.getType().equals(uk.ac.ebi.pride.utilities.term.CvTermReference.MS_NEUTRAL_LOSS))
            return rawMod.getType().name() + ":" + rawMod.getAccession();
        return rawMod.getAccession();
    }

    private static String getModificationName(ParamGroup paramGroup, String accession) {
        String name = null;
        if (paramGroup != null) {
            List<CvParam> cvParams = paramGroup.getCvParams();
            if (cvParams != null) {
                for (CvParam cvParam : cvParams) {
                    if (cvParam.getAccession().equals(accession)) {
                        name = cvParam.getName();
                    }
                }
            }
        }
        return name;
    }

    private static List<CvParam> transformSearchEngineProteinScores(uk.ac.ebi.pride.jmztab.model.Protein rawIdent, Metadata metadata) {
        List<CvParam> scoreParams = new ArrayList<CvParam>();
        for (ProteinSearchEngineScore proteinSearchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
            CvParam cvParam = MzTabUtils.convertParamToCvParam(proteinSearchEngineScore.getParam());
            for (MsRun msRun : metadata.getMsRunMap().values()) {
                Double searchEngineScore = rawIdent.getSearchEngineScore(proteinSearchEngineScore.getId(), msRun);
                if (searchEngineScore != null) {
                    cvParam.setValue(searchEngineScore.toString());
                    scoreParams.add(cvParam);
                }
            }
        }
        return scoreParams;
    }

    public static ExperimentProtocol transformProtocol(SortedMap<Integer, SplitList<Param>> sampleProcession) {
        ExperimentProtocol protocol = null;
        if (sampleProcession != null) {
            List<ParamGroup> protocolSteps = new ArrayList<ParamGroup>();
            for (Map.Entry entry : sampleProcession.entrySet()) {
                ParamGroup paramGroup = new ParamGroup();
                SplitList<Param> params = (SplitList<Param>) entry.getValue();
                for (Param param : params)
                    paramGroup.addCvParam(MzTabUtils.convertParamToCvParam(param));
                protocolSteps.add(paramGroup);
            }
            protocol = new ExperimentProtocol(null, MzTabTransformer.PROTOCOL_ID, null, protocolSteps);
        }
        return protocol;
    }

    public static Map<Comparable, SpectraData> transformMsRunMap(Map<Integer, MsRun> mRunMap) {
        Map<Comparable, SpectraData> spectraDataMap = new HashMap<Comparable, SpectraData>();
        for (Map.Entry entry : mRunMap.entrySet())
            spectraDataMap.put((entry.getKey()).toString(), transformMsRunToSpectraData((MsRun) entry.getValue()));
        return spectraDataMap;
    }

    private static SpectraData transformMsRunToSpectraData(MsRun msRun) {
        SpectraData spectraData;
        CvParam paramFormat = null;
        if (msRun.getFormat() != null)
            paramFormat = MzTabUtils.convertParamToCvParam(msRun.getFormat());

        CvParam idFormat = null;
        if (msRun.getIdFormat() != null)
            idFormat = MzTabUtils.convertParamToCvParam(msRun.getIdFormat());

        spectraData = new SpectraData(msRun.getId().toString(), msRun.getReference(), msRun.getLocation().getPath(), paramFormat, null, idFormat);
        return spectraData;

    }

    public static Collection<InstrumentConfiguration> transformInstrument(Map<Integer, Instrument> oldInstruments) {
        // We start with an empty list of instrument configurations
        List<InstrumentConfiguration> instrumentConfigurations = new ArrayList<InstrumentConfiguration>();

        for (Map.Entry entry : oldInstruments.entrySet()) {
            Instrument instrument = (Instrument) entry.getValue();
            if (instrument != null) {
                // create instrument param group to aid semantic support
                ParamGroup params = new ParamGroup();

                params.addCvParam(MzTabUtils.convertParamToCvParam(instrument.getName()));
                // create instrument components
                int sourceOrder = 1;

                //Source Instrument File Creation
                int detectorOrder = instrument.getAnalyzerList().size() + 2;
                List<InstrumentComponent> source = new ArrayList<InstrumentComponent>();
                InstrumentComponent sourceInstrument = transformSource(sourceOrder, instrument.getSource());
                source.add(sourceInstrument);

                List<InstrumentComponent> detector = new ArrayList<InstrumentComponent>();
                InstrumentComponent detectorInstrument = transformDetector(detectorOrder, instrument.getDetector());
                detector.add(detectorInstrument);

                List<Param> rawAnalyzers = instrument.getAnalyzerList();

                int orderCnt = 2;
                List<InstrumentComponent> analyzer = new ArrayList<InstrumentComponent>();
                for (Param rawAnalyzer : rawAnalyzers) {
                    InstrumentComponent analyzerInstrument = transformAnalyzer(orderCnt, rawAnalyzer);
                    analyzer.add(analyzerInstrument);
                    orderCnt++;
                }
                instrumentConfigurations.add(new InstrumentConfiguration(instrument.getId().toString(), null, null, source, analyzer, detector, params));
            }
        }

        return instrumentConfigurations;
    }

    private static InstrumentComponent transformAnalyzer(int orderCnt, Param rawAnalyzer) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have mass analyzer type (MS:1000443)
        if (rawAnalyzer != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertParamToCvParam(rawAnalyzer));
            component = new InstrumentComponent(orderCnt, params);
        }
        return component;
    }

    private static InstrumentComponent transformDetector(int detectorOrder, Param detector) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have detector type (MS:1000026)
        if (detector != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertParamToCvParam(detector));
            component = new InstrumentComponent(detectorOrder, params);
        }

        return component;
    }

    private static InstrumentComponent transformSource(int sourceOrder, Param source) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have ionization type (MS:1000008)
        if (source != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertParamToCvParam(source));
            component = new InstrumentComponent(sourceOrder, params);
        }

        return component;
    }

    public static List<SearchDataBase> transformDatabases(Set<Tuple<String, String>> oldDatabases) {


        List<SearchDataBase> databases = new ArrayList<SearchDataBase>();
        if (oldDatabases != null && oldDatabases.size() > 0) {
            for (Tuple<String, String> databaseString : oldDatabases) {
                String name = databaseString.getKey();
                String version = databaseString.getValue();
                ParamGroup params = new ParamGroup();
                CvParam databaseName = CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DATABASE, name);
                params.addCvParam(databaseName);
                databases.add(new SearchDataBase(name, version, params));
            }
        }
        return databases;
    }

    public static Map<Comparable, StudyVariable> transformStudyVariables(Metadata metadata, boolean quantitationExperiment) {
        Map<Comparable, StudyVariable> studyVariables = new HashMap<Comparable, StudyVariable>();
        if (quantitationExperiment && metadata.getStudyVariableMap() != null && metadata.getStudyVariableMap().size() > 0) {
            for (Map.Entry entry : metadata.getStudyVariableMap().entrySet()) {
                String key = entry.getKey().toString();

                uk.ac.ebi.pride.jmztab.model.StudyVariable oldStudyVariable = (uk.ac.ebi.pride.jmztab.model.StudyVariable) entry.getValue();
                StudyVariable studyVariable = new StudyVariable(key, oldStudyVariable.getDescription());

                List<Sample> samples = new ArrayList<Sample>();
                for (uk.ac.ebi.pride.jmztab.model.Sample oldsample : oldStudyVariable.getSampleMap().values()) {
                    samples.add(transformSample(oldsample));
                }
                studyVariable.setSamples(samples);

                List<Assay> assays = new ArrayList<Assay>();
                for (uk.ac.ebi.pride.jmztab.model.Assay oldassay : oldStudyVariable.getAssayMap().values()) {
                    assays.add(transformAssay(oldassay));
                }
                studyVariable.setAssays(assays);

                studyVariable.setDescription(oldStudyVariable.getDescription());

                studyVariables.put(key, studyVariable);
            }
        }
        return studyVariables;
    }

    /**
     * Trnasform an assay from mzTab to Assay in ms-data-core-api model
     *
     * @param oldAssay uk.ac.ebi.pride.jmztab.model.Assay
     * @return Assay
     */
    private static Assay transformAssay(uk.ac.ebi.pride.jmztab.model.Assay oldAssay) {
        if (oldAssay != null) {
            String key = oldAssay.getId().toString();
            String name = oldAssay.getReference();
            Sample sample = transformSample(oldAssay.getSample());
            CvParam reagent = MzTabUtils.convertParamToCvParam(oldAssay.getQuantificationReagent());
            ParamGroup params = new ParamGroup();
            params.addCvParam(reagent);
            return new Assay(params, key, name, sample, reagent);
        }
        return null;
    }
}

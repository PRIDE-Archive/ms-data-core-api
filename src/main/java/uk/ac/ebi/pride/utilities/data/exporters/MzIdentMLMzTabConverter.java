package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.utils.convert.ModParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineParam;
import uk.ac.ebi.pride.jmztab.utils.convert.utils.MZIdentMLUtils;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static uk.ac.ebi.pride.utilities.data.utils.MzTabUtils.removeNewLineAndTab;

/**
 * @author Yasset Perez-Riverol
 * @author ntoro
 */
public class MzIdentMLMzTabConverter extends AbstractMzTabConverter {

    //TODO: Move the parameters
    public static final String PEPTIDE_N_TERM = "MS:1001189";
    public static final String PROTEIN_N_TERM = "MS:1002057";
    public static final String PEPTIDE_C_TERM = "MS:1001190";
    public static final String PROTEIN_C_TERM = "MS:1002058";
    private static final String UNKNOWN_MOD = "MS:1001460";
    public static final String CHEMMOD = "CHEMMOD";
    public static final String UNKNOWN_MODIFICATION = "unknown modification";

    protected static Logger logger = Logger.getLogger(MzIdentMLMzTabConverter.class);

    protected Map<Comparable, Integer> spectraToRun;

    protected Map<Param, Set<String>> variableModifications = new HashMap<Param, Set<String>>();
    Map<Comparable, Integer> indexSpectrumID = new HashMap<Comparable, Integer>();

    private final MzIdentMLControllerImpl controller;

    protected Set<Comparable> proteinIds;

    /**
     * Default constructor
     * @param controller The DataAccessController to be Converted to MzTab
     */
    public MzIdentMLMzTabConverter(MzIdentMLControllerImpl controller) {
        super(controller);
        this.controller = controller;

    }
    /**
     * MzIdentML is always an Identification File
     * @return True if the DataAccessController is an Identification Experiment
     */
    @Override
    protected boolean isIdentification() {
        return true;
    }

    @Override
    protected void loadInstrument() {
        //Mzidentml do not have instrument information;
    }

    @Override
    protected void loadMsRun() {
        List<SpectraData> spectraDataList = source.getExperimentMetaData().getSpectraDatas();
        spectraToRun = new HashMap<Comparable, Integer>(spectraDataList.size());
        if(!spectraDataList.isEmpty()){
            int idRun = 1;
            for(SpectraData spectradata: spectraDataList){
                if(spectradata.getFileFormat() != null && spectradata.getFileFormat() != null)
                    metadata.addMsRunFormat(idRun, MzTabUtils.convertCvParamToCVParam(spectradata.getFileFormat()));
                if(spectradata.getSpectrumIdFormat() != null && spectradata.getSpectrumIdFormat() != null)
                    metadata.addMsRunIdFormat(idRun, MzTabUtils.convertCvParamToCVParam(spectradata.getSpectrumIdFormat()));

                String location = (spectradata.getLocation() != null && !spectradata.getLocation().isEmpty())?spectradata.getLocation():spectradata.getName();
                if(location != null && !location.isEmpty() && !location.contains("file:")) location = "file:"+location;
                if(location == null) location="";
                try{
                    metadata.addMsRunLocation(idRun, new URL(location));
                }catch (MalformedURLException e){
                    throw new DataAccessException("Error while adding ms run location", e);
                }
                spectraToRun.put(spectradata.getId(), idRun);
                idRun++;
            }
        }
    }

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory} which maintain a couple of {@link uk.ac.ebi.pride.jmztab.model.ProteinColumn}
     */
    @Override
    protected MZTabColumnFactory convertProteinColumnFactory() {
        this.proteinColumnFactory = MZTabColumnFactory.getInstance(Section.Protein);
        this.proteinColumnFactory.addDefaultStableColumns();

        // ms_run[1] optional columns
        for(MsRun msRun: metadata.getMsRunMap().values()){
            proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PSMS, msRun);
            proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_DISTINCT, msRun);
            proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_UNIQUE, msRun);
        }

        // for quantification file, need provide all optional columns for each ms_run.
        if (!isIdentification()) {
            for (Assay assay : metadata.getAssayMap().values()) {
                proteinColumnFactory.addAbundanceOptionalColumn(assay);
            }
        }

        //TODO check identification and summary
        for (Integer id : metadata.getProteinSearchEngineScoreMap().keySet()) {
            //To be compliance with the specification you need the columns in the psms too
            proteinColumnFactory.addBestSearchEngineScoreOptionalColumn(ProteinColumn.BEST_SEARCH_ENGINE_SCORE, id);
            //proteinColumnFactory.addSearchEngineScoreOptionalColumn(ProteinColumn.SEARCH_ENGINE_SCORE, id, metadata.getMsRunMap().get(1));
        }

        for(MsRun msRun: metadata.getMsRunMap().values())
            for(Integer idScore: metadata.getProteinSearchEngineScoreMap().keySet())
                proteinColumnFactory.addSearchEngineScoreOptionalColumn(ProteinColumn.SEARCH_ENGINE_SCORE, idScore, msRun);
        // check and set additional chromosome columns
        if (hasChromInformation()) {
            proteinColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_PROTEIN_NAME_COLUMN, String.class);
        }
        return proteinColumnFactory;
    }

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory} which maintain a couple of {@link uk.ac.ebi.pride.jmztab.model.PSMColumn}
     */
    @Override
    protected MZTabColumnFactory convertPSMColumnFactory() {
        this.psmColumnFactory = MZTabColumnFactory.getInstance(Section.PSM);
        psmColumnFactory.addDefaultStableColumns();
        psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_ID_COLUMN,String.class);
        psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_DECOY_COLUMN, Integer.class);
        psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_RANK_COLUMN, Integer.class);

        //Search engine score information (mandatory for all)
        for (Integer id : metadata.getPsmSearchEngineScoreMap().keySet()) {
            psmColumnFactory.addSearchEngineScoreOptionalColumn(PSMColumn.SEARCH_ENGINE_SCORE, id, null);
        }
        // check and set additional chromosome columns
        if (hasChromInformation()) {
            psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_CHROM_COLUMN, String.class);
            psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_CHROMSTART_COLUMN, String.class);
            psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_CHROMEND_COLUMN, String.class);
            psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_STRAND_COLUMN, String.class);
            psmColumnFactory.addOptionalColumn(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN, String.class);
        }
        return this.psmColumnFactory;
    }

    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected void fillData() {
        // Get a list of Identification ids
        proteinIds = new HashSet<Comparable>();

        try {
            if (!source.hasProteinAmbiguityGroup()) {
                Collection<Comparable> proteinIds = source.getProteinIds();
                //Iterate over proteins
                for (Comparable id : proteinIds) {
                    uk.ac.ebi.pride.utilities.data.core.Protein msProtein = source.getProteinById(id);
                    List<Peptide> peptides = msProtein.getPeptides();
                    Protein identification = loadProtein(msProtein, peptides);
                    proteins.add(identification);
                    psms.addAll(loadPSMs(msProtein, peptides));
                }
            } else {
                Collection<Comparable> proteinGroupIds = source.getProteinAmbiguityGroupIds();
                for (Comparable proteinGroupId : proteinGroupIds) {
                    Protein identification = getProteinGroupById(proteinGroupId);
                    proteins.add(identification);
                    psms.addAll(loadPSMs(source.getProteinAmbiguityGroupById(proteinGroupId).getProteinIds()));
                }
            }
        } catch (JAXBException e) {
            throw new DataAccessException("Error try to retrieve the information for own Protein");

        }

        loadMetadataModifications();
    }


    private Protein getProteinGroupById(Comparable proteinGroupId) throws JAXBException {

        ProteinGroup proteinAmbiguityGroup = source.getProteinAmbiguityGroupById(proteinGroupId);
        //Todo: We will annotated only the first protein, the core protein.
        uk.ac.ebi.pride.utilities.data.core.Protein firstProteinDetectionHypothesis = proteinAmbiguityGroup.getProteinDetectionHypothesis().get(0);
        if (proteinIds.contains(firstProteinDetectionHypothesis.getDbSequence().getAccession()))
            throw new DataAccessException("mzTab do not support one protein in more than one ambiguity groups.");
        else
            proteinIds.add(firstProteinDetectionHypothesis.getDbSequence().getAccession());

        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = firstProteinDetectionHypothesis.getPeptides();
        Protein protein = loadProtein(firstProteinDetectionHypothesis, peptides);

        String membersString = "";
        for(int i=1; i < proteinAmbiguityGroup.getProteinDetectionHypothesis().size();i++)
            membersString = proteinAmbiguityGroup.getProteinDetectionHypothesis().get(i).getDbSequence().getAccession() + ",";
        membersString = (membersString.isEmpty())?membersString:membersString.substring(0,membersString.length()-1);
        protein.addAmbiguityMembers(membersString);

        //Loop for spectrum to get all the ms_run to repeat the score at protein level
        Set<MsRun> msRuns = new HashSet<MsRun>();
        for (Peptide peptide : peptides) {
//            Comparable id = source.getPeptideSpectrumId(firstProteinDetectionHypothesis.getId(), index);
            Comparable id = controller.getSpectrumIdBySpectrumIdentificationItemId(peptide.getSpectrumIdentification().getId());

            if (id != null) {
                String[] spectumMap = id.toString().split("!");
                MsRun msRun = metadata.getMsRunMap().get(spectraToRun.get(spectumMap[1]));
                msRuns.add(msRun);
            }
        }
        // See which protein scores are supported

        for(CvParam cvPAram: firstProteinDetectionHypothesis.getCvParams()){
            if(proteinScoreToScoreIndex.containsKey(cvPAram.getAccession())){
                CVParam param = MzTabUtils.convertCvParamToCVParam(cvPAram);
                int idCount = proteinScoreToScoreIndex.get(cvPAram.getAccession());
                for (MsRun msRun: metadata.getMsRunMap().values()){
                    String value = null;
                    if(msRuns.contains(msRun))
                        value = param.getValue();
                    protein.setSearchEngineScore(idCount,msRun, value);
                }
            }
        }
        return protein;
    }

    protected void loadMetadataModifications() {

        final IdentificationMetaData identificationMetaData = source.getIdentificationMetaData();

        int i = 1;
        int j = 1;

        if (identificationMetaData != null) {
            final List<SpectrumIdentificationProtocol> spectrumIdDetectionProtocols = identificationMetaData.getSpectrumIdentificationProtocols();
            if (spectrumIdDetectionProtocols != null) {
                for (SpectrumIdentificationProtocol spectrumIdDetectionProtocol : spectrumIdDetectionProtocols) {
                    final List<SearchModification> searchModifications = spectrumIdDetectionProtocol.getSearchModifications();
                    if(searchModifications != null){
                        for (SearchModification searchModification : searchModifications) {
                            Param param = MzTabUtils.convertCvParamToCVParam(searchModification.getCvParams().get(0), searchModification.getMassDelta());

                            if (param != null) {

                                String site = null;
                                String position = null;

                                if(param.getAccession().equalsIgnoreCase(UNKNOWN_MOD)){
                                    //Transform in a CHEMMOD Type modification
                                    param = createUnknownModification(param.getValue());
                                }

                                if (searchModification.getSpecificities() != null && !searchModification.getSpecificities().isEmpty()) {

                                    site = searchModification.getSpecificities().get(0);

                                    final int size = searchModification.getSpecificities().size();
                                    if (size > 1) {
                                        //We annotate only one site in mzTab
                                        logger.warn("More than one residue specify");
                                        for (int k = 1; k < size; k++) {
                                            site = site + " " + searchModification.getSpecificities().get(k);
                                        }
                                    }

                                    if (site.equalsIgnoreCase(".")) {
                                        //We try to find a more specific site in the rules
                                        for (CvParam rule : searchModification.getSpecificityRules()) {
                                            if (rule.getAccession().equalsIgnoreCase(PEPTIDE_N_TERM)) {
                                                site = "N-term";
                                                position = "Peptide N-term";
                                            } else if (rule.getAccession().equalsIgnoreCase(PROTEIN_N_TERM)) {
                                                site = "N-term";
                                                position = "Protein N-term";
                                            } else if (rule.getAccession().equalsIgnoreCase(PEPTIDE_C_TERM)) {
                                                site = "C-term";
                                                position = "Peptide C-term";
                                            } else if (rule.getAccession().equalsIgnoreCase(PROTEIN_C_TERM)) {
                                                site = "C-term";
                                                position = "Protein C-term";
                                            } else {
                                                logger.warn("Cv Term for Rule: " + rule.toString() + "is not recognized");
                                                site = "C-term or N-term";
                                            }
                                        }
                                    }
                                }


                                if(searchModification.isFixedMod()){
                                    FixedMod mod = new FixedMod(i++);
                                    mod.setParam(param);
                                    if(site!= null){
                                        mod.setSite(site);
                                    }
                                    if(position!= null) {
                                        mod.setPosition(position);
                                    }
                                    metadata.addFixedMod(mod);
                                }
                                else {
                                    VariableMod mod = new VariableMod(j++);
                                    mod.setParam(param);
                                    if(site!= null){
                                        mod.setSite(site);
                                    }
                                    if(position!= null){
                                        mod.setPosition(position);
                                    }
                                    metadata.addVariableMod(mod);

                                }
                            }
                        }
                    }
                }
            }
        }


        if(metadata.getFixedModMap().isEmpty()){
            Comment comment = new Comment("Only variable modifications can be reported when the original source is a MZIdentML XML file");
            getMZTabFile().addComment(1, comment);
            metadata.addFixedModParam(1, new CVParam("MS", "MS:1002453", "No fixed modifications searched", null));
        }
        if(metadata.getVariableModMap().isEmpty()){
            metadata.addVariableModParam(1, new CVParam("MS", "MS:1002454", "No variable modifications searched", null));
        }
    }

    private Param createUnknownModification(String value) {
        return new CVParam(CHEMMOD, CHEMMOD + ":" + value, UNKNOWN_MODIFICATION, null);
    }

    @Override
    protected void loadSoftware() {

        List<Software> softwareList = source.getExperimentMetaData().getSoftwares();
        Protocol proteinDetectionProtocol = source.getIdentificationMetaData().getProteinDetectionProtocol();
        List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList = source.getIdentificationMetaData().getSpectrumIdentificationProtocols();

        if (!softwareList.isEmpty()) {

            for (int i = 0; i < softwareList.size(); i++) {

                if (!softwareList.get(i).getCvParams().isEmpty() || !softwareList.get(i).getUserParams().isEmpty()) {

                    String version = (softwareList.get(i).getVersion() != null && !softwareList.get(i).getVersion().isEmpty()) ? softwareList.get(i).getVersion() : "";
                    CVParam nameCV = null;

                    if (!softwareList.get(i).getCvParams().isEmpty()) {
                        CvParam nameCVparam = softwareList.get(i).getCvParams().get(0);
                        if (nameCVparam != null) {
                            nameCV = new CVParam(nameCVparam.getCvLookupID(), nameCVparam.getAccession(), nameCVparam.getName(), version);
                        }
                    } else if (!softwareList.get(i).getUserParams().isEmpty()) {
                        UserParam nameUserParam = softwareList.get(i).getUserParams().get(0);
                        if (nameUserParam != null) {
                            nameCV = new CVParam(CvTermReference.MS_SOFTWARE.getAccession(), CvTermReference.MS_SOFTWARE.getCvLabel(), CvTermReference.MS_SOFTWARE.getName(), nameUserParam.getName() + version);
                        }
                    }

                    if (nameCV != null) {
                        metadata.addSoftwareParam(i + 1, nameCV);

                        if (proteinDetectionProtocol != null && proteinDetectionProtocol.getAnalysisSoftware() != null &&
                                proteinDetectionProtocol.getAnalysisSoftware().getId().equals(softwareList.get(i).getId())) {
                            if (proteinDetectionProtocol.getThreshold() != null) {
                                loadCvParamSettings(i + 1, proteinDetectionProtocol.getThreshold());

                                //Add FDR at Protein level if is annotated
                                for (CvParam cvParam : proteinDetectionProtocol.getThreshold().getCvParams())
                                    if (CvTermReference.MS_GLOBAL_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                            CvTermReference.MS_LOCAL_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                            CvTermReference.MS_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()))
                                        metadata.addFalseDiscoveryRateParam(MzTabUtils.convertCvParamToCVParam(cvParam));
                            }
                            if (proteinDetectionProtocol.getAnalysisParam() != null) {
                                loadCvParamSettings(i + 1, proteinDetectionProtocol.getAnalysisParam());
                            }
                        }

                        for (SpectrumIdentificationProtocol spectrumIdentificationProtocol : spectrumIdentificationProtocolList) {
                            if (spectrumIdentificationProtocol.getAnalysisSoftware().getId().equals(softwareList.get(i).getId())) {
                                if (spectrumIdentificationProtocol.getThreshold() != null) {
                                    loadCvParamSettings(i + 1, spectrumIdentificationProtocol.getThreshold());
                                    //Add FDR at PSM level if is annotated
                                    for (CvParam cvParam : spectrumIdentificationProtocol.getThreshold().getCvParams())
                                        if (CvTermReference.MS_GLOBAL_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                                CvTermReference.MS_LOCAL_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                                CvTermReference.MS_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()))
                                            metadata.addFalseDiscoveryRateParam(MzTabUtils.convertCvParamToCVParam(cvParam));
                                }

                                loadCvParamSettings(i + 1, spectrumIdentificationProtocol);

                                if (spectrumIdentificationProtocol.getFragmentTolerance() != null) {
                                    loadCvParamListSettings(i + 1, spectrumIdentificationProtocol.getFragmentTolerance());
                                }
                                if (spectrumIdentificationProtocol.getParentTolerance() != null) {
                                    loadCvParamListSettings(i + 1, spectrumIdentificationProtocol.getParentTolerance());
                                }
                                //Todo: See if we need to capture other objects from fragmentation table, etc.
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load in metadata all possible settings.
     * @param order order of the CvParam
     * @param paramGroup Param List
     */
    private void loadCvParamSettings(int order, ParamGroup paramGroup){

        loadCvParamListSettings(order, paramGroup.getCvParams());

        for(uk.ac.ebi.pride.utilities.data.core.UserParam userParam: paramGroup.getUserParams()){
            String value = userParam.getName();
            if(userParam.getValue()!=null){
                value = value + " = " + userParam.getValue();
            }
            metadata.addSoftwareSetting(order, value);
        }
    }

    /**
     * Insert in metadata only the CvTerm List Settings in an specific order
     * @param order order of the Param
     * @param paramList Param List
     */
    private void loadCvParamListSettings(int order, List<CvParam> paramList){
        for (CvParam cvParam: paramList){
            String value = cvParam.getName();
            if(cvParam.getValue()!=null){
                value = value + " = " + cvParam.getValue();
            }
            metadata.addSoftwareSetting(order, value);
        }
    }

    /**
     * Processes the experiment additional params: (f.e. quant method, description...).
     */
    @Override
    protected void loadExperimentParams() {
        String description = "";
        description = description + ("Spectrum Identification Protocol: ");
        List<SpectrumIdentificationProtocol> psmProtocols = (source.getIdentificationMetaData() != null)?source.getIdentificationMetaData().getSpectrumIdentificationProtocols():new ArrayList<SpectrumIdentificationProtocol>();
        for(SpectrumIdentificationProtocol protocol: psmProtocols){
            List<Enzyme> enzymes = protocol.getEnzymes();
            if(enzymes!= null && !enzymes.isEmpty()){
                description = description + ("Enzymes - ");
                for(Enzyme enzyme: enzymes){
                    String name = "";
                    if(enzyme.getEnzymeName() != null && enzyme.getEnzymeName().getCvParams().size() != 0){
                        name = enzyme.getEnzymeName().getCvParams().get(0).getName();
                    }else if(enzyme.getEnzymeName() != null && enzyme.getEnzymeName().getUserParams().size() != 0){
                        name = (enzyme.getEnzymeName().getUserParams().get(0).getValue() != null)?enzyme.getEnzymeName().getUserParams().get(0).getValue():enzyme.getEnzymeName().getUserParams().get(0).getName();
                    }
                    description = (!name.isEmpty())?description + name + " ":description;
                }
                description = description.substring(0,description.length()-1);
            }
            if(protocol.getFilters() != null){
                description = description + ("; Database Filters - ");
                for(Filter filter: protocol.getFilters()){
                    String name = (filter.getFilterType().getCvParams() != null)?filter.getFilterType().getCvParams().get(0).getName():"";
                    description = (!name.isEmpty())?description + name + " ":description;
                }
                description = description.substring(0,description.length()-1);
            }
        }
        metadata.setDescription(removeNewLineAndTab(description));
    }

    @Override
    protected void loadSampleProcessing() {

    }

    /**
     * Adds the sample parameters (species, tissue, cell type, disease) to the unit and the various sub-samples.
     */
    @Override
    protected void loadSamples() {
        List<Sample> sampleList = source.getExperimentMetaData().getSamples();
        if(sampleList != null && !sampleList.isEmpty()){
            int idSample = 1;

            for(Sample sample: sampleList){
                int specieId = 1;
                int tissueId = 1;
                int cellTypeId = 1;
                int diseaseId = 1;
                for (CvParam cv: sample.getCvParams()){

                    if ("NEWT".equals(cv.getCvLookupID())) {
                        metadata.addSampleSpecies(specieId, MzTabUtils.convertCvParamToCVParam(cv));
                        specieId++;
                    } else if ("BTO".equals(cv.getCvLookupID())) {
                        metadata.addSampleTissue(tissueId, MzTabUtils.convertCvParamToCVParam(cv));
                        tissueId++;
                    } else if ("CL".equals(cv.getCvLookupID())) {
                        metadata.addSampleCellType(cellTypeId, MzTabUtils.convertCvParamToCVParam(cv));
                        cellTypeId++;
                    } else if ("DOID".equals(cv.getCvLookupID()) || "IDO".equals(cv.getCvLookupID())) {
                        metadata.addSampleDisease(diseaseId, MzTabUtils.convertCvParamToCVParam(cv));
                        diseaseId++;
                    }
                }
                idSample++;

                //metadata.addSampleDescription(idSample, sample.getName());
            }
        }
    }

    @Override
    protected void loadGelData() {

    }

    protected List<PSM> loadPSMs(List<Comparable> ids) {
        List<PSM> psmList = new ArrayList<PSM>();
        for(Comparable id: ids) {
            uk.ac.ebi.pride.utilities.data.core.Protein protein = source.getProteinById(id);
            psmList.addAll(loadPSMs(protein, protein.getPeptides()));
        }
        return psmList;
    }

    /**
     * Converts the passed Identification object into an MzTab PSM.
     */
    protected List<PSM> loadPSMs(uk.ac.ebi.pride.utilities.data.core.Protein protein, List<Peptide> peptides)  {

        List<PSM> psmList = new ArrayList<PSM>();

        for (Peptide oldPSM : peptides) {
            PSM psm = new PSM(psmColumnFactory, metadata);
            psm.setSequence(oldPSM.getPeptideSequence().getSequence());
            psm.setPSM_ID(oldPSM.getSpectrumIdentification().getId().toString());
            psm.setAccession(removeNewLineAndTab(generateAccession(oldPSM)));

            ParamGroup nameDatabase = oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getNameDatabase();
            psm.setDatabase(getDatabaseName(nameDatabase.getCvParams(), nameDatabase.getUserParams()));

            String dbVersion = oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getVersion();
            String version = (dbVersion != null && !dbVersion.isEmpty()) ? dbVersion : null;
            psm.setDatabaseVersion(version);

            if (oldPSM.getPeptideEvidence().getStartPosition() != null && oldPSM.getPeptideEvidence().getStartPosition() >= 0) {
                psm.setStart(oldPSM.getPeptideEvidence().getStartPosition());
            }

            if (oldPSM.getPeptideEvidence().getEndPosition() != null && oldPSM.getPeptideEvidence().getEndPosition() >= 0) {
                psm.setEnd(oldPSM.getPeptideEvidence().getEndPosition());
            }

            String pre = String.valueOf(oldPSM.getPeptideEvidence().getPreResidue());
            String post = String.valueOf(oldPSM.getPeptideEvidence().getPostResidue());
            psm.setPre((pre == null || pre.isEmpty() || pre.equalsIgnoreCase(String.valueOf('\u0000'))) ? null : pre);
            psm.setPost((post == null || post.isEmpty() || pre.equalsIgnoreCase(String.valueOf('\u0000'))) ? null : post);

            //TODO: Review
            List<Modification> mods = new ArrayList<Modification>();

            for (uk.ac.ebi.pride.utilities.data.core.Modification oldMod : oldPSM.getPeptideSequence().getModifications()) {
                if (oldMod.getCvParams() != null) {
                    Double mass = (oldMod.getMonoisotopicMassDelta() != null && !oldMod.getMonoisotopicMassDelta().isEmpty()) ? oldMod.getMonoisotopicMassDelta().get(0) : null;

                    for (CvParam param : oldMod.getCvParams()) {
                        //Try to map it directly (if it fails we know that is an unknown mod)
                        Modification mzTabMod = MZTabUtils.parseModification(Section.PSM, param.getAccession());

                        if (mzTabMod != null) {
                            mzTabMod.addPosition(oldMod.getLocation(), null);
                            mods.add(mzTabMod);

                            //Fixed and Variable modification (detected in metadata)
//                    String site;
//                    if(oldMod.getLocation()-1 < 0)
//                        site = "N-Term";
//                    else if(oldPSM.getPeptideEvidence().getPeptideSequence().getSequence().length() <= oldMod.getLocation() -1)
//                        site = "C-Term";
//                    else
//                        site = String.valueOf(oldPSM.getPeptideEvidence().getPeptideSequence().getSequence().charAt(oldMod.getLocation()-1));

//                    Param param = MzTabUtils.convertCvParamToCVParam(oldMod.getCvParams().get(0), mass);

//                    if(!variableModifications.containsKey(param) || !variableModifications.get(param).contains(site)){
//                        Set<String> sites = new HashSet<String>();
//                        sites = (variableModifications.containsKey(param.getAccession()))?variableModifications.get(param.getAccession()):sites;
//                        sites.add(site);
//                        variableModifications.put(param, sites);
//                    }


                        } else if (param.getAccession().equalsIgnoreCase(UNKNOWN_MOD) && mass != null) {  //Unknown mod
                            //Transform in a CHEMMOD Type modification
                            mzTabMod = new Modification(Section.PSM, Modification.Type.CHEMMOD, mass.toString());
                            mzTabMod.addPosition(oldMod.getLocation(), null);
                            mods.add(mzTabMod);
                        } else if (param.getAccession().equalsIgnoreCase(CvTermReference.MS_NEUTRAL_LOSS.getAccession())) { //Neutral losses
                            Double value = 0.0;
                            if (param.getValue() != null) {
                                try {
                                    value = Double.valueOf(param.getValue());
                                } catch (NumberFormatException e) {
                                    logger.warn("Neutral loss value: " + param.getValue() + " cannot be converted.");
                                    value = 0.0;
                                }
                            }

                            CVParam lost = MzTabUtils.convertCvParamToCVParam(param, value);

                            Modification modNeutral = new Modification(Section.PSM, Modification.Type.NEUTRAL_LOSS, lost.getAccession());
                            modNeutral.setNeutralLoss(lost);
                            modNeutral.addPosition(oldMod.getLocation(), null);
                            mods.add(modNeutral);
                        } else {
                            //We have a problem parsing the CvTerm (e.g.: accession "UNIMOD:")
                            //TODO: Infer the CvTerm using the mass for conversion purposes
                            logger.warn("Modification with accession: " + param.getAccession() + " cannot be converted.");

                        }

                    }
                }
            }

            for (Modification mod : mods)
                psm.addModification(mod);

            psm.setExpMassToCharge(oldPSM.getSpectrumIdentification().getExperimentalMassToCharge());
            psm.setCharge(oldPSM.getSpectrumIdentification().getChargeState());
            psm.setCalcMassToCharge(oldPSM.getSpectrumIdentification().getCalculatedMassToCharge());
            Comparable idSpectrum = controller.getSpectrumIdBySpectrumIdentificationItemId(oldPSM.getSpectrumIdentification().getId());


            if (idSpectrum != null) {
                String[] spectumMap = idSpectrum.toString().split("!");
                String spectrumReference = null;
                for (SpectraData spec : source.getExperimentMetaData().getSpectraDatas()) {
                    if (spec.getId().toString().equalsIgnoreCase(spectumMap[1])) {
                        spectrumReference = MzTabUtils.getOriginalSpectrumId(spec, spectumMap[0]);
                    }
                }
                if (spectumMap[1] != null && spectrumReference != null)
                    psm.addSpectraRef(new SpectraRef(metadata.getMsRunMap().get(spectraToRun.get(spectumMap[1])), spectrumReference));
            }

            // See which psm scores are supported
            for (CvParam cvPAram : oldPSM.getSpectrumIdentification().getCvParams()) {
                if (psmScoreToScoreIndex.containsKey(cvPAram.getAccession())) {
                    CVParam param = MzTabUtils.convertCvParamToCVParam(cvPAram);
                    int idCount = psmScoreToScoreIndex.get(cvPAram.getAccession());
                    psm.setSearchEngineScore(idCount, param.getValue());
                }
            }
            //loadModifications(psm,peptideEvidenceRef.getPeptideEvidence());
            if (!indexSpectrumID.containsKey(oldPSM.getSpectrumIdentification().getId())) {
                int indexMzTab = indexSpectrumID.size() + 1;
                indexSpectrumID.put(oldPSM.getSpectrumIdentification().getId(), indexMzTab);
            }
            //Set Search Engine

            Set<SearchEngineParam> searchEngines = new HashSet<SearchEngineParam>();
            List<SearchEngineParam> searchEngineParams = MzIdentMLUtils.getSearchEngineCvTermReferences(oldPSM.getSpectrumIdentification().getCvParams());
            searchEngines.addAll(searchEngineParams);

            for (SearchEngineParam searchEngineParam : searchEngines)
                psm.addSearchEngineParam(searchEngineParam.getParam());

            //Set optional parameter

            psm.setPSM_ID(indexSpectrumID.get(oldPSM.getSpectrumIdentification().getId()));
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_ID_COLUMN, oldPSM.getSpectrumIdentification().getId());
            Boolean decoy = oldPSM.getPeptideEvidence().isDecoy();
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN, (!decoy) ? 0 : 1);
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN, oldPSM.getSpectrumIdentification().getRank());
            // check and set additional chromosome information
            if (hasChromInformation()) {
                psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_COLUMN, "null");
                psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROMSTART_COLUMN, "null");
                psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROMEND_COLUMN, "null");
                psm.setOptionColumnValue(MzTabUtils.OPTIONAL_STRAND_COLUMN, "null");
                psm.setOptionColumnValue(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN, "null");
                for (UserParam userParam : oldPSM.getPeptideEvidence().getUserParams()) {
                    switch (userParam.getName()) {
                        case ("chr"):
                            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROM_COLUMN, userParam.getValue());
                            break;
                        case ("start_map"):
                            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROMSTART_COLUMN, userParam.getValue());
                            break;
                        case ("end_map"):
                            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_CHROMEND_COLUMN, userParam.getValue());
                            break;
                        case ("strand"):
                            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_STRAND_COLUMN, userParam.getValue());
                            break;
                        default:
                            break;
                    }
                }
                for (CvParam cvParam : oldPSM.getSpectrumIdentification().getCvParams()) {
                    if (cvParam.getAccession().equalsIgnoreCase("MS:1002356")) {
                        psm.setOptionColumnValue(MzTabUtils.OPTIONAL_PSM_FDRSCORE_COLUMN, cvParam.getValue());
                        break;
                    }
                }
            }

            psmList.add(psm);
        }

        //Load the modifications in case some of modifications are not reported in the SpectrumIdentificationProtocol
//        int varId = 1;
//        for(Param param: variableModifications.keySet()){
//            String siteString = "";
//            for(String site: variableModifications.get(param)){
//                siteString=siteString+" "+ site;
//            }
//            siteString = siteString.trim();
//            metadata.addVariableModParam(varId, param);
//            metadata.addVariableModSite(varId, siteString);
//
//            varId++;
//        }
        return psmList;
    }


    protected Protein loadProtein(uk.ac.ebi.pride.utilities.data.core.Protein msProtein, List<Peptide> peptides)  {


        DBSequence sequence = msProtein.getDbSequence();

        // create the protein object
        Protein protein = new Protein(proteinColumnFactory);
        protein.setAccession(removeNewLineAndTab(generateAccession(msProtein)));
        protein.setDatabase(getDatabaseName(sequence.getSearchDataBase().getNameDatabase().getCvParams(), sequence.getSearchDataBase().getNameDatabase().getUserParams()));
        String version = (sequence.getSearchDataBase().getVersion() != null && !sequence.getSearchDataBase().getVersion().isEmpty()) ? sequence.getSearchDataBase().getVersion() : null;
        protein.setDatabaseVersion(version);

        // set the description if available
        String description = (getDescriptionFromCVParams(sequence.getCvParams()) != null && !getDescriptionFromCVParams(sequence.getCvParams()).isEmpty()) ? getDescriptionFromCVParams(sequence.getCvParams()) : null;
        protein.setDescription(description);
        //Todo: check here what we need to do
        if (sequence.getSequence() != null && !sequence.getSequence().isEmpty()) {
            protein.setOptionColumnValue(MZIdentMLUtils.OPTIONAL_SEQUENCE_COLUMN, sequence.getSequence());
        } else
            protein.setOptionColumnValue(MZIdentMLUtils.OPTIONAL_SEQUENCE_COLUMN, "null");

        //Todo: MzIdentml the samples are completely disconnected from proteins and peptides.
        // set protein species and taxid. We are not sure about the origin of the protein. So we keep this value as
        // null to avoid discrepancies

        Map<Integer, Integer> totalPSM = new HashMap<Integer, Integer>();
        Set<Integer> msRunForProtein = new HashSet<Integer>();

        Set<SearchEngineParam> searchEngines = new HashSet<SearchEngineParam>();
        List<SearchEngineParam> searchEngineParams;

        //TODO: Review
        for (int index = 0; index < peptides.size(); index++) {
            Comparable ref = source.getPeptideSpectrumId(msProtein.getId(), index);
            if (ref != null)
                ref = ref.toString().split("!")[1];
            if (spectraToRun.containsKey(ref)) {
                Integer value = 1;
                if (totalPSM.containsKey(spectraToRun.get(ref))) {
                    value = totalPSM.get(spectraToRun.get(ref)) + 1;
                }
                msRunForProtein.add(spectraToRun.get(ref));
                totalPSM.put(spectraToRun.get(ref), value);
            }
            searchEngineParams = MzIdentMLUtils.getSearchEngineCvTermReferences(peptides.get(index).getSpectrumIdentification().getCvParams());
            searchEngines.addAll(searchEngineParams);
        }

        for (Integer msRunId : totalPSM.keySet())
            protein.setNumPSMs(metadata.getMsRunMap().get(msRunId), totalPSM.get(msRunId));

        //Set Search Engine
        for (SearchEngineParam searchEngineParam : searchEngines)
            protein.addSearchEngineParam(searchEngineParam.getParam());

        //Scores for Proteins


        // set the modifications
        // is not necessary check by ambiguous modifications because they are not supported
        // the actualization of the metadata with fixed and variable modifications is done in the metadata section
        loadModifications(protein, peptides);

        if(sequence.getSequence() != null && !sequence.getSequence().isEmpty())
            protein.setOptionColumnValue(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN, sequence.getSequence());

        // check and set additional chromosome information
        if (hasChromInformation()) {
            String proteinName;
            if (sequence.getAccession().startsWith("generic|")) {
                proteinName = StringUtils.substringBetween(sequence.getAccession(), "|");
                if (proteinName.startsWith("A_")) {
                    proteinName = proteinName.substring(2);
                }
            } else {
                proteinName = "null";
            }
            protein.setOptionColumnValue(MzTabUtils.OPTIONAL_PROTEIN_NAME_COLUMN, proteinName);
        }

        return protein;

    }

    private void loadModifications(PSM psm, PeptideEvidence peptideEvidence) {

        //TODO simplify
        Set<Modification> modifications = new TreeSet<Modification>(new Comparator<Modification>() {
            @Override
            public int compare(Modification o1, Modification o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });

        for (uk.ac.ebi.pride.utilities.data.core.Modification ptm : peptideEvidence.getPeptideSequence().getModifications()) {
            // ignore modifications that can't be processed correctly (can not be mapped to the protein)

            if (ptm.getCvParams().get(0).getAccession() == null) {
                continue;
            }

            // mod without position
            Modification mod = MZTabUtils.parseModification(Section.PSM, ptm.getCvParams().get(0).getAccession());

            if (mod != null) {
                // only biological significant modifications are propagated to the protein
                if (peptideEvidence.getStartPosition() != null && peptideEvidence.getStartPosition() >= 0 && ptm.getLocation() >= 0) {
                    Integer position = peptideEvidence.getStartPosition() + ptm.getLocation();
                    mod.addPosition(position, null);
                }
                //if position is not set null is reported

                modifications.add(mod);
            }
        }

        //We add to the protein not duplicated modifications
        for (Modification modification : modifications) {
            psm.addModification(modification);
        }
    }

    protected void loadModifications(Protein protein, List<uk.ac.ebi.pride.utilities.data.core.Peptide> items) {

        //TODO simplify
        Set<Modification> modifications = new TreeSet<Modification>(new Comparator<Modification>() {
            @Override
            public int compare(Modification o1, Modification o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });

        for (uk.ac.ebi.pride.utilities.data.core.Peptide item : items) {
            PeptideEvidence peptideEvidence = null;
            for(PeptideEvidence peptideEvidenceRef: item.getPeptideEvidenceList()){
                if(peptideEvidenceRef.getDbSequence().getAccession().equalsIgnoreCase(protein.getAccession())){
                    peptideEvidence = peptideEvidenceRef;
                    break;
                }
            }

            int seqLength = 0;
            if(item.getSequence() != null){
                seqLength = item.getPeptideSequence().length();
            }

            for (uk.ac.ebi.pride.utilities.data.core.Modification ptm : item.getPeptideSequence().getModifications()) {
                // ignore modifications that can't be processed correctly (can not be mapped to the protein)
                if (ptm.getCvParams().get(0).getAccession() == null) {
                    continue;
                }

                // mod without position
                Modification mod = MZTabUtils.parseModification(Section.Protein, ptm.getCvParams().get(0).getAccession());

                if (mod != null) {

                    // only biological significant modifications are propagated to the protein
                    if (ModParam.isBiological(ptm.getCvParams().get(0).getAccession())) {
                        // if we can calculate the position, we add it to the modification
                        // -1 to calculate properly the modification offset
                        if (peptideEvidence != null && peptideEvidence.getStartPosition() != null && peptideEvidence.getStartPosition() >= 0 && ptm.getLocation() >= 0) {
                            Integer position = peptideEvidence.getStartPosition() + ptm.getLocation() -1 ;
                            mod.addPosition(position, null);
                            if (ptm.getLocation() > 0 && ptm.getLocation() < (seqLength + 1)) {
                                mod.addPosition(position, null);
                                modifications.add(mod);
                            } else if (position == 0) { //n-term for protein
                                mod.addPosition(position, null);
                                modifications.add(mod);
                            }

                        } else  {
                            //if position is not set null is reported
                            modifications.add(mod);
                        }
                        // the metadata is updated in the PSM section because the protein modifications are a subset of
                        // the psm modifications
                    }
                }
            }
        }
        //We add to the protein not duplicated modifications
        for (Modification modification : modifications) {
            protein.addModification(modification);
        }
    }

    protected String getDescriptionFromCVParams(List<CvParam> cvParams) {
        for(CvParam cvparam: cvParams){
            if(cvparam.getAccession().equalsIgnoreCase(CvTermReference.MS_PROTEIN_DESCRIPTION.getAccession())){
                return cvparam.getValue();
            }
        }
        return null;
    }

    protected String getDatabaseName(List<CvParam> databaseName, List<uk.ac.ebi.pride.utilities.data.core.UserParam> userParam){
        if(databaseName != null && databaseName.size() != 0)
            return (databaseName.get(0).getValue()!=null)? databaseName.get(0).getValue():databaseName.get(0).getName();
        else if(userParam != null){
            return (userParam.get(0).getValue()!=null)?userParam.get(0).getValue():userParam.get(0).getName();
        }
        return null;
    }

//    private List<uk.ac.ebi.pride.utilities.data.core.Peptide> getScannedSpectrumIdentificationItems(Comparable proteinId) throws JAXBException {
//        Collection<Comparable> ids =  source.getPeptideIds(proteinId);
//        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = null;
//        if(ids != null && ids.size() > 0){
//            peptides = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Peptide>(ids.size());
//            for(Comparable id: ids)
//                peptides.add(source.getPeptideByIndex(proteinId, id));
//        }
//        return peptides;
//    }


//    private List<uk.ac.ebi.pride.utilities.data.core.Peptide> getScannedSpectrumIdentificationItems(uk.ac.ebi.pride.utilities.data.core.Protein protein){
//        return protein.getPeptides();
//    }

    public static boolean isSupported(DataAccessController controller){
        return ((controller.getType() == DataAccessController.Type.MZIDENTML ||
                controller.getType() == DataAccessController.Type.MZTAB ||
                controller.getType() == DataAccessController.Type.XML_FILE) && controller.hasProtein());
    }

    /**
     *  Returns the converted metadata, now including a check for chromosome information present, and if so,
     *  is set as a user param entry.
     *
     * @return  the Metadata result.
     */
    @Override
    protected Metadata convertMetadata() {
        Metadata result = super.convertMetadata();
        Collection<Comparable> proteinIds = source.getProteinIds();
        chromSearch: {
            for (Comparable proteinId : proteinIds) {
                Collection<Comparable> peptideIDs = source.getPeptideIds(proteinId);
                for (Comparable peptideID : peptideIDs) {
                    Collection<PeptideEvidence> peptideEvidences = source.getPeptideEvidences(proteinId, peptideID);
                    for (PeptideEvidence peptideEvidence : peptideEvidences) {
                        List<UserParam> userParams = peptideEvidence.getUserParams();
                        for (UserParam userParam : userParams) {
                            if (userParam.getName().equalsIgnoreCase("chr")) {
                                result.addCustom(new uk.ac.ebi.pride.jmztab.model.UserParam(MzTabUtils.CUSTOM_CHROM_INF_PARAM, "true"));
                                break chromSearch;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     *  A check to see if chromosome information is present, flagged by a user param in the metadata.
     *
     * @return  true if chromosome information is present, false otherwise.
     */
    private boolean hasChromInformation() {
        boolean result = false;
        for (Param param : metadata.getCustomList()) {
            if (param.getName().equalsIgnoreCase(MzTabUtils.CUSTOM_CHROM_INF_PARAM)) {
                result = param.getValue().equalsIgnoreCase("true");
                break;
            }
        }
        return result;
    }

}

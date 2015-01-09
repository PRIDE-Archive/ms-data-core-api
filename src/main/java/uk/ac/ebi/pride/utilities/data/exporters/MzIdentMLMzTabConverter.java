package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.DBSequence;
import uk.ac.ebi.pride.utilities.data.core.Enzyme;
import uk.ac.ebi.pride.utilities.data.core.Filter;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.PeptideEvidence;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentificationProtocol;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.utils.convert.ModParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineParam;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author ypriverol
 */
public class MzIdentMLMzTabConverter extends AbstractMzTabConverter{

    protected static Logger logger = Logger.getLogger(AbstractMzTabConverter.class);

    protected Map<Comparable, Integer> spectraToRun;

    private final static Integer THRESHOLD_LOOP_FOR_SCORE = 100;

    protected Map<Param, Set<String>> variableModifications;

    private Set<Comparable> proteinIds;

    /**
     * Default constructor
     * @param controller The DataAccessController to be Converted to MzTab
     */
    public MzIdentMLMzTabConverter(DataAccessController controller) {
        super(controller);
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

        return this.psmColumnFactory;
    }

    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected void fillData() {
        // Get a list of Identification ids
        proteinIds = new HashSet<Comparable>();

        try{
            if(!source.hasProteinAmbiguityGroup()){
                Collection<Comparable> proteinIds = source.getProteinIds();
                //Iterate over proteins
                for (Comparable id : proteinIds) {
                    Protein identification = getProteinById(id);
                    proteins.add(identification);
                    psms.addAll(loadPSMs(id));
                }
            }else{
                Collection<Comparable> proteinGroupIds = source.getProteinAmbiguityGroupIds();
                for(Comparable proteinGroupId: proteinGroupIds){
                    Protein identification = getProteinGroupById(proteinGroupId);
                    proteins.add(identification);
                    psms.addAll(loadPSMs(source.getProteinAmbiguityGroupById(proteinGroupId).getProteinIds()));
                }
            }
        }catch(JAXBException e){
            throw new DataAccessException("Error try to retrieve the information for own Protein");

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

    private Protein getProteinGroupById(Comparable proteinGroupId) throws JAXBException {
        ProteinGroup proteinAmbiguityGroup = source.getProteinAmbiguityGroupById(proteinGroupId);
        //Todo: We will annotated only the first protein, the core protein.
        uk.ac.ebi.pride.utilities.data.core.Protein firstProteinDetectionHypothesis = proteinAmbiguityGroup.getProteinDetectionHypothesis().get(0);
        if (proteinIds.contains(firstProteinDetectionHypothesis.getDbSequence().getAccession()))
            throw new DataAccessException("mzTab do not support one protein in more than one ambiguity groups.");
        else
            proteinIds.add(firstProteinDetectionHypothesis.getDbSequence().getAccession());

        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = getScannedSpectrumIdentificationItems(firstProteinDetectionHypothesis);
        Protein protein = loadProtein(firstProteinDetectionHypothesis.getId());
        String membersString = "";
        for(int i=1; i < proteinAmbiguityGroup.getProteinDetectionHypothesis().size();i++)
            membersString = proteinAmbiguityGroup.getProteinDetectionHypothesis().get(i).getDbSequence().getAccession() + ",";
        membersString = (membersString.isEmpty())?membersString:membersString.substring(0,membersString.length()-1);
        protein.addAmbiguityMembers(membersString);

        //Loop for spectrum to get all the ms_run to repeat the score at protein level
        Set<MsRun> msRuns = new HashSet<MsRun>();
        for(int index = 0; index < peptides.size(); index++){
            Comparable id = source.getPeptideSpectrumId(firstProteinDetectionHypothesis.getId(), index);
            if(id != null){
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


    private String getFileNameWithoutExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return fileName.substring(0, lastIndexOfDot);
    }

    @Override
    protected void loadSoftware(){

        List<Software> softwareList = source.getExperimentMetaData().getSoftwares();
        Protocol proteinDetectionProtocol = source.getIdentificationMetaData().getProteinDetectionProtocol();
        List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList = source.getIdentificationMetaData().getSpectrumIdentificationProtocols();

        if(!softwareList.isEmpty()){

            for(int i = 0; i < softwareList.size(); i++){
                CvParam nameCVparam = softwareList.get(i).getCvParams().get(0);
                if(nameCVparam!=null){

                    String version = (softwareList.get(i).getVersion() != null && !softwareList.get(i).getVersion().isEmpty())? softwareList.get(i).getVersion():"";
                    CVParam nameCV = MzTabUtils.convertCvParamToCVParam(nameCVparam);
                    metadata.addSoftwareParam(i+1, nameCV);
                    if(proteinDetectionProtocol != null && proteinDetectionProtocol.getAnalysisSoftware() != null &&
                            proteinDetectionProtocol.getAnalysisSoftware().getId().equals(softwareList.get(i).getId())){
                        if(proteinDetectionProtocol.getThreshold() != null){
                            loadCvParamSettings(i+1, proteinDetectionProtocol.getThreshold());

                            //Add FDR at Protein level if is annotated
                            for(CvParam cvParam: proteinDetectionProtocol.getThreshold().getCvParams())
                                if(CvTermReference.MS_GLOBAL_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                        CvTermReference.MS_LOCAL_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                        CvTermReference.MS_FDR_PROTEIN.getAccession().equalsIgnoreCase(cvParam.getAccession()))
                                    metadata.addFalseDiscoveryRateParam(MzTabUtils.convertCvParamToCVParam(cvParam));
                        }
                        if(proteinDetectionProtocol.getAnalysisParam() != null){
                            loadCvParamSettings(i+1, proteinDetectionProtocol.getAnalysisParam());
                        }
                    }

                    for(SpectrumIdentificationProtocol spectrumIdentificationProtocol: spectrumIdentificationProtocolList){
                        if(spectrumIdentificationProtocol.getAnalysisSoftware().getId().equals(softwareList.get(i).getId())){
                            if(spectrumIdentificationProtocol.getThreshold() != null){
                                loadCvParamSettings(i+1, spectrumIdentificationProtocol.getThreshold());
                                //Add FDR at PSM level if is annotated
                                for(CvParam cvParam: spectrumIdentificationProtocol.getThreshold().getCvParams())
                                    if(CvTermReference.MS_GLOBAL_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                            CvTermReference.MS_LOCAL_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()) ||
                                            CvTermReference.MS_FDR_PSM.getAccession().equalsIgnoreCase(cvParam.getAccession()))
                                        metadata.addFalseDiscoveryRateParam(MzTabUtils.convertCvParamToCVParam(cvParam));
                            }
                            if(spectrumIdentificationProtocol != null){
                                loadCvParamSettings(i+1, spectrumIdentificationProtocol);
                            }
                            if(spectrumIdentificationProtocol.getFragmentTolerance() != null){
                                loadCvParamListSettings(i + 1, spectrumIdentificationProtocol.getFragmentTolerance());
                            }
                            if(spectrumIdentificationProtocol.getParentTolerance() != null){
                                loadCvParamListSettings(i+1, spectrumIdentificationProtocol.getParentTolerance());
                            }
                            //Todo: See if we need to capture other objects from fragmentation table, etc.
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
            metadata.addSoftwareSetting(order, userParam.getName() + " = " + userParam.getValue());
        }
    }

    /**
     * Isert in metadata only the CvTerm List Settings in an specific order
     * @param order order of the Param
     * @param paramList Param List
     */
    private void loadCvParamListSettings(int order, List<CvParam> paramList){
        for (CvParam cvParam: paramList){
            metadata.addSoftwareSetting(order, cvParam.getName() + " = " + cvParam.getValue());
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
        metadata.setDescription(description);
    }

    @Override
    protected void loadSampleProcessing() {

    }


    private void loadURI() {
        String expAccession = source.getName();
        if (expAccession == null || expAccession.isEmpty()) {
            return;
        }
        expAccession = expAccession.replaceAll("\\s+","-");
        try {
            URI uri = new URI("http://www.ebi.ac.uk/pride/archive/assays/" + expAccession);
            metadata.addUri(uri);
        } catch (URISyntaxException e) {
            throw new DataAccessException("Error while building URI at the metadata section", e);
        }
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
                        metadata.addSampleSpecies(specieId++, MzTabUtils.convertCvParamToCVParam(cv));
                    } else if ("BTO".equals(cv.getCvLookupID())) {
                        metadata.addSampleTissue(tissueId++, MzTabUtils.convertCvParamToCVParam(cv));
                        tissueId++;
                    } else if ("CL".equals(cv.getCvLookupID())) {
                        metadata.addSampleCellType(cellTypeId++, MzTabUtils.convertCvParamToCVParam(cv));
                    } else if ("DOID".equals(cv.getCvLookupID()) || "IDO".equals(cv.getCvLookupID())) {
                        metadata.addSampleDisease(diseaseId++, MzTabUtils.convertCvParamToCVParam(cv));
                    }
                }
                metadata.addSampleDescription(idSample++, sample.getName());
            }
        }
    }

    @Override
    protected void loadGelData() {

    }

    protected List<PSM> loadPSMs(List<Comparable> ids) throws JAXBException {
        List<PSM> psmList = new ArrayList<PSM>();
        for(Comparable id: ids)
            psmList.addAll(loadPSMs(id));
        return psmList;
    }

    /**
     * Converts the passed Identification object into an MzTab PSM.
     */
    protected List<PSM> loadPSMs(Comparable id) throws JAXBException {

        List<Peptide> peptides = source.getProteinById(id).getPeptides();
        Map<Comparable, Integer> indexSpectrumID = new HashMap<Comparable, Integer>();
        List<PSM> psmList = new ArrayList<PSM>();
        variableModifications = new HashMap<Param, Set<String>>();

        for (int index = 0; index < peptides.size(); index++) {
            Peptide oldPSM = peptides.get(index);
            PSM psm = new PSM(psmColumnFactory, metadata);
            psm.setSequence(oldPSM.getPeptideSequence().getSequence());
            psm.setPSM_ID(oldPSM.getSpectrumIdentification().getId().toString());
            psm.setAccession(oldPSM.getPeptideEvidence().getDbSequence().getAccession());
            psm.setDatabase(getDatabaseName(oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getNameDatabase().getCvParams(),oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getNameDatabase().getUserParams()));
            String version = (oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getVersion() != null && !oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getVersion().isEmpty())?oldPSM.getPeptideEvidence().getDbSequence().getSearchDataBase().getVersion():null;
            psm.setDatabaseVersion(version);

            psm.setStart(oldPSM.getPeptideEvidence().getStartPosition());
            psm.setEnd(oldPSM.getPeptideEvidence().getEndPosition());
            String pre  = String.valueOf(oldPSM.getPeptideEvidence().getPreResidue());
            String post = String.valueOf(oldPSM.getPeptideEvidence().getPostResidue());
            psm.setPre((pre == null || pre.isEmpty() || pre.equalsIgnoreCase(String.valueOf('\u0000')))?null:pre);
            psm.setPost((post == null || post.isEmpty() || pre.equalsIgnoreCase(String.valueOf('\u0000')))?null:post);


            List<Modification> mods = new ArrayList<Modification>();
            for(uk.ac.ebi.pride.utilities.data.core.Modification oldMod: oldPSM.getPeptideSequence().getModifications()){
                Modification mod = MZTabUtils.parseModification(Section.PSM, oldMod.getCvParams().get(0).getAccession());
                if(mod != null){
                    mod.addPosition(oldMod.getLocation(), null);
                    mods.add(mod);
                    String site;
                    if(oldMod.getLocation()-1 < 0)
                        site = "N-Term";
                    else if(oldPSM.getPeptideEvidence().getPeptideSequence().getSequence().length() <= oldMod.getLocation() -1)
                        site = "C-Term";
                    else
                        site = String.valueOf(oldPSM.getPeptideEvidence().getPeptideSequence().getSequence().charAt(oldMod.getLocation()-1));
                    Double mass = (oldMod.getMonoisotopicMassDelta() !=null && !oldMod.getMonoisotopicMassDelta().isEmpty())? oldMod.getMonoisotopicMassDelta().get(0):null;
                    Param param = MzTabUtils.convertCvParamToCVParam(oldMod.getCvParams().get(0), mass);

                    if(!variableModifications.containsKey(param) || !variableModifications.get(param).contains(site)){
                        Set<String> sites = new HashSet<String>();
                        sites = (variableModifications.containsKey(param.getAccession()))?variableModifications.get(param.getAccession()):sites;
                        sites.add(site);
                        variableModifications.put(param, sites);
                    }
                }else{
                    logger.warn("Your mzidentml contains an UNKNOWN modification which is not supported by mzTab format");
                }
                for(CvParam param: oldMod.getCvParams()) {
                    if(param.getAccession().equalsIgnoreCase(CvTermReference.MS_NEUTRAL_LOSS.getAccession())){
                        CVParam lost = MzTabUtils.convertCvParamToCVParam(param, 0.0);

                        Modification modNeutral = new Modification(Section.PSM,Modification.Type.NEUTRAL_LOSS, lost.getAccession());
                        modNeutral.setNeutralLoss(lost);
                        modNeutral.addPosition(oldMod.getLocation(), null);
                        mods.add(modNeutral);
                    }
                }
            }

            for(Modification mod: mods) psm.addModification(mod);
            psm.setExpMassToCharge(oldPSM.getSpectrumIdentification().getExperimentalMassToCharge());
            psm.setCharge(oldPSM.getSpectrumIdentification().getChargeState());
            psm.setCalcMassToCharge(oldPSM.getSpectrumIdentification().getCalculatedMassToCharge());
            Comparable idSpectrum = source.getPeptideSpectrumId(id,index);
            if(idSpectrum != null){
                String[] spectumMap = idSpectrum.toString().split("!");
                String spectrumReference = null;
                for(SpectraData spec: source.getExperimentMetaData().getSpectraDatas()){
                    if(spec.getId().toString().equalsIgnoreCase(spectumMap[1])){
                        spectrumReference = MzTabUtils.getOriginalSpectrumId(spec, spectumMap[0]);
                    }
                }
                if(spectumMap[1] != null && spectrumReference != null)
                    psm.addSpectraRef(new SpectraRef(metadata.getMsRunMap().get(spectraToRun.get(spectumMap[1])), spectrumReference));
            }
            psm.setStart(oldPSM.getPeptideEvidence().getStartPosition());
            psm.setEnd(oldPSM.getPeptideEvidence().getEndPosition());

            // See which psm scores are supported
            for(CvParam cvPAram: oldPSM.getSpectrumIdentification().getCvParams()){
                if(psmScoreToScoreIndex.containsKey(cvPAram.getAccession())){
                    CVParam param = MzTabUtils.convertCvParamToCVParam(cvPAram);
                    int idCount = psmScoreToScoreIndex.get(cvPAram.getAccession());
                    psm.setSearchEngineScore(idCount, param.getValue());
                }
            }
            //loadModifications(psm,peptideEvidenceRef.getPeptideEvidence());
            if(!indexSpectrumID.containsKey(oldPSM.getSpectrumIdentification().getId())){
                int indexMzTab = indexSpectrumID.size()+1;
                indexSpectrumID.put(oldPSM.getSpectrumIdentification().getId(), indexMzTab);
            }
            //Set Search Engine

            Set<SearchEngineParam> searchEngines = new HashSet<SearchEngineParam>();
            List<SearchEngineParam> searchEngineParams = MzIdentMLUtils.getSearchEngineTypes(oldPSM.getSpectrumIdentification().getCvParams());
            searchEngines.addAll(searchEngineParams);

            for(SearchEngineParam searchEngineParam: searchEngines)
                psm.addSearchEngineParam(searchEngineParam.getParam());

            //Set optional parameter

            psm.setPSM_ID(indexSpectrumID.get(oldPSM.getSpectrumIdentification().getId()));
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_ID_COLUMN, oldPSM.getSpectrumIdentification().getId());
            Boolean decoy = oldPSM.getPeptideEvidence().isDecoy();
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_DECOY_COLUMN, (!decoy)?0:1);
            psm.setOptionColumnValue(MzTabUtils.OPTIONAL_RANK_COLUMN, oldPSM.getSpectrumIdentification().getRank());
            psmList.add(psm);
        }

        //Load the modifications in case some of modifications are not reported in the SpectrumIdentificationProtocol
        int varId = variableModifications.size() + 1;
        for(Param param: variableModifications.keySet()){
            String siteString = "";
            for(String site: variableModifications.get(param)){
                siteString=siteString+" "+ site;
            }
            siteString = siteString.trim();
            metadata.addVariableModParam(varId, param);
            metadata.addVariableModSite(varId, siteString);

            varId++;
        }
        return psmList;
    }


    private Protein getProteinById(Comparable proteinId) throws JAXBException {
        return loadProtein(proteinId);
    }

    private Protein loadProtein(Comparable proteinId) throws JAXBException {
        DBSequence sequence = source.getProteinSequence(proteinId);
        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = getScannedSpectrumIdentificationItems(proteinId);
        // create the protein object
        Protein protein = new Protein(proteinColumnFactory);
        protein.setAccession(sequence.getAccession());
        protein.setDatabase(getDatabaseName(sequence.getSearchDataBase().getNameDatabase().getCvParams(), sequence.getSearchDataBase().getNameDatabase().getUserParams()));
        String version = (sequence.getSearchDataBase().getVersion() != null && !sequence.getSearchDataBase().getVersion().isEmpty())?sequence.getSearchDataBase().getVersion():null;
        protein.setDatabaseVersion(version);

        // set the description if available
        String description = (getDescriptionFromCVParams(sequence.getCvParams()) != null && !getDescriptionFromCVParams(sequence.getCvParams()).isEmpty())?getDescriptionFromCVParams(sequence.getCvParams()):null;
        protein.setDescription(description);

        //Todo: MzIdentml the samples are completely disconnected from proteins and peptides.
        // set protein species and taxid. We are not sure about the origin of the protein. So we keep this value as
        // null to avoid discrepancies

        Map<Integer, Integer> totalPSM = new HashMap<Integer, Integer>();
        Set<Integer> msRunforProtein = new HashSet<Integer>();

        for(int index = 0; index < peptides.size(); index++ ){
            Comparable ref = source.getPeptideSpectrumId(proteinId, index);
            if(ref != null)
                ref = ref.toString().split("!")[1];
            if(spectraToRun.containsKey(ref)){
                Integer value = 1;
                if(totalPSM.containsKey(spectraToRun.get(ref))){
                    value = totalPSM.get(spectraToRun.get(ref)) + 1;
                }
                msRunforProtein.add(spectraToRun.get(ref));
                totalPSM.put(spectraToRun.get(ref), value);
            }

        }

        //Scores for Proteins

        for(Integer msRunId: totalPSM.keySet())
            protein.setNumPSMs(metadata.getMsRunMap().get(msRunId), totalPSM.get(msRunId));

        //Set Search Engine
        Set<SearchEngineParam> searchEngines = new HashSet<SearchEngineParam>();
        for(int i=0; i < THRESHOLD_LOOP_FOR_SCORE && i < peptides.size(); i++){
            List<SearchEngineParam> searchEngineParams = MzIdentMLUtils.getSearchEngineTypes(peptides.get(i).getSpectrumIdentification().getCvParams());
            searchEngines.addAll(searchEngineParams);
        }
        for(SearchEngineParam searchEngineParam: searchEngines)
            protein.addSearchEngineParam(searchEngineParam.getParam());

        // set the modifications
        // is not necessary check by ambiguous modifications because are not supported in PRIDE XML
        // the actualization of the metadata with fixed and variable modifications is done in the peptide section
        loadModifications(protein, peptides);

        if(sequence.getSequence() != null && !sequence.getSequence().isEmpty())
            protein.setOptionColumnValue(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN, sequence.getSequence());


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
                if (peptideEvidence.getStartPosition() != null && ptm.getLocation() != -1) {
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

    private void loadModifications(Protein protein, List<uk.ac.ebi.pride.utilities.data.core.Peptide> items) {

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
                        if (peptideEvidence != null && peptideEvidence.getStartPosition() != null && ptm.getLocation() != -1) {
                            Integer position = peptideEvidence.getStartPosition() + ptm.getLocation();
                            mod.addPosition(position, null);

                        }
                        //if position is not set null is reported
                        modifications.add(mod);

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

    private String getDescriptionFromCVParams(List<CvParam> cvParams) {
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

    private List<uk.ac.ebi.pride.utilities.data.core.Peptide> getScannedSpectrumIdentificationItems(Comparable proteinId) throws JAXBException {
        Collection<Comparable> ids =  source.getPeptideIds(proteinId);
        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = null;
        if(ids != null && ids.size() > 0){
            peptides = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Peptide>(ids.size());
            for(Comparable id: ids)
                peptides.add(source.getPeptideByIndex(proteinId, id));
        }
        return peptides;
    }


    private List<uk.ac.ebi.pride.utilities.data.core.Peptide> getScannedSpectrumIdentificationItems(uk.ac.ebi.pride.utilities.data.core.Protein proteinDetectionHypothesis){
//        List<SpectrumIdentification> spectrumIdentIds = new ArrayList<SpectrumIdentification>();
        return proteinDetectionHypothesis.getPeptides();
//        for(Peptide peptideHypothesis: peptideHypothesises){
//            List<SpectrumIdentification> specRefs = peptideHypothesis.getSpectrumIdentificationItemRef();
//            for(SpectrumIdentification spectrumIdentification: specRefs)
//                spectrumIdentIds.add(spectrumIdentificationItemRef.getSpectrumIdentificationItem());
//        }
    }

    public static boolean isSupported(DataAccessController controller){
        return ((controller.getType() == DataAccessController.Type.MZIDENTML ||
                controller.getType() == DataAccessController.Type.MZTAB ||
                controller.getType() == DataAccessController.Type.XML_FILE) && controller.hasProtein());
    }
}

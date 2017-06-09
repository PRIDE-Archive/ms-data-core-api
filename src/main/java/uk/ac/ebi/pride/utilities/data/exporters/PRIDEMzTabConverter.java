package uk.ac.ebi.pride.utilities.data.exporters;

import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.convert.ModParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineScoreParam;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.CvUtilities;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.utilities.data.utils.PRIDEUtils;
import uk.ac.ebi.pride.utilities.exception.IllegalAminoAcidSequenceException;
import uk.ac.ebi.pride.utilities.mol.Element;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;
import uk.ac.ebi.pride.utilities.mol.NeutralLoss;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;
import uk.ac.ebi.pride.utilities.util.StringUtils;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.isEmpty;
import static uk.ac.ebi.pride.utilities.data.utils.MzTabUtils.removeNewLineAndTab;

/**
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class PRIDEMzTabConverter extends AbstractMzTabConverter {


    public static final String GEL_IDENTIFIER     = "gel_identifier";
    public static final String GEL_SPOTIDENTIFIER = "gel_spotidentifier";
    public static final String EMPAI              = "empai";
    public static final String GEL_COORDINATES    = "gel_coordinates";
    private boolean gelExperiment;

    private int alternativeId = 0;

    /**
     * Important: Some PRIDE XMls contains for one protein accession more than one protein identification, but mzTab do not allow to have this then
     * We decided to merge of all of them in one protein using the merge function. We should investigate if those proteins are different runs or different quantitation values.
     */
    private SortedMap<String, List<uk.ac.ebi.pride.jmztab.model.Protein>> accessionProteinMap = new TreeMap<String, List<uk.ac.ebi.pride.jmztab.model.Protein>>();

    public PRIDEMzTabConverter(DataAccessController controller) {
        super(controller);
    }

    @Override
    protected void loadGelData() {

        // Get a list of Identification ids
        Collection<Comparable> ids = source.getProteinIds();

        if (!ids.isEmpty()) {
            // Iterate over each identification
            for (Comparable id : ids) {
                Protein identification = source.getProteinById(id);
                if(identification.getGel() != null){
                    gelExperiment = true;
                    return;
                }
            }
        }
    }

    @Override
    protected void loadExperimentParams() {

        if (source.getExperimentMetaData().getAdditional() == null) {
            return;
        }

        for (CvParam p : source.getExperimentMetaData().getAdditional().getCvParams()) {
            if (CvTermReference.EXPERIMENT_DESCRIPTION.getAccession().equals(p.getAccession()) && !StringUtils.isEmpty(p.getValue())) {
                metadata.setDescription(removeNewLineAndTab(p.getValue()));
            } else if (QuantCvTermReference.isQuantitativeMethodParam(p.getAccession())) {
                // check if it's a quantification method
                metadata.setQuantificationMethod(MzTabUtils.convertCvParamToCVParam(p));
            } else if (CvTermReference.PRIDE_GEL_BASED_EXPERIMENT.getAccession().equals(p.getAccession())) {
                //If it a gel we add the optional columns for gel
                gelExperiment = true;
                metadata.addCustom(MzTabUtils.convertCvParamToCVParam(p));
            }
        }
    }

    @Override
    protected void loadSoftware() {
        List<Software> softwares = source.getExperimentMetaData().getSoftwares();
        int iSoftware = 1;
        for(Software software: softwares){
            // The name of the software is always the first CVTerm
            CvParam softwareCvName = software.getCvParams().get(0);
            metadata.addSoftwareParam(iSoftware, MzTabUtils.convertCvParamToCVParam(softwareCvName));
            iSoftware++;
        }
    }

    @Override
    protected void loadSamples() {
        //Todo: Review this funtion for Quantitation Information

        List<Sample> samples = source.getExperimentMetaData().getSamples();
        if (samples == null) {
            return;
        }

        for (Sample sample : samples) {
            // Quantification
            for (CvParam p : sample.getCvParams()) {
                // check for subsample descriptions
                if (QuantCvTermReference.SUBSAMPLE1_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(1, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE2_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(2, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE3_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(3, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE4_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(4, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE5_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(5, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE6_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(6, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE7_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(7, removeNewLineAndTab(p.getValue()));
                    continue;
                }
                if (QuantCvTermReference.SUBSAMPLE8_DESCRIPTION.getAccession().equals(p.getAccession())) {
                    metadata.addSampleDescription(8, removeNewLineAndTab(p.getValue()));
                    continue;
                }

                // check if it belongs to a sample
                if (!isEmpty(p.getValue()) && p.getValue().startsWith("subsample")) {
                    // get the subsample number
                    Pattern subsampleNumberPattern = Pattern.compile("subsample(\\d+)");
                    Matcher matcher = subsampleNumberPattern.matcher(p.getValue());

                    if (matcher.find()) {
                        Integer id = Integer.parseInt(matcher.group(1));

                        // add the param depending on the type
                        if ("NEWT".equals(p.getCvLookupID())) {
                            metadata.addSampleSpecies(id, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("BRENDA".equals(p.getCvLookupID())) {
                            metadata.addSampleTissue(id, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("CL".equals(p.getCvLookupID())) {
                            metadata.addSampleCellType(id, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("DOID".equals(p.getCvLookupID()) || "IDO".equals(p.getCvLookupID())) {
                            metadata.addSampleDisease(id, MzTabUtils.convertCvParamToCVParam(p));
                        } else if (QuantCvTermReference.isReagent(p.getAccession())) {
                            metadata.addAssayQuantificationReagent(id, MzTabUtils.convertCvParamToCVParam(p));
                        } else {
                            metadata.addSampleCustom(id, MzTabUtils.convertCvParamToCVParam(p));
                        }
                    }
                }
            }

            // Identification
            if (metadata.getSampleMap().isEmpty()) {
                for (CvParam p : sample.getCvParams()) {
                    if (!isEmpty(p.getCvLookupID())) {
                        if ("NEWT".equals(p.getCvLookupID())) {
                            metadata.addSampleSpecies(1, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("BTO".equals(p.getCvLookupID())) {
                            metadata.addSampleTissue(1, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("CL".equals(p.getCvLookupID())) {
                            metadata.addSampleCellType(1, MzTabUtils.convertCvParamToCVParam(p));
                        } else if ("DOID".equals(p.getCvLookupID()) || "IDO".equals(p.getCvLookupID())) {
                            //DOID: Human Disease Ontology
                            //IDO: Infectious Disease Ontology
                            metadata.addSampleDisease(1, MzTabUtils.convertCvParamToCVParam(p));
                        } else if (!isEmpty(p.getName())) {
                            metadata.addSampleCustom(1, MzTabUtils.convertCvParamToCVParam(p));
                        }
                    }
                    if (sample.getName() != null && !sample.getName().isEmpty()) {
                        metadata.addSampleDescription(1, sample.getName());
                    }
                }

            }


            // setting custom parameter for identification.
            if (metadata.getSampleMap().size() <= 1) {
                for (UserParam userParam : sample.getUserParams()) {
                    metadata.addSampleCustom(1, MzTabUtils.convertUserParamToCVParam(userParam));
                }
            }

            // create relationships between ms_run, samples, and assays
            if (metadata.getSampleMap().size() == 1) {
                // Identification
                metadata.addAssaySample(1, metadata.getSampleMap().get(1));
                metadata.addAssayMsRun(1, metadata.getMsRunMap().get(1));
            } else {
                for (Assay assay : metadata.getAssayMap().values()) {
                    assay.setSample(metadata.getSampleMap().get(assay.getId()));
                    assay.setMsRun(metadata.getMsRunMap().get(1));
                }
            }

        }
    }

    @Override
    protected void loadSampleProcessing() {
        ExperimentProtocol protocol  = source.getExperimentMetaData().getProtocol();
        if (protocol == null) {
            return;
        }
        int i = 1;
        if (protocol.getProtocolSteps() != null) {
            for (ParamGroup param : protocol.getProtocolSteps()) {
                if (param != null && param.getCvParams() != null)
                    for (CvParam cvParam : param.getCvParams())
                        metadata.addSampleProcessingParam(i, MzTabUtils.convertCvParamToCVParam(cvParam));
                if (param != null && param.getUserParams() != null)
                    for (UserParam userParam : param.getUserParams())
                        metadata.addSampleProcessingParam(i, MzTabUtils.convertUserParamToCVParam(userParam));

                i++;
            }
        }
    }

    /**
     * Retrieve the data for instrument in and Instrument Processing (Analyzer, Source and Detector)
     */
    @Override
    protected void loadInstrument() {
        List<InstrumentConfiguration> instruments = source.getMzGraphMetaData().getInstrumentConfigurations();

        if (instruments == null) {
            return;
        }
        int iInstrument = 1;

        // MzTab doesn't support several detectors names or sources, only several analyzers,
        // so for now we can add only the first one
        for(InstrumentConfiguration instrument: instruments){
            for(CvParam cvParam: instrument.getCvParams()) {
                if (cvParam.getAccession().equalsIgnoreCase(CvTermReference.INSTRUMENT_MODEL.getAccession())) {
                    metadata.addInstrumentName(iInstrument, MzTabUtils.convertCvParamToCVParam(cvParam));
                    //We can store only one name so we exit the loop if we find it
                    break;
                }
            }

            //Retrieve all the data related with Source
            // for (InstrumentComponent instrumentComponent: instrument.getSource()){
            if ( !instrument.getSource().isEmpty() ){
                InstrumentComponent instrumentComponent = instrument.getSource().get(0);
                if(instrumentComponent.getCvParams()!= null && !instrumentComponent.getCvParams().isEmpty()){
                    CvParam cvParam = instrumentComponent.getCvParams().iterator().next();
                    metadata.addInstrumentSource(iInstrument, MzTabUtils.convertCvParamToCVParam(cvParam));
                } else {
                    if(instrumentComponent.getUserParams()!= null && !instrumentComponent.getUserParams().isEmpty()) {
                        UserParam userParam = instrumentComponent.getUserParams().iterator().next();
                        metadata.addInstrumentSource(iInstrument, MzTabUtils.convertUserParamToCVParam(userParam));
                    }
                }
            }
            //Retrieve all the data related with Analyzer
            for (InstrumentComponent instrumentComponent: instrument.getAnalyzer()){
                for(CvParam cvParam: instrumentComponent.getCvParams())
                    metadata.addInstrumentAnalyzer(iInstrument, MzTabUtils.convertCvParamToCVParam(cvParam));
                for(UserParam userParam: instrumentComponent.getUserParams())
                    metadata.addInstrumentAnalyzer(iInstrument, MzTabUtils.convertUserParamToCVParam(userParam));
            }

            //Retrieve all the data related with Analyzer
            // for (InstrumentComponent instrumentComponent: instrument.getDetector()){
            if (!instrument.getDetector().isEmpty()){
                InstrumentComponent instrumentComponent = instrument.getDetector().get(0);
                if(instrumentComponent.getCvParams()!= null && !instrumentComponent.getCvParams().isEmpty()){
                    CvParam cvParam = instrumentComponent.getCvParams().iterator().next();
                    metadata.addInstrumentDetector(iInstrument, MzTabUtils.convertCvParamToCVParam(cvParam));
                } else {
                    if(instrumentComponent.getUserParams()!= null && !instrumentComponent.getUserParams().isEmpty()) {
                        UserParam userParam = instrumentComponent.getUserParams().iterator().next();
                        metadata.addInstrumentDetector(iInstrument, MzTabUtils.convertUserParamToCVParam(userParam));
                    }
                }
            }
            iInstrument++;
        }
    }

    @Override
    protected void loadMsRun() {
        //Todo: Here we should specify a PRIDE XML CVTerm but it doesn't exist then we will use mzTab file.

        metadata.addMsRunFormat(1, MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_PSI_MZDATA_FILE, null)));
        metadata.addMsRunIdFormat(1, MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_SPEC_NATIVE_ID_FORMAT, null)));
        try {
            metadata.addMsRunLocation(1, new URL("file:/" + source.getName()));
        } catch (MalformedURLException e) {
            throw new DataAccessException("Error while adding ms run location", e);
        }
    }

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory} which maintain a couple of {@link uk.ac.ebi.pride.jmztab.model.ProteinColumn}
     */
    @Override
    protected MZTabColumnFactory convertProteinColumnFactory() {
        proteinColumnFactory = MZTabColumnFactory.getInstance(Section.Protein);
        this.proteinColumnFactory.addDefaultStableColumns();

        // If not provide protein_quantification_unit in metadata, default value is Ratio
        if (!isIdentification() && metadata.getProteinQuantificationUnit() == null) {
            metadata.setProteinQuantificationUnit(new CVParam("PRIDE", "PRIDE:0000395", "Ratio", null));
        }

        // ms_run[1] optional columns
        proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PSMS, metadata.getMsRunMap().get(1));
        proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_DISTINCT, metadata.getMsRunMap().get(1));
        proteinColumnFactory.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_UNIQUE, metadata.getMsRunMap().get(1));
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
            proteinColumnFactory.addSearchEngineScoreOptionalColumn(ProteinColumn.SEARCH_ENGINE_SCORE, id, metadata.getMsRunMap().get(1));
        }

        if(gelExperiment) {
            proteinColumnFactory.addOptionalColumn(GEL_SPOTIDENTIFIER, String.class);
            logger.debug("Optional column gel_spotidentifier added;");

            proteinColumnFactory.addOptionalColumn(GEL_IDENTIFIER, String.class);
            logger.debug("Optional column gel_identifier added;");

            proteinColumnFactory.addOptionalColumn(GEL_COORDINATES, String.class);
            logger.debug("Optional column gel_coordinates added;");
        }

        return proteinColumnFactory;
    }

    /**
     * Fill the data from the file, proteins and PSMs
     */
    @Override
    protected void fillData() {

        // Get a list of Identification ids
        Collection<Comparable> ids = source.getProteinIds();

        if (!ids.isEmpty()) {
            // Iterate over each identification
            for (Comparable id : ids) {
                Protein identification = source.getProteinById(id);

                uk.ac.ebi.pride.jmztab.model.Protein protein = loadProtein(identification);

                if (protein != null) {
                    // we create a check for duplicated proteins ids.
                    // If the protein is not null, it represents that have been
                    // already added and we need to merge the information with the original
                    List<uk.ac.ebi.pride.jmztab.model.Protein> proteinList = accessionProteinMap.get(protein.getAccession());
                    if (proteinList == null) {
                        proteinList = new ArrayList<uk.ac.ebi.pride.jmztab.model.Protein>();
                    }
                    proteinList.add(protein);
                    accessionProteinMap.put(protein.getAccession(), proteinList);

                }
                // convert psm
                // they don't have problems with duplications
                List<PSM> psmList = loadPSMs(identification);
                psms.addAll(psmList);
            }

            if (!accessionProteinMap.isEmpty()) {
                for (List<uk.ac.ebi.pride.jmztab.model.Protein> proteinList : accessionProteinMap.values()) {
                    uk.ac.ebi.pride.jmztab.model.Protein protein = merge(proteinList);
                    proteins.add(protein);
                }
            }
        } else {
            logger.warn("There is not Protein Identification information in the file.");
        }

        //This check can not be move to the metadata section
        //TODO: Move to the right place, it is a default checking (ConverterProvider)
        if (metadata.getFixedModMap().isEmpty()) {
            metadata.addFixedModParam(1, MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_SEARCH_PARAM_FIXED_MOD, null)));
            Comment comment = new Comment("Only variable modifications can be reported when the original source is a PRIDE XML file");
            getMZTabFile().addComment(getMZTabFile().getComments().size() + 1, comment);
        }

        //TODO: Move to the right place, it is a default checking (ConverterProvider)
        if (metadata.getVariableModMap().isEmpty()) {
            metadata.addVariableModParam(1, MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_SEARCH_PARAM_VAR_MOD, null)));
        }

    }

    /**
     * Converts the passed Identification object into an MzTab protein.
     * @param identification Protein Identification
     * @return  MzTab Protein Object
     */
    private uk.ac.ebi.pride.jmztab.model.Protein loadProtein(Protein identification) {

        // create the protein object
        uk.ac.ebi.pride.jmztab.model.Protein protein = new uk.ac.ebi.pride.jmztab.model.Protein(proteinColumnFactory);
        CVParam decoy = MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.PRIDE_DECOY_HIT,null));

        protein.setAccession(removeNewLineAndTab(generateAccession(identification)));
        protein.setDatabase(identification.getDbSequence().getSearchDataBase().getName());
        String version = (identification.getDbSequence().getSearchDataBase().getVersion() != null && ! identification.getDbSequence().getSearchDataBase().getVersion().isEmpty())?identification.getDbSequence().getSearchDataBase().getVersion():null;
        protein.setDatabaseVersion(version);

        // We mark the decoy hits
        // The optional column was added previously
        if (CvUtilities.isDecoyHit(identification) || CvUtilities.isAccessionDecoy(identification)) {
            String header = CVParamOptionColumn.getHeader(null, decoy);
            MZTabColumn column = proteinColumnFactory.findColumnByHeader(header);
            if (column != null) {
                protein.setOptionColumnValue(decoy, MZBoolean.True);
            } else {
                proteinColumnFactory.addOptionalColumn(decoy, MZBoolean.class);
                protein.setOptionColumnValue(decoy, MZBoolean.True);
                logger.debug("The protein decoy column has been added.");
            }
        } else {
            String header = CVParamOptionColumn.getHeader(null, decoy);
            MZTabColumn column = proteinColumnFactory.findColumnByHeader(header);
            if (column != null) {
                protein.setOptionColumnValue(decoy, MZBoolean.False);
            } else {
                proteinColumnFactory.addOptionalColumn(decoy, MZBoolean.class);
                protein.setOptionColumnValue(decoy, MZBoolean.False);
                logger.debug("The protein decoy column has been added.");
            }
        }

        loadSearchEngineScore(protein, identification);

        //TODO protein, species and taxid
        // set protein species and taxid. We are not sure about the origin of the protein. So we keep this value as
        // null to avoid discrepancies, we don't copy from metadata
//        if (!metadata.getSampleMap().isEmpty()) {
//            Sample sample = metadata.getSampleMap().get(1);
//            if (!sample.getSpeciesList().isEmpty()) {
//                Param speciesParam = sample.getSpeciesList().get(0);
//                protein.setSpecies(speciesParam.getName());
//                protein.setTaxid(speciesParam.getAccession());
//            }
//        }

        /* Todo: We should implement the following metrics for ms-data-core-api:
         *      - Todo: Number of peptides.
         *      - Todo: Number of modified peptides
         *      - Todo: Number unique peptides
         *      - Todo: All of them by MS_Run
         *
         *      - Todo: Number of PSMs.
         *      - Todo: Number of modified PSMs
         *      - Todo: Number unique PSMs
         *      - Todo: All of them by MS_Run
         *
         *  Right now are only for the conversion
        */
        // get the number of psms and distinct peptides
        List<Peptide> items = identification.getPeptides();
        List<String> allPeptideList = new ArrayList<String>();
        // sequence + modifications + charge
        HashSet<String> distinctPeptideList = new HashSet<String>();

        for (Peptide item : items) {
            List<Modification> modList = item.getModifications();
            StringBuilder sb = new StringBuilder();
            for (Modification mod : modList) {
                sb.append(mod.getMonoisotopicMassDelta()).append(mod.getLocation());
            }
            sb.append(item.getSequence());
            sb.append(item.getPrecursorCharge());

            distinctPeptideList.add(sb.toString());
            allPeptideList.add(item.getSequence());
        }

        protein.setNumPSMs(metadata.getMsRunMap().get(1), allPeptideList.size());
        protein.setNumPeptidesDistinct(metadata.getMsRunMap().get(1), distinctPeptideList.size());


//        protein.setNumPeptidesUnique();

        // set the modifications
        // is not necessary check by ambiguous modifications because are not supported in PRIDEXML
        // the actualization of the metadata with fixed and variable modifications is done in the peptide section
        loadModifications(protein, items);

        // protein coverage
        if(identification.getSequenceCoverage()>=0) {
            protein.setProteinConverage(identification.getSequenceCoverage());
        }

        //gel location
        if(identification.getGel() != null) {
            String coordinates = "[" + identification.getGel().getXCoordinate() +
                    ", " + identification.getGel().getYCoordinate() + "]";
            MzTabUtils.addOptionalColumnValue(protein, proteinColumnFactory, GEL_COORDINATES, coordinates);

        }


        // process the additional params
        if (identification.getCvParams() != null) {
            for (CvParam p : identification.getCvParams()) {
                // check if there's a quant unit set
                if (!isIdentification() && (QuantCvTermReference.UNIT_RATIO.getAccession().equals(p.getAccession()) || QuantCvTermReference.UNIT_COPIES_PER_CELL.getAccession().equals(p.getAccession()))) {
                    CVParam param = MzTabUtils.convertCvParamToCVParam(p);
                    if (param != null && metadata.getProteinQuantificationUnit() == null) {
                        metadata.setProteinQuantificationUnit(param);
                    }
                }
                // Quantification values
                else if (QuantCvTermReference.INTENSITY_SUBSAMPLE1.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(1), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE2.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(2), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE3.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(3), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE4.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(4), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE5.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(5), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE6.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(6), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE7.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(7), removeNewLineAndTab(p.getValue()));
                } else if (QuantCvTermReference.INTENSITY_SUBSAMPLE8.getAccession().equalsIgnoreCase(p.getAccession())) {
                    protein.setAbundanceColumnValue(metadata.getAssayMap().get(8), removeNewLineAndTab(p.getValue()));
                } else {
                    // check optional column.
                    if (QuantCvTermReference.EMPAI_VALUE.getAccession().equals(p.getAccession())) {
                        MzTabUtils.addOptionalColumnValue(protein, proteinColumnFactory, EMPAI, p.getValue());
                    } else if (CvTermReference.PRIDE_GEL_SPOT_IDENTIFIER.getAccession().equals(p.getAccession())) {
                        // check if there's gel spot identifier
                        MzTabUtils.addOptionalColumnValue(protein, proteinColumnFactory, GEL_SPOTIDENTIFIER, p.getValue());
                    } else if (CvTermReference.PRIDE_GEL_IDENTIFIER.getAccession().equals(p.getAccession())) {
                        // check if there's gel identifier
                        MzTabUtils.addOptionalColumnValue(protein, proteinColumnFactory, GEL_IDENTIFIER, p.getValue());
                    }
                }
            }

            // set the description if available
            String description = CvUtilities.getValueFromParmGroup(identification.getCvParams(), CvTermReference.PRIDE_PROTEIN_NAME.getAccession());
            if(description!= null && !description.isEmpty()) {
                protein.setDescription(description.trim());
            }

            // add the indistinguishable accessions to the ambiguity members
            // for now we are not going to look for these cases because for some of them they are annotated as synonyms instead of ambiguity members of the inference problem,
            // PRIDEXML conversion doesn't support ambiguity members
//            List<String> ambiguityMembers = getAmbiguityMembers(identification, CvTermReference.PRIDE_INDISTINGUISHABLE_ACCESSION.getAccession());
//            for (String member : ambiguityMembers) {
//                protein.addAmbiguityMembers(member);
//            }

        }
        return protein;

    }

    private void loadSearchEngineScore(uk.ac.ebi.pride.jmztab.model.Protein protein, Protein identification) {

        //SearchEngineScoreParam psm_searchEngineScoreParam;
        Score score = identification.getScore();
        Integer id;

        if (score != null) {
            //CVParam for the metadata section
            //searchEngineScoreParam = SearchEngineScoreParam.getSearchEngineScoreParamByName(searchEngineName);
            List<SearchEngineScoreParam> searchEngineScoreParam = MzTabUtils.getSearchEngineScoreTerm(identification.getScore());

            if (searchEngineScoreParam != null && searchEngineScoreParam.size()>0) {
                id = -1;
                for(SearchEngineScoreParam searchparam: searchEngineScoreParam){
                    CVParam scoreParam = searchparam.getParam(null);
                    Number scoreValue = score.getValueBySearchEngineScoreTerm(scoreParam.getAccession());
                    for (Map.Entry<Integer, ProteinSearchEngineScore> entry : metadata.getProteinSearchEngineScoreMap().entrySet()) {
                        if (entry.getValue().getParam().equals(scoreParam)) {
                            id = entry.getKey();
                            break;
                        }
                    }
                    if (id <= 0) { //if the search engine score is not in the metadata we have a problem
                        logger.warn("The search engine score value can not be converted because the search engine score is not defined in the metadata section.");
                        return;
                    }
                    //We assume the search engine scores has been detected previously
                    protein.setSearchEngineScore(id, metadata.getMsRunMap().get(1), (scoreValue != null)?scoreValue.doubleValue():null);
                    protein.setBestSearchEngineScore(id, (scoreValue != null)?scoreValue.doubleValue():null);
                    protein.addSearchEngineParam(searchparam.getSearchEngineParam().getParam());
                }
            }
        }

    }


    private void loadModifications(uk.ac.ebi.pride.jmztab.model.Protein protein, List<Peptide> items) {

        Set<uk.ac.ebi.pride.jmztab.model.Modification> modifications = new TreeSet<uk.ac.ebi.pride.jmztab.model.Modification>(new Comparator<uk.ac.ebi.pride.jmztab.model.Modification>() {
            @Override
            public int compare(uk.ac.ebi.pride.jmztab.model.Modification o1, uk.ac.ebi.pride.jmztab.model.Modification o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });

        for (Peptide item : items) {
            int seqLength = 0;
            if(item.getSequence()!= null){
                seqLength = item.getSequence().length();
            }
            for (Modification ptm : item.getModifications()) {
                // ignore modifications that can't be processed correctly (can not be mapped to the protein)
                if (ptm.getId() == null) {
                    continue;
                }

                // mod without position
                uk.ac.ebi.pride.jmztab.model.Modification mod = MZTabUtils.parseModification(Section.Protein, ptm.getId().toString());

                if (mod != null) {

                    // only biological significant modifications are propagated to the protein
                    if (ModParam.isBiological(ptm.getId().toString())) {
                        // if we can calculate the position, we add it to the modification
                        // -1 to calculate properly the modification offset
                        final PeptideEvidence peptideEvidence = item.getPeptideEvidence();
                        if (peptideEvidence != null && peptideEvidence.getStartPosition() != null && peptideEvidence.getStartPosition() >= 0 && ptm.getLocation() >= 0) {
                            int modLocation = ptm.getLocation();
                            int startPos = peptideEvidence.getStartPosition();
                            // n-term and c-term mods are not propagated to the protein except the case that the start
                            // position is 1 (beginning of the protein)
                            int position = startPos + modLocation - 1;
                            if (modLocation > 0 && modLocation < (seqLength + 1)) {
                                mod.addPosition(position, null);
                                modifications.add(mod);
                            } else if(position == 0) { //n-term for protein
                                mod.addPosition(position, null);
                                modifications.add(mod);
                            }
                        } else {
                            modifications.add(mod);
                            //if position is not set null is reported
                        }
                        // the metadata is updated in the PSM section because the protein modifications are a subset of
                        // the psm modifications
                    }
                }
            }
        }

        //We add to the protein not duplicated modifications
        for (uk.ac.ebi.pride.jmztab.model.Modification modification : modifications) {
            protein.addModification(modification);
        }

    }

    /**
     * Converts the passed Identification object into an MzTab PSM.
     * @param identification Protein Idnetification
     * @return List of mzTab PSMs
     */
    private List<PSM> loadPSMs(Protein identification) {

        List<PSM> psmList = new ArrayList<PSM>();
        CVParam decoy = MzTabUtils.convertCvParamToCVParam(CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DECOY_PEPTIDE, null));

        String header = CVParamOptionColumn.getHeader(null, decoy);

        for (Peptide peptideItem : identification.getPeptides()) {

            // create the peptide object
            PSM psm = new PSM(psmColumnFactory, metadata);

            psm.setSequence(peptideItem.getSequence());

            String spectrumReference;

            if (peptideItem.getSpectrum() != null) {
                psm.setPSM_ID(peptideItem.getSpectrum().getId().toString());
                spectrumReference = "spectrum=" + peptideItem.getSpectrum().getId().toString();
                // set the peptide spectrum reference
                psm.addSpectraRef(new SpectraRef(metadata.getMsRunMap().get(1),  spectrumReference));

            }
            else{
                psm.setPSM_ID(alternativeId++);
                logger.debug("There is no spectrum available, using an alternative id as PSM id.");
            }

            psm.setAccession(removeNewLineAndTab(generateAccession(identification)));
            psm.setDatabase(identification.getDbSequence().getSearchDataBase().getName());
            String version = (identification.getDbSequence().getSearchDataBase().getVersion() != null && ! identification.getDbSequence().getSearchDataBase().getVersion().isEmpty())?
                    identification.getDbSequence().getSearchDataBase().getVersion():null;
            psm.setDatabaseVersion(version);


            if (CvUtilities.isDecoyHit(identification) || CvUtilities.isAccessionDecoy(identification)) {
                MZTabColumn column = psmColumnFactory.findColumnByHeader(header);
                if (column != null) {
                    psm.setOptionColumnValue(decoy, MZBoolean.True);
                } else {
                    psmColumnFactory.addOptionalColumn(decoy, MZBoolean.class);
                    psm.setOptionColumnValue(decoy, MZBoolean.True);
                    logger.debug("The psm decoy column has been added.");
                }
            } else {
                MZTabColumn column = psmColumnFactory.findColumnByHeader(header);
                if (column != null) {
                    psm.setOptionColumnValue(decoy, MZBoolean.False);
                } else {
                    psmColumnFactory.addOptionalColumn(decoy, MZBoolean.class);
                    psm.setOptionColumnValue(decoy, MZBoolean.False);
                    logger.debug("The psm decoy column has been added.");
                }
            }


            // set the search engine - if possible
            loadSearchEngineScore(psm, peptideItem);

            // set the modifications
            // is not necessary check by ambiguous modifications because are not supported in PRIDEXML
            // updates the metadata with fixed and variable modifications
            loadModifications(psm, peptideItem);

            // set exp m/z
            int precursorCharge = peptideItem.getPrecursorCharge();
            double precursorMz = peptideItem.getSpectrumIdentification().getExperimentalMassToCharge();
            double calculatedMz = peptideItem.getSpectrumIdentification().getCalculatedMassToCharge();

            // check the legality of the input arguments first
            if (calculatedMz < 0 && precursorMz > 0 && precursorCharge != 0) {

                // create a new double array
                // attach water loss monoisotopic mass
                double[] masses;
                int size = 1;
                int i = 0;

                if (peptideItem.getModifications() != null && !peptideItem.getModifications().isEmpty()) {
                    size += peptideItem.getModifications().size();
                    masses = new double[size];
                    for (Modification modificationItem : peptideItem.getModifications()) {
                        if (modificationItem.getMonoisotopicMassDelta() != null && !modificationItem.getMonoisotopicMassDelta().isEmpty()) {
                            masses[i++] = modificationItem.getMonoisotopicMassDelta().get(0);
                        } else {
                            final PTM ptm = ModReader.getInstance().getPTMbyAccession((String) modificationItem.getId());
                            //For some parent terms the mass is unknown so we assigns 0.0 in that cases
                            if (ptm != null && ptm.getMonoDeltaMass()!= null) {
                                masses[i++] = ptm.getMonoDeltaMass();
                            }
                            else {
                                masses[i++] = 0.0;
                            }
                            logger.warn("Monoisotopic Mass Delta not available. Calculated m/z can be innacurate");
                        }
                    }
                } else {
                    masses = new double[size];
                }

                masses[i] = NeutralLoss.WATER_LOSS.getMonoMass();

                try {
                    // theoretical mass
                    double theoreticalMass = MoleculeUtilities.calculateTheoreticalMass(peptideItem.getSequence(), masses);

                    // delta mass
                    calculatedMz = (theoreticalMass + precursorCharge * Element.H.getMass()) / precursorCharge;
//                    calculatedMz =  (theoreticalMass + precursorCharge * NuclearParticle.PROTON.getMonoMass()) / precursorCharge;

                } catch (IllegalAminoAcidSequenceException ex) {
                    // do nothing
                    logger.warn("Monoisotopic Mass Delta not available. Illegal Amino Acid Sequence provided");
                }
            }

            psm.setCharge(precursorCharge);
            psm.setExpMassToCharge(precursorMz);
            psm.setCalcMassToCharge(calculatedMz);

            if (peptideItem.getPeptideEvidence().getStartPosition() != null && peptideItem.getPeptideEvidence().getStartPosition()>=0) {
                psm.setStart(peptideItem.getPeptideEvidence().getStartPosition());
            }
            if (peptideItem.getPeptideEvidence().getEndPosition() != null && peptideItem.getPeptideEvidence().getEndPosition()>=0) {
                psm.setEnd(peptideItem.getPeptideEvidence().getEndPosition());
            }

            // process the additional params -- mainly check for quantity units
            if (peptideItem.getSpectrumIdentification() != null) {
                for (CvParam p : peptideItem.getSpectrumIdentification().getCvParams()) {
                    if (CvTermReference.PRIDE_UPSTREAM_FLANKING_SEQUENCE.getAccession().equalsIgnoreCase(p.getAccession())) {
                        psm.setPre(p.getValue());
                    }
                    if (CvTermReference.PRIDE_DOWNSTREAM_FLANKING_SEQUENCE.getAccession().equalsIgnoreCase(p.getAccession())) {
                        psm.setPost(p.getValue());
                    }
                }
            }
            psmList.add(psm);
        }
        return psmList;
    }

    private void loadSearchEngineScore(PSM psm, Peptide peptideItem) {

        //SearchEngineScoreParam psm_searchEngineScoreParam;
        Score score = peptideItem.getScore();
        Integer id;

        if (score != null) {
            //CVParam for the metadata section
            //searchEngineScoreParam = SearchEngineScoreParam.getSearchEngineScoreParamByName(searchEngineName);
            List<SearchEngineScoreParam> searchEngineScoreParam = MzTabUtils.getSearchEngineScoreTerm(peptideItem.getScore());

            if (searchEngineScoreParam != null && searchEngineScoreParam.size()>0) {
                id = -1;
                for(SearchEngineScoreParam searchparam: searchEngineScoreParam){
                    CVParam scoreParam = searchparam.getParam(null);
                    Number scoreValue = score.getValueBySearchEngineScoreTerm(scoreParam.getAccession());
                    for (Map.Entry<Integer, PSMSearchEngineScore> entry : metadata.getPsmSearchEngineScoreMap().entrySet()) {
                        if (entry.getValue().getParam().equals(scoreParam)) {
                            id = entry.getKey();
                            break;
                        }
                    }
                    if (id <= 0) { //if the search engine score is not in the metadata we have a problem
                        logger.warn("The search engine score value can not be converted because the search engine score is not defined in the metadata section.");
                        return;
                    }
                    //We assume the search engine scores has been detected previously
                    psm.setSearchEngineScore(id,  (scoreValue != null)?scoreValue.doubleValue():null);
                    psm.addSearchEngineParam(searchparam.getSearchEngineParam().getParam());
                }
            }
        }
    }

    private void loadModifications(PSM psm, Peptide item) {

        uk.ac.ebi.pride.jmztab.model.Modification mod;
        for (Modification ptm : item.getModifications()) {
            // ignore modifications that can't be processed correctly
            if (ptm.getId() == null) {
                continue;
            }

            mod = MZTabUtils.parseModification(Section.Peptide, ptm.getId().toString());

            if (mod != null) {

                if (ptm.getLocation() >= 0) {
                    Integer position = ptm.getLocation();
                    mod.addPosition(position, null);
                }
                psm.addModification(mod);

                // an additional param should exist with the cv name.
                // if not, we can not convert the modification to the header because we don't store all the mod cv terms
                // in this moment
                if (ptm.getCvParams() != null && ptm.getCvParams().size() > 0) {

                    for(CvParam param: ptm.getCvParams()){

                        Param metadataParam = MzTabUtils.convertCvParamToCVParam(param);

                        // propagate the modification to the metadata section
                        boolean found = false;

                        //For PRIDEXML converter all the modifications are considered variables because
                        // we don't have the information form the original experiment
                        for (VariableMod variableMod : metadata.getVariableModMap().values()) {
                            if (variableMod.getParam().getAccession().equals(ptm.getId())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            //TODO Add the modification site in the future
                            metadata.addVariableModParam(metadata.getVariableModMap().size() + 1, metadataParam);
                        }

                    }

                } else {
                    logger.warn("A CvParam with the modification information is not provided. The modification can not be propagated to the metadata section.");
                }
            } else {
                logger.warn("A CvParam with the modification information is not provided. The modification can not be propagated to the metadata section.");
            }

        }
    }

    private List<String> getAmbiguityMembers(ParamGroup param, String accession) {
        if (param == null || isEmpty(accession)) {
            return null;
        }

        // this only makes sense if we have a list of params and an accession!
        List<String> ambiguityMembers = new ArrayList<String>();
        for (CvParam p : param.getCvParams()) {
            if (accession.equals(p.getAccession())) {
                ambiguityMembers.add(p.getValue());
                logger.debug("Ambiguity member added from PRIDE XML file");
            }
        }

        return ambiguityMembers;
    }

    /**
     * Function to merge proteins that have the same accession. The search engine score will be written in an
     * optional column. The quantification information of the inference is lost after the merge.
     * @param proteinList MzTab Protein List
     * @return only one protein with merge information
     */
    private uk.ac.ebi.pride.jmztab.model.Protein merge(List<uk.ac.ebi.pride.jmztab.model.Protein> proteinList) {


        int numProteinSearchEngineScore = metadata.getProteinSearchEngineScoreMap().size();
        int numDupProteins = proteinList.size();
        MZBoolean hadQuant = MZBoolean.False;

        StringBuilder dupProteinsSearchEngine;
        StringBuilder dupProteinsSearchEngineScore;
        StringBuilder dupProteinsBestSearchEngineScore;

        dupProteinsSearchEngine = new StringBuilder();
        dupProteinsSearchEngineScore = new StringBuilder();
        dupProteinsBestSearchEngineScore = new StringBuilder();

        checkMergeColumnsDefinition();

        //We fill/initialize the information with the first one and we put in optional columns or merge the rest of information
        uk.ac.ebi.pride.jmztab.model.Protein protein = new uk.ac.ebi.pride.jmztab.model.Protein(proteinColumnFactory);

        //More than one protein. Merge and record the problem
        int i = 0;

        for (uk.ac.ebi.pride.jmztab.model.Protein duplicated : proteinList) {
            if (i == 0) {

                protein = proteinList.get(0);

                if (proteinList.size() == 1) {
                    //We don't need to merge proteins
                    dupProteinsSearchEngine.append("null");
                    dupProteinsSearchEngineScore.append("null");
                    dupProteinsBestSearchEngineScore.append("null");
                }

            } else {
                //Information mergeable
                if (duplicated.getModifications() != null) {
                    for (uk.ac.ebi.pride.jmztab.model.Modification modification : duplicated.getModifications()) {
                        if (protein.getModifications() != null) {
                            if (!protein.getModifications().contains(modification)) {
                                protein.addModification(modification);
                            }
                        } else {
                            //initialize the splitList and add the modification
                            protein.addModification(modification);
                        }
                    }
                }

                if (duplicated.getAmbiguityMembers() != null) {
                    for (String ambiguityMember : duplicated.getAmbiguityMembers()) {
                        if (protein.getAmbiguityMembers() != null) {
                            if (!protein.getAmbiguityMembers().contains(ambiguityMember)) {
                                protein.addAmbiguityMembers(ambiguityMember);
                            }
                        } else {
                            protein.addAmbiguityMembers(ambiguityMember);
                        }
                    }
                }

                //Counts
                for (MsRun msRun : metadata.getMsRunMap().values()) {
                    Integer numPeptidesDistinct = duplicated.getNumPeptidesDistinct(msRun);
                    Integer numPeptidesUnique = duplicated.getNumPeptidesUnique(msRun);
                    Integer numPSMs = duplicated.getNumPSMs(msRun);

                    if (numPeptidesDistinct != null) {
                        protein.setNumPeptidesDistinct(msRun, protein.getNumPeptidesDistinct(msRun) + numPeptidesDistinct);
                    }
                    if (numPeptidesUnique != null) {
                        protein.setNumPeptidesUnique(msRun, protein.getNumPeptidesUnique(msRun) + numPeptidesUnique);
                    }
                    if (numPSMs != null) {
                        protein.setNumPSMs(msRun, protein.getNumPSMs(msRun) + numPSMs);
                    }
                }

                //Not mergeable  search_engine
                //Same columns and appended by "|"
                SplitList<Param> searchEngine = duplicated.getSearchEngine();
                if (searchEngine != null) {
                    dupProteinsSearchEngine.append(searchEngine.toString());
                } else {
                    dupProteinsSearchEngine.append((String) null);
                }

                if (i < numDupProteins - 1) {
                    dupProteinsSearchEngine.append(";");
                }

                //Not mergeable  search_engine_score per ms_run
                for (ProteinSearchEngineScore proteinSearchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
                    Param roParam = proteinSearchEngineScore.getParam();   //not setValue allow in Param
                    for (MsRun msRun : metadata.getMsRunMap().values()) {

                        Double searchEngineScore = duplicated.getSearchEngineScore(proteinSearchEngineScore.getId(), msRun);
                        CVParam cvParam;
                        if (searchEngineScore != null) {
                            cvParam = new CVParam(roParam.getCvLabel(), roParam.getAccession(), roParam.getName(), searchEngineScore.toString());
                        } else {
                            cvParam = new CVParam(roParam.getCvLabel(), roParam.getAccession(), roParam.getName(), null);
                        }
                        dupProteinsSearchEngineScore.append(cvParam.toString());
                        if (proteinSearchEngineScore.getId() < numProteinSearchEngineScore) {
                            dupProteinsBestSearchEngineScore.append("|");
                        }
                    }
                }

                if (i < numDupProteins - 1) {
                    dupProteinsSearchEngineScore.append(";");
                }

                //Not mergeable  best_search_engine_score
                for (ProteinSearchEngineScore proteinSearchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
                    Param roParam = proteinSearchEngineScore.getParam();   //not setValue allow

                    Double bestSearchEngineScore = duplicated.getBestSearchEngineScore(proteinSearchEngineScore.getId());
                    CVParam cvParam;
                    if (bestSearchEngineScore != null) {
                        cvParam = new CVParam(roParam.getCvLabel(), roParam.getAccession(), roParam.getName(), bestSearchEngineScore.toString());
                    } else {
                        cvParam = new CVParam(roParam.getCvLabel(), roParam.getAccession(), roParam.getName(), null);
                    }

                    dupProteinsBestSearchEngineScore.append(cvParam.toString());
                    if (proteinSearchEngineScore.getId() < numProteinSearchEngineScore) {
                        dupProteinsBestSearchEngineScore.append("|");
                    }
                }

                if (i < numDupProteins - 1) {
                    dupProteinsBestSearchEngineScore.append(";");
                }
            }
            i++;
        }



        if(numDupProteins > 1){
            logger.warn(numDupProteins + " duplicated proteins with accession " + protein.getAccession() + " have been merge");
        }

        protein.setOptionColumnValue(PRIDEUtils.NUM_MERGE_PROTEINS, numDupProteins);
        protein.setOptionColumnValue(PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES, dupProteinsSearchEngine.toString());
        protein.setOptionColumnValue(PRIDEUtils.DUP_PROTEINS_BEST_SEARCH_ENGINES_SCORE, dupProteinsBestSearchEngineScore.toString());
        protein.setOptionColumnValue(PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES_SCORES, dupProteinsSearchEngineScore.toString());

        //Quant
        if(metadata.getProteinQuantificationUnit() != null){
            hadQuant = MZBoolean.True;
        }

        protein.setOptionColumnValue(PRIDEUtils.DUP_PROTEINS_HAD_QUANT, hadQuant);

        if(hadQuant == MZBoolean.True){
            logger.warn(numDupProteins + " duplicated proteins with accession " + protein.getAccession() + " contained quantification information");
        }

        return protein;
    }

    private void checkMergeColumnsDefinition() {

        String header;
        MZTabColumn column;

        //Optional columns definition

        header = OptionColumn.getHeader(null, PRIDEUtils.NUM_MERGE_PROTEINS);
        column = proteinColumnFactory.findColumnByHeader(header);
        if (column == null) {
            proteinColumnFactory.addOptionalColumn(PRIDEUtils.NUM_MERGE_PROTEINS, Integer.class);
        }

        header = OptionColumn.getHeader(null, PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES);
        column = proteinColumnFactory.findColumnByHeader(header);
        if (column == null) {
            proteinColumnFactory.addOptionalColumn(PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES, String.class);
        }

        header = OptionColumn.getHeader(null, PRIDEUtils.DUP_PROTEINS_BEST_SEARCH_ENGINES_SCORE);
        column = proteinColumnFactory.findColumnByHeader(header);
        if (column == null) {
            proteinColumnFactory.addOptionalColumn(PRIDEUtils.DUP_PROTEINS_BEST_SEARCH_ENGINES_SCORE, String.class);
        }

        header = OptionColumn.getHeader(null, PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES_SCORES);
        column = proteinColumnFactory.findColumnByHeader(header);
        if (column == null) {
            proteinColumnFactory.addOptionalColumn(PRIDEUtils.DUP_PROTEINS_SEARCH_ENGINES_SCORES, String.class);
        }

        header = OptionColumn.getHeader(null, PRIDEUtils.DUP_PROTEINS_HAD_QUANT);
        column = proteinColumnFactory.findColumnByHeader(header);
        if (column == null) {
            proteinColumnFactory.addOptionalColumn(PRIDEUtils.DUP_PROTEINS_HAD_QUANT, MZBoolean.class);
        }
    }

}

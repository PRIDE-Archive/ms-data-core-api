package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.Comment;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import javax.xml.bind.JAXBException;
import java.util.*;

/**
 * @author ntoro
 * @since 24/02/15 10:10
 */
public class PassThresholdMzIdentMLMzTabConverter extends MzIdentMLMzTabConverter {

    protected static Logger logger = Logger.getLogger(PassThresholdMzIdentMLMzTabConverter.class);

    public static final String NO_THRESHOLD_MS_AC = "MS:1001494";
    Boolean proteinDetectionProtocolThreshold = null;
    Boolean spectrumIdentificationProtocolThreshold = null;


    /**
     * Default constructor
     *
     * @param controller The DataAccessController to be Converted to MzTab
     */
    public PassThresholdMzIdentMLMzTabConverter(DataAccessController controller) {
        super(controller);
        detectNoThreshold();
    }

    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected void fillData() {
        // Get a list of Identification ids
        proteinIds = new HashSet<Comparable>();

        //Try to detect if threshold is not provided
        try {
            if (!source.hasProteinAmbiguityGroup()) {
                //We have threshold at protein level
                if (!proteinDetectionProtocolThreshold) {
                    Collection<Comparable> proteinIds = source.getProteinIds();
                    //Iterate over proteins
                    for (Comparable id : proteinIds) {

                        // Check the protein and peptides threshold
                        uk.ac.ebi.pride.utilities.data.core.Protein msProtein = source.getProteinById(id);

                        if (msProtein.isPassThreshold()) {
                            List<Peptide> peptides = getPassThresholdScannedSpectrumIdentificationItems(msProtein);
                            Protein identification = loadProtein(msProtein, peptides);
                            proteins.add(identification);
                            psms.addAll(loadPSMs(msProtein, peptides));
                        } else {
                            //Can be the case that the protein is not coming from protein hypothesis so the threshold needs to be checked at peptide level.
                            List<Peptide> peptides = getPassThresholdScannedSpectrumIdentificationItems(msProtein);
                            if (peptides != null && !peptides.isEmpty()) {
                                //We have at least some peptides that pass the threshold
                                Protein identification = loadProtein(msProtein, peptides);
                                proteins.add(identification);
                                psms.addAll(loadPSMs(msProtein, peptides));
                            } else {
                                super.fillData();
                                //All of them are false or they are not annotated
                                logger.warn("No peptides pass the threshold for protein: " + id);
                            }
                        }
                    }
                }
                //We don't have threshold at protein level but we have at spectrum level
                else if (spectrumIdentificationProtocolThreshold) {
                    logger.warn("Option not implemented yet");

                } else {
                    super.fillData();
                    //All of them are false or they are not annotated
                    logger.warn("Threshold is not provided");
                }
            } else {
                if (proteinDetectionProtocolThreshold== null ) {
//                    Collection<Comparable> proteinIds = source.getProteinIds();
//                    //Iterate over proteins
//                    for (Comparable id : proteinIds) {
//
//                        // Check the protein and peptides threshold
//                        uk.ac.ebi.pride.utilities.data.core.Protein msProtein = source.getProteinById(id);
//
//                        if (msProtein.isPassThreshold()) {
//                            List<Peptide> peptides = getPassThresholdScannedSpectrumIdentificationItems(msProtein);
//                            Protein identification = loadProtein(msProtein, peptides);
//                            proteins.add(identification);
//                            psms.addAll(loadPSMs(msProtein, peptides));
//                        } else {
//                            //Can be the case that the protein is not coming from protein hypothesis so the threshold needs to be checked at peptide level.
//                            List<Peptide> peptides = getPassThresholdScannedSpectrumIdentificationItems(msProtein);
//                            if (peptides != null && !peptides.isEmpty()) {
//                                //We have at least some peptides that pass the threshold
//                                Protein identification = loadProtein(msProtein, peptides);
//                                proteins.add(identification);
//                                psms.addAll(loadPSMs(msProtein, peptides));
//                            } else {
//                                super.fillData();
//                                //All of them are false or they are not annotated
//                                logger.warn("No peptides pass the threshold for protein: " + id);
//                            }
//                        }
//                    }
                    Collection<Comparable> proteinGroupIds = source.getProteinAmbiguityGroupIds();
                    for (Comparable proteinGroupId : proteinGroupIds) {
                        Protein identification = getProteinGroupById(proteinGroupId);
                        proteins.add(identification);
                        psms.addAll(loadPSMs(source.getProteinAmbiguityGroupById(proteinGroupId).getProteinIds()));
                    }
                }
                //We don't have threshold at protein level but we have at spectrum level
                else if (spectrumIdentificationProtocolThreshold) {
                    logger.warn("Option not implemented yet");

                } else {
                    super.fillData();
                    //All of them are false or they are not annotated
                    logger.warn("Threshold is not provided");
                }
            }
        } catch (JAXBException e) {
            throw new DataAccessException("Error try to retrieve the information for own Protein");
        }

        if (metadata.getFixedModMap().isEmpty()) {
            Comment comment = new Comment("Only variable modifications can be reported when the original source is a MZIdentML XML file");
            getMZTabFile().addComment(1, comment);
            metadata.addFixedModParam(1, new CVParam("MS", "MS:1002453", "No fixed modifications searched", null));
        }
        if (metadata.getVariableModMap().isEmpty()) {
            metadata.addVariableModParam(1, new CVParam("MS", "MS:1002454", "No variable modifications searched", null));
        }


    }

    private void detectNoThreshold() {

        if (source.getIdentificationMetaData() != null) {
            List<SpectrumIdentificationProtocol> protocols = source.getIdentificationMetaData().getSpectrumIdentificationProtocols();
            if (protocols != null && !protocols.isEmpty()) {
                for (SpectrumIdentificationProtocol protocol : protocols) {
                    if (protocol != null) {
                        ParamGroup threshold = protocol.getThreshold();
                        if (threshold.getCvParams() != null && !threshold.getCvParams().isEmpty()) {
                            if (threshold.getCvParams().get(0).getAccession().equals(NO_THRESHOLD_MS_AC)) {
                                spectrumIdentificationProtocolThreshold = false;
                            }
                        }
                    }
                }
            }
            if (source.getIdentificationMetaData().getProteinDetectionProtocol() != null) {
                ParamGroup threshold = source.getIdentificationMetaData().getProteinDetectionProtocol().getThreshold();
                if (threshold.getCvParams() != null && !threshold.getCvParams().isEmpty()) {
                    if (threshold.getCvParams().get(0).getAccession().equals(NO_THRESHOLD_MS_AC)) {
                        proteinDetectionProtocolThreshold = false;
                    }
                }
            }
        }
    }

    protected Protein getProteinGroupById(Comparable proteinGroupId) throws JAXBException {

        //TODO: Review
        Protein protein = null;

        ProteinGroup proteinAmbiguityGroup = source.getProteinAmbiguityGroupById(proteinGroupId);

        List<uk.ac.ebi.pride.utilities.data.core.Protein> msProteins = getPassThresholdProteinIdentificationItems(proteinAmbiguityGroup);

        if(!msProteins.isEmpty()){
        // Todo: We will annotated only the first protein, the core protein
        // and the proteins with different peptides to the core protein to avoid lost peptide identification
        uk.ac.ebi.pride.utilities.data.core.Protein firstProteinDetectionHypothesis = msProteins.get(0);
        if (proteinIds.contains(firstProteinDetectionHypothesis.getDbSequence().getAccession()))
            throw new DataAccessException("mzTab do not support one protein in more than one ambiguity groups.");
        else
            proteinIds.add(firstProteinDetectionHypothesis.getDbSequence().getAccession());

        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = getPassThresholdScannedSpectrumIdentificationItems(firstProteinDetectionHypothesis);
            protein = loadProtein(firstProteinDetectionHypothesis, peptides);

            String membersString = "";
        for(int i=1; i < proteinAmbiguityGroup.getProteinDetectionHypothesis().size();i++)
            membersString = proteinAmbiguityGroup.getProteinDetectionHypothesis().get(i).getDbSequence().getAccession() + ",";

        membersString = (membersString.isEmpty())?membersString:membersString.substring(0, membersString.length()-1);
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
        }
        return protein;
    }

    private List<uk.ac.ebi.pride.utilities.data.core.Protein> getPassThresholdProteinIdentificationItems(ProteinGroup proteinAmbiguityGroup) {
        List<uk.ac.ebi.pride.utilities.data.core.Protein> passThresholdProteins = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Protein>();

        for (uk.ac.ebi.pride.utilities.data.core.Protein protein : proteinAmbiguityGroup.getProteinDetectionHypothesis()) {
            if(protein.isPassThreshold()){
                passThresholdProteins.add(protein);
            }
        }

        return passThresholdProteins;
    }


    private List<uk.ac.ebi.pride.utilities.data.core.Peptide> getPassThresholdScannedSpectrumIdentificationItems(uk.ac.ebi.pride.utilities.data.core.Protein protein) {

        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Peptide>();

        for (Peptide peptide : protein.getPeptides()) {
            if (peptide.getSpectrumIdentification() != null) {
               if( peptide.getSpectrumIdentification().isPassThreshold()) {
                   peptides.add(peptide);
               }
            }
        }

        return peptides;

    }
}

package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.Comment;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.filter.NoProteinFilter;
import uk.ac.ebi.pride.utilities.data.filter.PeptideFilter;
import uk.ac.ebi.pride.utilities.data.filter.ProteinFilter;
import uk.ac.ebi.pride.utilities.data.filter.RankOnePeptideFilter;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import java.util.*;

/**
 * @author ntoro
 * @since 24/02/15 10:10
 */
public class PassThresholdMzIdentMLMzTabConverter extends MzIdentMLMzTabConverter {

    protected static Logger logger = Logger.getLogger(PassThresholdMzIdentMLMzTabConverter.class);

    public static final String NO_THRESHOLD_MS_AC = "MS:1001494";
    public static final String NO_THRESHOLD = "no threshold";

    private final class AmbiguityGroup {

        uk.ac.ebi.pride.utilities.data.core.Protein anchorProtein;
        List<uk.ac.ebi.pride.utilities.data.core.Protein> restOfMembers;
        List<Peptide> anchorPeptides;

    }


    /**
     * Default constructor
     *
     * @param controller The DataAccessController to be Converted to MzTab
     */
    public PassThresholdMzIdentMLMzTabConverter(DataAccessController controller) {
        super(controller);
    }

    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected void fillData() {
        // Get a list of Identification ids
        proteinIds = new HashSet<Comparable>();

        if (source.hasProteinAmbiguityGroup()) {
            Collection<Comparable> proteinGroupIds = source.getProteinAmbiguityGroupIds();

            final IdentificationMetaData identificationMetaData = source.getIdentificationMetaData();
            if (identificationMetaData != null) {
                //We assume that if we have protein ambiguity group, we have protein detection protocol
                final Protocol proteinProtocol = identificationMetaData.getProteinDetectionProtocol();

                if (proteinProtocol != null) {
                    // It detects if the CV Term no_threshold is used
                    if (noThresholdAvailable(proteinProtocol)) {
                        //We try to detect the spectrum detection protocol threshold
                        final List<SpectrumIdentificationProtocol> protocols = identificationMetaData.getSpectrumIdentificationProtocols();
                        if (noThresholdAvailable(protocols)) {
                            //RANK 1 FILTER. The proteins and protein groups will be filtered according to the remaining psms
                            for (Comparable proteinGroupId : proteinGroupIds) {

                                final ProteinGroup proteinAmbiguityGroup = source.getProteinAmbiguityGroupById(proteinGroupId);
                                final List<uk.ac.ebi.pride.utilities.data.core.Protein> proteinDetectionHypothesis = proteinAmbiguityGroup.getProteinDetectionHypothesis();
                                List<AmbiguityGroup> identifications = getProteinGroupById(proteinDetectionHypothesis, new NoProteinFilter(), new RankOnePeptideFilter());

                                for (AmbiguityGroup identification : identifications) {
                                    //We don't have proteins without peptides
                                    Protein protein;

                                    if (proteinIds.contains(identification.anchorProtein.getDbSequence().getAccession()))
                                        throw new DataAccessException("mzTab do not support the same protein as anchor of more than one ambiguity groups.");
                                    else
                                        proteinIds.add(identification.anchorProtein.getDbSequence().getAccession());

                                    protein = loadProtein(identification.anchorProtein, identification.anchorPeptides);

                                    //We retrieve the other members in the group
                                    String membersString = "";

                                    for (uk.ac.ebi.pride.utilities.data.core.Protein member : identification.restOfMembers) {
                                        membersString = generateAccession(member) + ",";
                                    }

                                    membersString = (membersString.isEmpty()) ? membersString : membersString.substring(0, membersString.length() - 1);
                                    protein.addAmbiguityMembers(membersString);

                                    //Loop for spectrum to get all the ms_run to repeat the score at protein level
                                    Set<MsRun> msRuns = new HashSet<MsRun>();
                                    for (int index = 0; index < identification.anchorPeptides.size(); index++) {
                                        Comparable id = source.getPeptideSpectrumId(identification.anchorProtein.getId(), index);
                                        if (id != null) {
                                            String[] spectumMap = id.toString().split("!");
                                            MsRun msRun = metadata.getMsRunMap().get(spectraToRun.get(spectumMap[1]));
                                            msRuns.add(msRun);
                                        }
                                    }
                                    // See which protein scores are supported

                                    for (CvParam cvPAram : identification.anchorProtein.getCvParams()) {
                                        if (proteinScoreToScoreIndex.containsKey(cvPAram.getAccession())) {
                                            CVParam param = MzTabUtils.convertCvParamToCVParam(cvPAram);
                                            int idCount = proteinScoreToScoreIndex.get(cvPAram.getAccession());
                                            for (MsRun msRun : metadata.getMsRunMap().values()) {
                                                String value = null;
                                                if (msRuns.contains(msRun))
                                                    value = param.getValue();
                                                protein.setSearchEngineScore(idCount, msRun, value);
                                            }
                                        }
                                    }

                                    proteins.add(protein);
                                    psms.addAll(loadPSMs(identification.anchorProtein, identification.anchorPeptides));
                                }
                            }
                        }
                    } else {


                    }
                }
            }
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

    protected List<AmbiguityGroup> getProteinGroupById(List<uk.ac.ebi.pride.utilities.data.core.Protein> proteins, ProteinFilter proteinFilter, PeptideFilter peptidesPeptideFilter) {


        List<AmbiguityGroup> ambiguityGroups = new ArrayList<AmbiguityGroup>();

        List<uk.ac.ebi.pride.utilities.data.core.Protein> msProteins = proteinFilter.filter(proteins);

        /*
            We annotate only the first protein, as the anchor protein,
            and the proteins with different peptides to the core protein to avoid lost peptide identifications
        */
        if (!msProteins.isEmpty()) {

            uk.ac.ebi.pride.utilities.data.core.Protein anchorProtein;
            List<uk.ac.ebi.pride.utilities.data.core.Peptide> anchorPeptides;

            for (int i = 0; i < msProteins.size(); i++) {
                AmbiguityGroup ambiguityGroup = new AmbiguityGroup();

                anchorProtein = msProteins.get(i);
                anchorPeptides = peptidesPeptideFilter.filter(anchorProtein.getPeptides());
                if (!anchorPeptides.isEmpty()) {

                    List<uk.ac.ebi.pride.utilities.data.core.Protein> members = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Protein>(msProteins);
                    //We remove the anchor protein for the members
                    members.remove(i);

                    ambiguityGroup.anchorProtein = anchorProtein;
                    ambiguityGroup.restOfMembers = members;
                    ambiguityGroup.anchorPeptides = anchorPeptides;
                    ambiguityGroups.add(ambiguityGroup);

                }
                //TODO: Merge same-set groups
            }
        }

        return ambiguityGroups;
    }

    private static boolean noThresholdAvailable(List<? extends Protocol> protocols) {

        for (Protocol protocol : protocols) {
            if (noThresholdAvailable(protocol)) {
                return true;
            }
        }
        return false;
    }

    private static boolean noThresholdAvailable(Protocol protocol) {

        if (protocol != null) {
            //Threshold is mandatory in the schema
            ParamGroup threshold = protocol.getThreshold();
            if (threshold.getCvParams() != null && !threshold.getCvParams().isEmpty()) {
                //If it is no-threshold, it will have only one cv
                for (CvParam cvParam : threshold.getCvParams()) {
                    if (cvParam.getAccession().equals(NO_THRESHOLD_MS_AC)) {
                        return true;
                    }
                }
            }
            if (threshold.getUserParams() != null && !threshold.getUserParams().isEmpty()) {
                //If it is no-threshold, it will have only one cv
                for (UserParam userParam : threshold.getUserParams()) {
                    if (userParam.getValue().equals(NO_THRESHOLD) || userParam.getName().equalsIgnoreCase(NO_THRESHOLD)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

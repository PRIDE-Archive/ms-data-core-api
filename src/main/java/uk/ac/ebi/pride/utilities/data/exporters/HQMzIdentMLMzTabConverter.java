package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.filter.*;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import java.util.*;

/**
 * @author ntoro
 * @since 24/02/15 10:10
 */
public class HQMzIdentMLMzTabConverter extends MzIdentMLMzTabConverter {

    protected static Logger logger = Logger.getLogger(HQMzIdentMLMzTabConverter.class);

    public static final String NO_THRESHOLD_MS_AC = "MS:1001494";
    public static final String NO_THRESHOLD = "no threshold";
    public static final String SAME_SET = "same_set";
    private final MzIdentMLControllerImpl controller;


    private final class AmbiguityGroup {

        uk.ac.ebi.pride.utilities.data.core.Protein anchorProtein;
        List<uk.ac.ebi.pride.utilities.data.core.Protein> restOfMembers;
        List<Peptide> anchorPeptides;
        boolean sameSet = false;

    }


    /**
     * Default constructor
     *
     * @param controller The DataAccessController to be Converted to MzTab
     */
    public HQMzIdentMLMzTabConverter(MzIdentMLControllerImpl controller) {
        super(controller);
        this.controller = controller;

    }

    @Override
    protected MZTabColumnFactory convertProteinColumnFactory() {
        super.convertProteinColumnFactory();
        //We add sameSet column to clarify the ambiguity in the groups

//        proteinColumnFactory.addOptionalColumn(SAME_SET, String.class);

        return proteinColumnFactory;

    }
    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected void fillData() {
        // Get a list of Identification ids
        proteinIds = new HashSet<Comparable>();
        ProteinFilter proteinFilter = new NoProteinFilter();
        PeptideFilter peptideFilter = new NoPeptideFilter();

        final IdentificationMetaData identificationMetaData = source.getIdentificationMetaData();

        if (identificationMetaData != null) {
            //We assume that if we have protein ambiguity group, we have protein detection protocol
            final Protocol proteinDetectionProtocol = identificationMetaData.getProteinDetectionProtocol();
            final List<SpectrumIdentificationProtocol> spectrumIdDetectionProtocols = identificationMetaData.getSpectrumIdentificationProtocols();

            if (proteinDetectionProtocol == null || noThresholdAvailable(proteinDetectionProtocol)) {

                proteinFilter = new NoProteinFilter();

                //We try to detect the spectrum detection protocol no_threshold
                if (noThresholdAvailable(spectrumIdDetectionProtocols)) {
                    //RANK 1 FILTER. The proteins and protein groups will be filtered according to the remaining psms
                    peptideFilter = new RankOnePeptideFilter();
                } else {
                    //Threshold filter. The proteins and protein groups will be filtered according to the remaining psms
                    peptideFilter = new ThresholdPeptideFilter();
                }
            } else {
                //Threshold filter. The proteins and protein groups will be filtered according to threshold first
                proteinFilter = new ThresholdProteinFilter();
                if (noThresholdAvailable(spectrumIdDetectionProtocols)) {
                    //TODO Rewrite comment
                    // The proteins and protein groups will not be filtered to avoid remove proteins that has passed the filter
//                        peptideFilter = new NoPeptideFilter();
                    peptideFilter = new ThresholdPeptideFilter();

                } else {
                    // Threshold filter. We don't expect any change in the proteins after the filter is applied
                    // TODO: Detect the case a log a warning
                    peptideFilter = new ThresholdPeptideFilter();
                }
            }

        } else {
            //No threshold information at all (protein or peptide)
            proteinFilter = new NoProteinFilter();
            peptideFilter = new RankOnePeptideFilter();
        }


        if (source.hasProteinAmbiguityGroup()) {
            Collection<Comparable> proteinGroupIds = source.getProteinAmbiguityGroupIds();
            for (Comparable proteinGroupId : proteinGroupIds) {

                final ProteinGroup proteinAmbiguityGroup = source.getProteinAmbiguityGroupById(proteinGroupId);
                final List<uk.ac.ebi.pride.utilities.data.core.Protein> proteinDetectionHypothesis = proteinAmbiguityGroup.getProteinDetectionHypothesis();

                List<AmbiguityGroup> identifications = getProteinGroupById(proteinDetectionHypothesis, proteinFilter, peptideFilter);
                 if(!identifications.isEmpty()) {
                     for (AmbiguityGroup identification : identifications) {
                         //We don't have proteins without peptides
                         uk.ac.ebi.pride.jmztab.model.Protein protein;

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
//                             source.getSpectrumIdForPeptide(identification.anchorPeptides.get(index).getPeptideEvidence().getId());
                             Comparable id = controller.getSpectrumIdBySpectrumIdentificationItemId(identification.anchorPeptides.get(index).getSpectrumIdentification().getId());
                             //id = identification.anchorPeptides.get(index).getPeptideEvidence().getId();
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

//                         protein.setOptionColumnValue(SAME_SET, "" + identification.sameSet + "\"");
                         proteins.add(protein);
                         psms.addAll(loadPSMs(identification.anchorProtein, identification.anchorPeptides));
                     }
                 } else {
                     logger.debug("Protein group " + proteinGroupId + " filtered.");
                 }
            }

        }
        else { // No ambiguity
             //No PROTEINS PRE FILTER -> NO THRESHOLD AVAILABLE
            if (identificationMetaData != null) {
                assert identificationMetaData.getProteinDetectionProtocol() == null;
            }
            // Iterate over proteins. We assume that there is no threshold information because there is no detection list,
            // so we don't have proteinDetectionProtocol
            for (Comparable id : source.getProteinIds()) {

                // Check the protein and peptides threshold
                uk.ac.ebi.pride.utilities.data.core.Protein msProtein = source.getProteinById(id);
                List<Peptide> peptides = peptideFilter.filter(msProtein.getPeptides());

                if (peptides != null) {
                    uk.ac.ebi.pride.jmztab.model.Protein identification = loadProtein(msProtein, peptides);
//                    identification.setOptionColumnValue(SAME_SET, null);
                    proteins.add(identification);
                    psms.addAll(loadPSMs(msProtein, peptides));
                }
                else {
                    logger.debug("No peptides pass the threshold for protein: " + id);
                }
            }
        }

        loadMetadataModifications();

    }

    protected List<AmbiguityGroup> getProteinGroupById(List<uk.ac.ebi.pride.utilities.data.core.Protein> proteins, ProteinFilter proteinFilter, PeptideFilter peptidesFilter) {


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
                anchorPeptides = peptidesFilter.filter(anchorProtein.getPeptides());
                if (!anchorPeptides.isEmpty()) {

                    List<uk.ac.ebi.pride.utilities.data.core.Protein> members = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Protein>(msProteins);
                    //We remove the anchor protein for the members
                    members.remove(i);

                    ambiguityGroup.anchorProtein = anchorProtein;
                    ambiguityGroup.restOfMembers = members;
                    ambiguityGroup.anchorPeptides = anchorPeptides;
                    ambiguityGroups.add(ambiguityGroup);

                }
                else {
                    logger.debug("No peptides pass the threshold for protein: " + generateAccession(anchorProtein) + "in the protein group");
                }
            }
        }

        //Possible merge in case of same set
//        if(!ambiguityGroups.isEmpty()) {
//            List<AmbiguityGroup> aux = new ArrayList<AmbiguityGroup>(ambiguityGroups);
//            boolean sameSet = true;
//
//            for (int i = 0; i < ambiguityGroups.size() - 1 && sameSet; i++) {
//                Set<Peptide> one = new HashSet<Peptide>(ambiguityGroups.get(i).anchorPeptides);
//                Set<Peptide> two = new HashSet<Peptide>(ambiguityGroups.get(i + 1).anchorPeptides);
//
//
//                if (one.equals(two)) {
//                    aux.remove(0); //always the head
//                    //We can do it but we will loose the position of the mappings for the no anchor proteins
//                    //because in reality these are not only psm, are peptideEvidences.
//                } else {
//                    sameSet = false;
//                }
//            }
//
//            if (sameSet) {
//                ambiguityGroups = aux;
//                if (ambiguityGroups.size() == 1) {
//                    ambiguityGroups.get(0).sameSet = true;
//                } else {
//                    logger.warn("Same set with more that one protein");
//                }
//            }
//        }

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

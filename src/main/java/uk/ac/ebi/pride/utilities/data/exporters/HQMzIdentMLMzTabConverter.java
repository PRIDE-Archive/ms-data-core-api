package uk.ac.ebi.pride.utilities.data.exporters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.filter.*;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import java.util.*;

/**
 *  The filtering when exporting a mzIdentML to mzTab is done follows the next set of rules:
 *
 *      If there is not protein detection protocol in mzIdentML (e. g. no ambiguity groups provided) or there is not threshold define in the protein detection protocol:
 *          -The filtering can not be done at protein level directly. In this case is needed to look into the spectrum identification protocol.
 *              -If there is no threshold available at spectrum identification protocol
 *                  The spectra is filtered using rank information. Only spectrum with rank one pass the filter
 *              -If there is a threshold available at spectrum identification protocol
 *                  The spectra is filtered using using the provided threshold
 *          -Only the proteins whose spectra remain after the filtering will be kept.
 *      If there is protein detection protocol in mzIdentML the proteins and protein groups will be filtered according to threshold first.
 *               - After that the filtering by threshold at peptide level will be applied, because in the worst case scenario it will remove only proteins without spectra evidence that pass the filter.
 *               Before NoPeptideFilter was used to avoid inconsistencies with the protein filter, however was observed that some spectra evidences that did not pass the threshold were
 *               included because the threshold was provided but was incorrectly annotated in the file as NoThresholdAvailable. This option minimized the inclusion of spectra under the threshold.
 *      If there is no threshold information at protein or peptide level available
 *           -The spectra is filtered using rank information. Only spectrum with rank one pass the filter
 *           -Only the proteins whose spectra remain after the filtering will be kept.
 *
 *  @author ntoro
 *  @since 24/02/15 10:10
 */
public class HQMzIdentMLMzTabConverter extends MzIdentMLMzTabConverter {

    protected static Logger logger = LoggerFactory.getLogger(HQMzIdentMLMzTabConverter.class);

    public static final String NO_THRESHOLD_MS_AC = "MS:1001494";
    public static final String NO_THRESHOLD = "no threshold";
    private final MzIdentMLControllerImpl controller;


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
                    //Threshold filter. After that the filtering by threshold at peptide level will be applied,
                    // because in the worst case scenario it will remove only proteins without spectra evidence that pass the filter.
                    // Before NoPeptideFilter was used to avoid inconsistencies with the protein filter,
                    // however was observed that some spectra evidences that didn't pass the threshold were included because the threshold was provided but was
                    // incorrectly annotated in the file as NoThresholdAvailable. This option minimized he inclusion of under the threshold spectra
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

//                         if (proteinIds.contains(identification.anchorProtein.getDbSequence().getAccession()))
//                             throw new DataAccessException("mzTab do not support the same protein as anchor of more than one ambiguity groups.");
//                         else
//                             proteinIds.add(identification.anchorProtein.getDbSequence().getAccession());

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
                             Comparable id = controller.getSpectrumIdBySpectrumIdentificationItemId(identification.anchorPeptides.get(index).getSpectrumIdentification().getId());
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

                         if(!proteinIds.contains(protein.getAccession())){
                             proteinIds.add(protein.getAccession());
                             proteins.add(protein);
                         }else {
                             for(uk.ac.ebi.pride.jmztab.model.Protein oldProtein: proteins){
                                 if(oldProtein.getAccession().equalsIgnoreCase(protein.getAccession())){
                                     if(protein.getAmbiguityMembers() != null)
                                         for(String member: protein.getAmbiguityMembers())
                                             oldProtein.addAmbiguityMembers(member);
                                 }
                             }
                         }
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

    private final class AmbiguityGroup {
        uk.ac.ebi.pride.utilities.data.core.Protein anchorProtein;
        List<uk.ac.ebi.pride.utilities.data.core.Protein> restOfMembers;
        List<Peptide> anchorPeptides;
    }

}

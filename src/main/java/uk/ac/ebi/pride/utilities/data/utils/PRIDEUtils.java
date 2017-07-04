package uk.ac.ebi.pride.utilities.data.utils;

import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class PRIDEUtils {

    public static final String DUP_PROTEINS_SEARCH_ENGINES = "duplicated_proteins_search_engines";
    public static final String DUP_PROTEINS_SEARCH_ENGINES_SCORES = "duplicated_proteins_search_engines_scores";
    public static final String DUP_PROTEINS_BEST_SEARCH_ENGINES_SCORE = "duplicated_proteins_best_search_engines_score";
    public static final String DUP_PROTEINS_HAD_QUANT = "duplicated_proteins_had_quantification";
    public static final String NUM_MERGE_PROTEINS = "num_merge_proteins";

    public static final List<String> PREFIX_PRIDE_DECOY_ENTRIES  = Arrays.asList("DECOY_", "#C#", "#DECOY#",
            "###REV###", "REV_", "REVERSE_", "##REV", "DECOY_REV", "RANDOM_", "###RND###", "##RND","REV");
    public static final List<String> POSTFIX_PRIDE_DECOY_ENTRIES = Arrays.asList("_REVERSED", "-DECOY", "RANDOM_","|RND");
    public static final List<String> MIDDLE_PRIDE_DECOY_ENTRIES  = Arrays.asList("RANDOM_", "REV_", "_REVERSED");

    /**
     * Conversion from PRIDE Software to CVParam using a general CVTerm like Analysis Software
     * @param name Generic name of a Software
     * @param version Software Version
     * @return Return the generic CV term for Software
     */
    public static CvParam convertSoftwareNameToCvPram(String name, String version){

        CvParam cvParam = null;
        StringBuilder sb = new StringBuilder();

        if (!StringUtils.isEmpty(name)) {
            sb.append(name);
        }

        if (!StringUtils.isEmpty(version)) {
            sb.append(" v").append(version);
        }

        if(sb.length() > 0)
            cvParam =  CvUtilities.getCVTermFromCvReference(CvTermReference.MS_ANALYSIS_SOFTWARE,  sb.toString().replaceAll(",", ""));

        return cvParam;

    }




}

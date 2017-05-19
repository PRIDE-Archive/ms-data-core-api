package uk.ac.ebi.pride.utilities.data.utils;

import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineScoreParam;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Score;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;
import uk.ac.ebi.pride.utilities.term.SearchEngineScoreCvTermReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class MzTabUtils {

    public static final String OPTIONAL_ID_COLUMN            = "mzidentml_original_ID";
    public static final String OPTIONAL_SEQUENCE_COLUMN      = "protein_sequence";
    public static final String OPTIONAL_DECOY_COLUMN         = "cv_MS:1002217_decoy_peptide";
    public static final String OPTIONAL_RANK_COLUMN          = "cv_PRIDE:0000091_rank";
    public static final String OPTIONAL_CHROM_COLUMN         = "cv_MS:1002637_chromosome_name";
    public static final String OPTIONAL_STRAND_COLUMN        = "cv_MS:1002638_chromosome_strand";
    public static final String OPTIONAL_CHROMEND_COLUMN      = "cv_MS:1002640_peptide_end_on_chromosome";
    public static final String OPTIONAL_CHROM_EXON_COUNT_COLUMN        = "cv_MS:1002641_peptide_exon_count";
    public static final String OPTIONAL_CHROM_EXON_SIZES_COLUMN        = "cv_MS:1002642_peptide_exon_nucleotide_sizes";
    public static final String OPTIONAL_CHROM_EXON_STARTS_COLUMN        = "cv_MS:1002643_peptide_start_positions_on_chromosome";
    public static final String OPTIONAL_GENOME_REF_VERSION_COLUMN        = "cv_MS:1002644_genome_reference_version";
    public static final String OPTIONAL_PROTEIN_ACC_COLUMN = "proteinAccession";
    public static final String OPTIONAL_PROTEOGROUPER = "cv_MS:1002235_ProteoGrouper_PDH_score";
    public static final String OPTIONAL_PSM_FDRSCORE_COLUMN  = "cv_MS:1002356_PSM-level_combined_FDRScore";
    public static final String CUSTOM_CHROM_INF_PARAM = "Chromosome information present";

    /**
     * This function takes the value of a CVParam and retrieve the corresponding CVParam
     * @param cvParam A CvParam
     * @return CVParam for mzTab
     */
    public static CVParam convertCvParamToCVParam(CvParam cvParam){
        if(cvParam != null)
         return new CVParam(cvParam.getCvLookupID(),cvParam.getAccession(),removeNewLineAndTab(cvParam.getName()),removeNewLineAndTab(cvParam.getValue()));
        return null;
    }

    /**
     * This function takes the value of a CVParam and retrieve the corresponding CVParam
     * @param cvParam A CvParam
     * @return CVParam for mzTab
     */
    public static CVParam convertCvParamToCVParam(CvParam cvParam, Double mass){
        if(cvParam != null) {
            if (mass != null)
                return new CVParam(cvParam.getCvLookupID(), cvParam.getAccession(), removeNewLineAndTab(cvParam.getName()), mass.toString());
            else
               return convertCvParamToCVParam(cvParam);
        }
        return null;
    }

    public static List<CvParam> convertParamToCvParam(List<Param> params){
        List<CvParam> cvParamList = new ArrayList<CvParam>();
        if(params != null && params.size() > 0){
            for(Param param: params)
                cvParamList.add(MzTabUtils.convertParamToCvParam(param));
        }
        return cvParamList;
    }

    public static List<UserParam> convertStringListToUserParam(List<String> settingList) {
        List<UserParam> userParamList = new ArrayList<UserParam>();
        if(settingList != null && settingList.size() > 0){
            for(String setting: settingList)
                userParamList.add(MzTabUtils.convertStringToUserParam(setting));
        }
        return userParamList;
    }

    public static UserParam convertStringToUserParam(String value) {
        if(value != null && !value.isEmpty()){
            return new UserParam(null,null,value,null,null,null);
        }
        return null;
    }

    /**
     * This function takes the value of a UserParam and returns a corresponding CVParam
     * @param param User Param
     * @return Return a CVParam
     */
    public static CVParam convertUserParamToCVParam(UserParam param) {
        return new CVParam(null, null, param.getName(), param.getValue());
    }

    /**
     * Get the Search Engines Scores for a Proteins or Peptide
     * @return List of SearchEngine Score params
     */
    public static List<SearchEngineScoreParam> getSearchEngineScoreTerm(Score score) {
        List<SearchEngineScoreParam> scores = new ArrayList<SearchEngineScoreParam>();
        if (score != null)
            for (SearchEngineScoreCvTermReference term : score.getSearchEngineScoreCvTermReferenceWithValues())
                if(SearchEngineScoreParam.getSearchEngineScoreParamByAccession(term.getAccession()) != null)
                    scores.add(SearchEngineScoreParam.getSearchEngineScoreParamByAccession(term.getAccession()));
        return scores;
    }

    public static void addOptionalColumnValue(MZTabRecord record, MZTabColumnFactory columnFactory, String name, String value) {

        String header = OptionColumn.getHeader(null, name);
        MZTabColumn column = columnFactory.findColumnByHeader(header);

        String logicalPosition;

        if (column == null) {
            logicalPosition =  columnFactory.addOptionalColumn(name, String.class);
        }
        else {
            logicalPosition = column.getLogicPosition();
        }

        record.setValue(logicalPosition, value);
    }


    public static Constants.SpecIdFormat getSpectraDataIdFormat(SpectraData spectraData) {
        CvParam specIdFormat = spectraData.getSpectrumIdFormat();
        return getSpectraDataIdFormat(specIdFormat.getAccession());
    }

    //TODO: Extend support for Thermo, Waters, Bruker
    private static Constants.SpecIdFormat getSpectraDataIdFormat(String accession) {
        if (accession.equals("MS:1001528"))
            return Constants.SpecIdFormat.MASCOT_QUERY_NUM;
        if (accession.equals("MS:1000774"))
            return Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID;
        if (accession.equals("MS:1000775"))
            return Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID;
        if (accession.equals("MS:1001530"))
            return Constants.SpecIdFormat.MZML_ID;
        if (accession.equals("MS:1000776"))
            return Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID;
        if (accession.equals("MS:1000770"))
            return Constants.SpecIdFormat.WIFF_NATIVE_ID;
        if (accession.equals("MS:1000777"))
            return Constants.SpecIdFormat.MZDATA_ID;
        if(accession.equals(("MS:1000768")))
            return Constants.SpecIdFormat.SPECTRUM_NATIVE_ID;
        return Constants.SpecIdFormat.NONE;
    }

    public static Constants.SpecFileFormat getSpectraDataFormat(SpectraData spectraData) {
        uk.ac.ebi.pride.utilities.data.core.CvParam specFileFormat = spectraData.getFileFormat();
        if (specFileFormat != null) {
            if (specFileFormat.getAccession().equals("MS:1000613"))
                return Constants.SpecFileFormat.DTA;
            if (specFileFormat.getAccession().equals("MS:1001062"))
                return Constants.SpecFileFormat.MGF;
            if (specFileFormat.getAccession().equals("MS:1000565"))
                return Constants.SpecFileFormat.PKL;
            if (specFileFormat.getAccession().equals("MS:1000584") || specFileFormat.getAccession().equals("MS:1000562"))
                return Constants.SpecFileFormat.MZML;
            if (specFileFormat.getAccession().equals("MS:1000566"))
                return Constants.SpecFileFormat.MZXML;
        }
        return Constants.SpecFileFormat.NONE;
    }

    public static String getSpectrumId(SpectraData spectraData, String spectrumID) {

        Constants.SpecIdFormat fileIdFormat = getSpectraDataIdFormat(spectraData);

        if (fileIdFormat == Constants.SpecIdFormat.MASCOT_QUERY_NUM) {
            String rValueStr = spectrumID.replaceAll("query=", "");
            String id = null;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
            }
            return id;
        } else if (fileIdFormat == Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID) {
            String rValueStr = spectrumID.replaceAll("index=", "");
            String id = null;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
            }
            return id;
        } else if (fileIdFormat == Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
            return spectrumID.replaceAll("file=", "");
        } else if (fileIdFormat == Constants.SpecIdFormat.MZML_ID) {
            return spectrumID.replaceAll("mzMLid=", "");
        } else if (fileIdFormat == Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
            return spectrumID.replaceAll("scan=", "");
        } else {
            return spectrumID;
        }
    }

    public static String getOriginalSpectrumId(SpectraData spectraData, String spectrumID) {

        Constants.SpecIdFormat fileIdFormat = getSpectraDataIdFormat(spectraData);

        if (fileIdFormat == Constants.SpecIdFormat.MASCOT_QUERY_NUM) {
            return "query=" + spectrumID;
        } else if (fileIdFormat == Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID) {
            return "index=" + spectrumID;
        } else if (fileIdFormat == Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
            return "file=" + spectrumID;
        } else if (fileIdFormat == Constants.SpecIdFormat.MZML_ID) {
            return "mzMLid="+spectrumID;
        } else if (fileIdFormat == Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
            return "scan=" + spectrumID;
        } else {
            return spectrumID;
        }
    }


    public static CvParam convertParamToCvParam(Param param) {
        return new CvParam(param.getAccession(), removeNewLineAndTab(param.getName()),param.getCvLabel(),removeNewLineAndTab(param.getValue()),null,null,null);
    }

    /**
     * Param for the reagent to retrieve the PRIDE Term for that, is important that most of the fields can be null, for
     * that reason we look through all the the information looking for patterns.
     * Todo: Looks for an option to properly the information for quantitation.
     * @param quantificationReagent The quantitative Reagent Param
     * @return Return a CVParam for the mzTab Quant Parameter
     */
    public static CvParam parseQuantitationReagentCvParam(Param quantificationReagent) {

        if(quantificationReagent != null){
                if(quantificationReagent.getAccession() != null && QuantCvTermReference.getCvRefByAccession(quantificationReagent.getAccession()) != null){
                    QuantCvTermReference cvTerm = QuantCvTermReference.getCvRefByAccession(quantificationReagent.getAccession());
                    return new CvParam(cvTerm.getAccession(),cvTerm.getName(),cvTerm.getCvLabel(),quantificationReagent.getValue(),null,null,null);
                }else if(quantificationReagent.getAccession() != null && QuantCvTermReference.getReagentByShortAccessionLabel(quantificationReagent.getAccession()) != null){
                    QuantCvTermReference cvTerm = QuantCvTermReference.getReagentByShortAccessionLabel(quantificationReagent.getAccession());
                    return new CvParam(cvTerm.getAccession(),cvTerm.getName(),cvTerm.getCvLabel(),quantificationReagent.getValue(),null,null,null);
                }
        }
        return null;
    }

    /**
     * If there exists reserved characters in value, remove them all.
     *
     * @param value The Value of the MzTab Term
     * @return Return a value without tab
     */
    public static String removeNewLineAndTab(String value) {
        if (value != null) {
            value = value.trim();

            // define a reserved character list.
            List<String> reserveCharList = new ArrayList<String>();

            reserveCharList.add("\n");
            reserveCharList.add("\t");

            for (String c : reserveCharList) {
                value = value.replaceAll(c, " ");
            }
        }

        return value;
    }
}

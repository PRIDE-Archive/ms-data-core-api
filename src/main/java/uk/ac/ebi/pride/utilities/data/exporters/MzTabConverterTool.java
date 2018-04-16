package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.data.util.MassSpecFileFormat;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertProvider;
import uk.ac.ebi.pride.jmztab.utils.errors.*;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

import java.io.*;
import java.util.SortedMap;

/**
 * This code is licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>This is a simple converter tool that enables to convert mzIdentML to mzTab Files
 *
 * <p>
 *
 * <p>This class
 *
 * <p>Created by ypriverol (ypriverol@gmail.com) on 11/05/2017.
 */
public class MzTabConverterTool {

  private static final Logger log = LoggerFactory.getLogger(MzTabConverterTool.class);

  private MZTabErrorList errorList = new MZTabErrorList();
  private ConvertProvider convertProvider;

  public MzTabConverterTool(File inFile, MassSpecFileFormat format) {
    this(inFile, format, true, true);
  }

  // Allows load mzIdentML in memory or not (only for this case)
  public MzTabConverterTool(
      File inFile, MassSpecFileFormat format, boolean consistencyCheck, boolean mzIdentMLInMemory) {
    if (format == null) {
      throw new NullPointerException("Source file format is null");
    }
    DataAccessController controller = null;
    switch (format) {
      case PRIDE:
        controller = new PrideXmlControllerImpl(inFile);
        convertProvider = new PRIDEMzTabConverter(controller);
        break;
      case MZIDENTML:
        controller = new MzIdentMLControllerImpl(inFile);
        convertProvider = new MzIdentMLMzTabConverter((MzIdentMLControllerImpl) controller);
        break;
      default:
        throw new IllegalArgumentException("Can not convert " + format + " to mztab.");
    }
    if (consistencyCheck) {
      check(convertProvider.getMZTabFile());
    } else {
      convertProvider.getMZTabFile();
    }
  }

  /**
   * Use this constructor only to check the files without convert them first. The file needs to be
   * generated previously. This constructor will disappear in future versions.
   */
  public MzTabConverterTool() {}

  /**
   * Do whole {@link MZTabFile} consistency check.
   *
   * @see #checkMetadata(Metadata)
   * @see #checkProtein(Metadata, MZTabColumnFactory)
   * @see #checkPeptide(Metadata, MZTabColumnFactory)
   * @see #checkPSM(Metadata, MZTabColumnFactory)
   * @deprecated This method will be unified with the one in the parser and would be removed.
   */
  @Deprecated
  public void check(MZTabFile mzTabFile) {
    Metadata metadata = mzTabFile.getMetadata();
    MZTabColumnFactory proteinFactory = mzTabFile.getProteinColumnFactory();
    MZTabColumnFactory peptideFactory = mzTabFile.getPeptideColumnFactory();
    MZTabColumnFactory psmFactory = mzTabFile.getPsmColumnFactory();
    MZTabColumnFactory smlFactory = mzTabFile.getSmallMoleculeColumnFactory();

    checkMetadata(metadata);
    checkProtein(metadata, proteinFactory);
    checkPeptide(metadata, peptideFactory);
    checkPSM(metadata, psmFactory);
  }

  private void checkMetadata(Metadata metadata) {
    MZTabDescription.Mode mode = metadata.getMZTabMode();
    MZTabDescription.Type type = metadata.getMZTabType();

    SortedMap<Integer, StudyVariable> svMap = metadata.getStudyVariableMap();
    SortedMap<Integer, Assay> assayMap = metadata.getAssayMap();
    SortedMap<Integer, MsRun> runMap = metadata.getMsRunMap();

    if (mode == MZTabDescription.Mode.Complete) {
      if (metadata.getSoftwareMap().size() == 0) {
        errorList.add(
            new MZTabError(
                LogicalErrorType.NotDefineInMetadata,
                -1,
                "software[1-n]",
                mode.toString(),
                type.toString()));
      }

      if (type == MZTabDescription.Type.Quantification) {
        if (metadata.getQuantificationMethod() == null) {
          errorList.add(
              new MZTabError(
                  LogicalErrorType.NotDefineInMetadata,
                  -1,
                  "quantification_method",
                  mode.toString(),
                  type.toString()));
        }
        for (Integer id : assayMap.keySet()) {
          if (assayMap.get(id).getMsRun() == null) {
            errorList.add(
                new MZTabError(
                    LogicalErrorType.NotDefineInMetadata,
                    -1,
                    "assay[" + id + "]-ms_run_ref",
                    mode.toString(),
                    type.toString()));
          }
          if (assayMap.get(id).getQuantificationReagent() == null) {
            errorList.add(
                new MZTabError(
                    LogicalErrorType.NotDefineInMetadata,
                    -1,
                    "assay[" + id + "]-quantification_reagent",
                    mode.toString(),
                    type.toString()));
          }
        }
        if (svMap.size() > 0 && assayMap.size() > 0) {
          for (Integer id : svMap.keySet()) {
            if (svMap.get(id).getAssayMap().size() == 0) {
              errorList.add(
                  new MZTabError(
                      LogicalErrorType.AssayRefs, -1, "study_variable[" + id + "]-assay_refs"));
            }
          }
        }
      }
    }

    // Complete and Summary should provide following information.
    // mzTab-version, mzTab-mode and mzTab-type have default values in create metadata. Not check
    // here.
    if (metadata.getDescription() == null) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.NotDefineInMetadata,
              -1,
              "description",
              mode.toString(),
              type.toString()));
    }
    for (Integer id : runMap.keySet()) {
      if (runMap.get(id).getLocation() == null) {
        errorList.add(
            new MZTabError(
                LogicalErrorType.NotDefineInMetadata,
                -1,
                "ms_run[" + id + "]-location",
                mode.toString(),
                type.toString()));
      }
    }

    // mods
    // fixed
    if (metadata.getFixedModMap().size() == 0) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.NotDefineInMetadata,
              -1,
              "fixed_mod[1-n]",
              mode.toString(),
              type.toString()));
    }
    // variable
    if (metadata.getVariableModMap().size() == 0) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.NotDefineInMetadata,
              -1,
              "variable_mod[1-n]",
              mode.toString(),
              type.toString()));
    }

    if (type == MZTabDescription.Type.Quantification) {
      for (Integer id : svMap.keySet()) {
        if (svMap.get(id).getDescription() == null) {
          errorList.add(
              new MZTabError(
                  LogicalErrorType.NotDefineInMetadata,
                  -1,
                  "study_variable[" + id + "]-description",
                  mode.toString(),
                  type.toString()));
        }
      }
    }
  }

  private void refineOptionalColumn(
      MZTabDescription.Mode mode,
      MZTabDescription.Type type,
      MZTabColumnFactory factory,
      String columnHeader) {
    if (factory.findColumnByHeader(columnHeader) == null) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.NotDefineInHeader,
              -1,
              columnHeader,
              mode.toString(),
              type.toString()));
    }
  }

  private void checkProtein(Metadata metadata, MZTabColumnFactory proteinFactory) {
    if (proteinFactory == null) {
      return;
    }
    MZTabDescription.Mode mode = metadata.getMZTabMode();
    MZTabDescription.Type type = metadata.getMZTabType();
    // We check that protein_search_engine_score is defined
    if (metadata.getProteinSearchEngineScoreMap().size() == 0) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.ProteinSearchEngineScoreNotDefined,
              -1,
              "protein_search_engine_score[1-n]",
              mode.toString(),
              type.toString()));
    }
    // Mandatory in all modes
    for (SearchEngineScore searchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
      String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
      refineOptionalColumn(
          mode, type, proteinFactory, "best_search_engine_score" + searchEngineScoreLabel);
    }
    if (mode == MZTabDescription.Mode.Complete) {
      // Mandatory for all complete (Quantification and Identification)
      for (MsRun msRun : metadata.getMsRunMap().values()) {
        String msRunLabel = "_ms_run[" + msRun.getId() + "]";
        for (SearchEngineScore searchEngineScore :
            metadata.getProteinSearchEngineScoreMap().values()) {
          String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
          refineOptionalColumn(
              mode,
              type,
              proteinFactory,
              "search_engine_score" + searchEngineScoreLabel + msRunLabel);
        }
      }

      if (type == MZTabDescription.Type.Identification) {
        for (MsRun msRun : metadata.getMsRunMap().values()) {
          String msRunLabel = "_ms_run[" + msRun.getId() + "]";
          refineOptionalColumn(mode, type, proteinFactory, "num_psms" + msRunLabel);
          refineOptionalColumn(mode, type, proteinFactory, "num_peptides_distinct" + msRunLabel);
          refineOptionalColumn(mode, type, proteinFactory, "num_peptides_unique" + msRunLabel);
        }
      } else { // Quantification and Complete
        for (Assay assay : metadata.getAssayMap().values()) {
          String assayLabel = "_assay[" + assay.getId() + "]";
          refineOptionalColumn(mode, type, proteinFactory, "protein_abundance" + assayLabel);
        }
      }
    }

    if (type == MZTabDescription.Type.Quantification) { // Summary and Complete
      if (metadata.getProteinQuantificationUnit() == null) {
        errorList.add(
            new MZTabError(
                LogicalErrorType.NotDefineInMetadata,
                -1,
                "protein-quantification_unit",
                mode.toString(),
                type.toString()));
      }
      for (StudyVariable studyVariable : metadata.getStudyVariableMap().values()) {
        String svLabel = "_study_variable[" + studyVariable.getId() + "]";
        refineOptionalColumn(mode, type, proteinFactory, "protein_abundance" + svLabel);
        refineOptionalColumn(mode, type, proteinFactory, "protein_abundance_stdev" + svLabel);
        refineOptionalColumn(mode, type, proteinFactory, "protein_abundance_std_error" + svLabel);
      }
    }
  }

  private void checkPeptide(Metadata metadata, MZTabColumnFactory peptideFactory) {
    if (peptideFactory == null) {
      return;
    }
    MZTabDescription.Mode mode = metadata.getMZTabMode();
    MZTabDescription.Type type = metadata.getMZTabType();
    // peptide_search_engine_score
    if (metadata.getPeptideSearchEngineScoreMap().size() == 0) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.PeptideSearchEngineScoreNotDefined,
              -1,
              "peptide_search_engine_score[1-n]",
              mode.toString(),
              type.toString()));
    }

    if (type == MZTabDescription.Type.Quantification) {
      if (metadata.getPeptideQuantificationUnit() == null) {
        errorList.add(
            new MZTabError(
                LogicalErrorType.NotDefineInMetadata,
                -1,
                "peptide-quantification_unit",
                mode.toString(),
                type.toString()));
      }
      for (SearchEngineScore searchEngineScore :
          metadata.getPeptideSearchEngineScoreMap().values()) {
        String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
        refineOptionalColumn(
            mode, type, peptideFactory, "best_search_engine_score" + searchEngineScoreLabel);
      }

      for (StudyVariable studyVariable : metadata.getStudyVariableMap().values()) {
        String svLabel = "_study_variable[" + studyVariable.getId() + "]";
        refineOptionalColumn(mode, type, peptideFactory, "peptide_abundance" + svLabel);
        refineOptionalColumn(mode, type, peptideFactory, "peptide_abundance_stdev" + svLabel);
        refineOptionalColumn(mode, type, peptideFactory, "peptide_abundance_std_error" + svLabel);
      }
      if (mode == MZTabDescription.Mode.Complete) {
        for (MsRun msRun : metadata.getMsRunMap().values()) {
          String msRunLabel = "_ms_run[" + msRun.getId() + "]";
          for (SearchEngineScore searchEngineScore :
              metadata.getPeptideSearchEngineScoreMap().values()) {
            String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
            refineOptionalColumn(
                mode,
                type,
                peptideFactory,
                "search_engine_score" + searchEngineScoreLabel + msRunLabel);
          }
        }
        for (Assay assay : metadata.getAssayMap().values()) {
          String assayLabel = "_assay[" + assay.getId() + "]";
          refineOptionalColumn(mode, type, peptideFactory, "peptide_abundance" + assayLabel);
        }
      }
    }
  }

  private void checkPSM(Metadata metadata, MZTabColumnFactory psmFactory) {
    if (psmFactory == null) {
      return;
    }
    MZTabDescription.Mode mode = metadata.getMZTabMode();
    MZTabDescription.Type type = metadata.getMZTabType();
    // psm_search_engine_score
    if (metadata.getPsmSearchEngineScoreMap().size() == 0) {
      errorList.add(
          new MZTabError(
              LogicalErrorType.PSMSearchEngineScoreNotDefined,
              -1,
              "psm_search_engine_score[1-n]",
              mode.toString(),
              type.toString()));
    }
    // Mandatory in all modes
    for (SearchEngineScore searchEngineScore : metadata.getPsmSearchEngineScoreMap().values()) {
      String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
      refineOptionalColumn(mode, type, psmFactory, "search_engine_score" + searchEngineScoreLabel);
    }
  }

  public static MassSpecFileFormat getFormat(String format) {
    if (MZTabUtils.isEmpty(format)) {
      return null;
    }

    if (format.equalsIgnoreCase(MassSpecFileFormat.PRIDE.name())) {
      return MassSpecFileFormat.PRIDE;
    } else if (format.equalsIgnoreCase(MassSpecFileFormat.MZIDENTML.name())) {
      return MassSpecFileFormat.MZIDENTML;
    } else {
      return MassSpecFileFormat.PRIDE;
    }
  }

  public static void printErrorCode(CommandLine cmd) {
    MZTabErrorTypeMap typeMap = new MZTabErrorTypeMap();
    MZTabErrorType type;
    if (cmd.hasOption(ARG_CODE)) {
      String[] values = cmd.getOptionValues(ARG_CODE);
      Integer code = new Integer(values[0]);
      type = typeMap.getType(code);
      if (type == null) {
        log.info("Not found MZTabErrorType, the code is :" + code);
      } else {
        log.info("" + type);
      }
    }
  }

  public static void check(CommandLine cmd) throws Exception {
    File outFile = null;
    if (cmd.hasOption(ARG_OUTPUTFILE)) {
      outFile = new File(cmd.getOptionValue(ARG_OUTPUTFILE));
    }
    OutputStream out =
        (outFile == null) ? System.out : new BufferedOutputStream(new FileOutputStream(outFile));
    MZTabErrorType.Level level = MZTabErrorType.Level.Info;
    if (cmd.hasOption(ARG_LEVEL)) {
      level = MZTabErrorType.findLevel(cmd.getOptionValue(ARG_LEVEL));
    }
    String inputFilePath = cmd.getOptionValue(ARG_INPUTFILE);
    if (inputFilePath == null) {
      throw new IllegalArgumentException("Not setting input file!");
    }
    File inFile = new File(inputFilePath.trim());
    log.info("Begin checking mztab file: " + inFile.getAbsolutePath());
    MZTabFileParser mzTabFileParser = new MZTabFileParser(inFile, out, level);
    MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();
    log.debug(mzTabFile.toString());
    if (out != System.out) {
      out.close();
    }
  }

  public static void convert(CommandLine cmd) throws Exception {
    File inFile;
    File outFile = null;
    if (cmd.hasOption(ARG_OUTPUTFILE)) {
      outFile = new File(cmd.getOptionValue(ARG_OUTPUTFILE));
    }
    OutputStream out =
        (outFile == null) ? System.out : new BufferedOutputStream(new FileOutputStream(outFile));

    String inputFilePath = cmd.getOptionValue(ARG_INPUTFILE);
    if (inputFilePath == null) {
      throw new IllegalArgumentException("Not setting input file!");
    }

    String fileFormat = cmd.getOptionValue(ARG_FORMAT);
    if (fileFormat == null) {
      throw new IllegalArgumentException("Not setting format of the file!");
    }
    inFile = new File(inputFilePath.trim());
    MassSpecFileFormat format = getFormat(fileFormat.trim());
    log.info(
        "Begin converting "
            + inFile.getAbsolutePath()
            + " which format is "
            + (format != null ? format.name() : "")
            + " to mztab file.");
    MZTabFile tabFile = convert(inFile, format);
    MZTabFileConverter checker = new MZTabFileConverter();
    checker.check(tabFile);
    if (checker.getErrorList().isEmpty()) {
      log.info("Begin writing mztab file.");
      tabFile.printMZTab(out);
    } else {
      log.error("There are errors in mztab file.");
      checker.getErrorList().print(out);
    }
    if (out != System.out) {
      out.close();
    }
  }

  private static MZTabFile convert(File inFile, MassSpecFileFormat format) {
    MZTabFile resultFile;
    if (format == null) {
      throw new NullPointerException("Source file format is null");
    } else {
      switch (format) {
        case PRIDE:
          resultFile = (new PRIDEMzTabConverter(new PrideXmlControllerImpl(inFile))).getMZTabFile();
          break;
        case MZIDENTML:
          resultFile =
              (new MzIdentMLMzTabConverter(new MzIdentMLControllerImpl(inFile, true)))
                  .getMZTabFile();
          break;
        default:
          throw new IllegalArgumentException("Can not convert " + format + " to mztab.");
      }
    }
    return resultFile;
  }
}

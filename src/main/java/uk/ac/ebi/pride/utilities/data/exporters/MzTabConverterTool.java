package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.cli.*;
import uk.ac.ebi.pride.data.util.MassSpecFileFormat;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertMZidentMLFile;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertPrideXMLFile;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertProvider;
import uk.ac.ebi.pride.jmztab.utils.errors.*;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.SortedMap;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * This is a simple converter tool that enables to convert mzIdentML to mzTab Files
 *
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 11/05/2017.
 */
public class MzTabConverterTool {


    private MZTabErrorList errorList = new MZTabErrorList();
    private ConvertProvider convertProvider;

    public MzTabConverterTool(File inFile, MassSpecFileFormat format) {
        this(inFile, format, true, true);
    }

    //Allows load mzIdentML in memory or not (only for this case)
    public MzTabConverterTool(File inFile, MassSpecFileFormat format, boolean consistencyCheck, boolean mzIdentMLInMemory) {
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

        if(consistencyCheck)
            check(convertProvider.getMZTabFile());
        else
            convertProvider.getMZTabFile();
    }

    /**
     * Use this constructor only to check the files without convert them first. The file needs to be generated previously.
     * This constructor will disappear in future versions.
     */
    public MzTabConverterTool() {
    }

    /**
     * Do whole {@link MZTabFile} consistency check.
     *
     * @see #checkMetadata(Metadata)
     * @see #checkProtein(Metadata, MZTabColumnFactory)
     * @see #checkPeptide(Metadata, MZTabColumnFactory)
     * @see #checkPSM(Metadata, MZTabColumnFactory)
     *
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
                errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "software[1-n]", mode.toString(), type.toString()));
            }

            if (type == MZTabDescription.Type.Quantification) {
                if (metadata.getQuantificationMethod() == null) {
                    errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "quantification_method", mode.toString(), type.toString()));
                }
                for (Integer id : assayMap.keySet()) {
                    if (assayMap.get(id).getMsRun() == null) {
                        errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "assay[" + id + "]-ms_run_ref", mode.toString(), type.toString()));
                    }
                    if (assayMap.get(id).getQuantificationReagent() == null) {
                        errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "assay[" + id + "]-quantification_reagent", mode.toString(), type.toString()));
                    }
                }
                if (svMap.size() > 0 && assayMap.size() > 0) {
                    for (Integer id : svMap.keySet()) {
                        if (svMap.get(id).getAssayMap().size() == 0) {
                            errorList.add(new MZTabError(LogicalErrorType.AssayRefs, -1, "study_variable[" + id + "]-assay_refs"));
                        }
                    }
                }
            }
        }

        // Complete and Summary should provide following information.
        // mzTab-version, mzTab-mode and mzTab-type have default values in create metadata. Not check here.
        if (metadata.getDescription() == null) {
            errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "description", mode.toString(), type.toString()));
        }
        for (Integer id : runMap.keySet()) {
            if (runMap.get(id).getLocation() == null) {
                errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "ms_run[" + id + "]-location", mode.toString(), type.toString()));
            }
        }

        //mods
        //fixed
        if (metadata.getFixedModMap().size() == 0) {
            errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "fixed_mod[1-n]", mode.toString(), type.toString()));
        }
        //variable
        if (metadata.getVariableModMap().size() == 0) {
            errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "variable_mod[1-n]", mode.toString(), type.toString()));
        }

        if (type == MZTabDescription.Type.Quantification) {
            for (Integer id : svMap.keySet()) {
                if (svMap.get(id).getDescription() == null) {
                    errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "study_variable[" + id + "]-description", mode.toString(), type.toString()));
                }
            }
        }
    }

    private void refineOptionalColumn(MZTabDescription.Mode mode, MZTabDescription.Type type,
                                      MZTabColumnFactory factory, String columnHeader) {
        if (factory.findColumnByHeader(columnHeader) == null) {
            errorList.add(new MZTabError(LogicalErrorType.NotDefineInHeader, -1, columnHeader, mode.toString(), type.toString()));
        }
    }

    private void checkProtein(Metadata metadata, MZTabColumnFactory proteinFactory) {
        if (proteinFactory == null) {
            return;
        }

        MZTabDescription.Mode mode = metadata.getMZTabMode();
        MZTabDescription.Type type = metadata.getMZTabType();

        //We check that protein_search_engine_score is defined
        if (metadata.getProteinSearchEngineScoreMap().size() == 0) {
            errorList.add(new MZTabError(LogicalErrorType.ProteinSearchEngineScoreNotDefined, -1, "protein_search_engine_score[1-n]", mode.toString(), type.toString()));
        }

        //Mandatory in all modes
        for (SearchEngineScore searchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
            String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
            refineOptionalColumn(mode, type, proteinFactory, "best_search_engine_score" + searchEngineScoreLabel);
        }

        if (mode == MZTabDescription.Mode.Complete) {

            //Mandatory for all complete (Quantification and Identification)
            for (MsRun msRun : metadata.getMsRunMap().values()) {
                String msRunLabel = "_ms_run[" + msRun.getId() + "]";
                for (SearchEngineScore searchEngineScore : metadata.getProteinSearchEngineScoreMap().values()) {
                    String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
                    refineOptionalColumn(mode, type, proteinFactory, "search_engine_score" + searchEngineScoreLabel + msRunLabel);
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

        if (type == MZTabDescription.Type.Quantification) { //Summary and Complete
            if (metadata.getProteinQuantificationUnit() == null) {
                errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "protein-quantification_unit", mode.toString(), type.toString()));
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


        //peptide_search_engine_score
        if (metadata.getPeptideSearchEngineScoreMap().size() == 0) {
            errorList.add(new MZTabError(LogicalErrorType.PeptideSearchEngineScoreNotDefined, -1, "peptide_search_engine_score[1-n]", mode.toString(), type.toString()));
        }

        if (type == MZTabDescription.Type.Quantification) {
            if (metadata.getPeptideQuantificationUnit() == null) {
                errorList.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, "peptide-quantification_unit", mode.toString(), type.toString()));
            }
            for (SearchEngineScore searchEngineScore : metadata.getPeptideSearchEngineScoreMap().values()) {
                String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
                refineOptionalColumn(mode, type, peptideFactory, "best_search_engine_score" + searchEngineScoreLabel);
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
                    for (SearchEngineScore searchEngineScore : metadata.getPeptideSearchEngineScoreMap().values()) {
                        String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
                        refineOptionalColumn(mode, type, peptideFactory, "search_engine_score" + searchEngineScoreLabel + msRunLabel);
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

        //psm_search_engine_score
        if (metadata.getPsmSearchEngineScoreMap().size() == 0) {
            errorList.add(new MZTabError(LogicalErrorType.PSMSearchEngineScoreNotDefined, -1, "psm_search_engine_score[1-n]", mode.toString(), type.toString()));
        }

        //Mandatory in all modes
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

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        MZTabErrorTypeMap typeMap = new MZTabErrorTypeMap();

        // Definite command line
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "print help message");

        String msgOpt = "message";
        String codeOpt = "code";
        Option msgOption = OptionBuilder.withArgName(codeOpt)
                .hasArgs(2)
                .withValueSeparator()
                .withDescription("print Error/Warn detail message based on code number.")
                .create(msgOpt);
        options.addOption(msgOption);

        String outOpt = "outFile";
        options.addOption(outOpt, true, "Record error/warn messages into outfile. If not set, print message on the screen. ");

        String checkOpt = "check";
        String inFileOpt = "inFile";
        Option checkOption = OptionBuilder.withArgName(inFileOpt)
                .hasArgs(2)
                .withValueSeparator()
                .withDescription("Choose a file from input directory. This parameter should not be null!")
                .create(checkOpt);
        options.addOption(checkOption);

        String levelOpt = "level";
        options.addOption(levelOpt, true, "Choose validate level(Info, Warn, Error), default level is Error!");

        String convertOpt = "convert";
        String formatOpt = "format";
        Option convertOption = OptionBuilder.withArgName(inFileOpt + ", " + formatOpt)
                .hasArgs()
                .withValueSeparator()
                .withDescription("Converts the given format file (PRIDE or MZIDENTML) to an mztab file.")
                .create(convertOpt);
        options.addOption(convertOption);

        // Parse command line
        CommandLine line = parser.parse(options, args);
        if (line.hasOption(helpOpt)) {
            //HelpFormatter formatter = new HelpFormatter();
            //formatter.printHelp("jmztab", options);
            printHelpHack();
        } else if (line.hasOption(msgOpt)) {
            String[] values = line.getOptionValues(msgOpt);
            Integer code = new Integer(values[1]);
            MZTabErrorType type = typeMap.getType(code);

            if (type == null) {
                System.out.println("Not found MZTabErrorType, the code is :" + code);
            } else {
                System.out.println(type);
            }
        } else {

            File outFile = null;
            if (line.hasOption(outOpt)) {
                outFile = new File(line.getOptionValue(outOpt));
            }

            OutputStream out = outFile == null ? System.out : new BufferedOutputStream(new FileOutputStream(outFile));

            MZTabErrorType.Level level = MZTabErrorType.Level.Error;
            if (line.hasOption(levelOpt)) {
                level = MZTabErrorType.findLevel(line.getOptionValue(levelOpt));
            }

            if (line.hasOption(checkOpt)) {
                String[] values = line.getOptionValues(checkOpt);
                if (values.length != 2) {
                    throw new IllegalArgumentException("Not setting input file!");
                }
                File inFile = new File(values[1].trim());
                System.out.println("Begin check mztab file: " + inFile.getAbsolutePath());
                new MZTabFileParser(inFile, out, level);
            } else if (line.hasOption(convertOpt)) {
                String[] values = line.getOptionValues(convertOpt);
                File inFile = null;
                MassSpecFileFormat format = MassSpecFileFormat.PRIDE;
                for (int i = 0; i < values.length; i++) {
                    String type = values[i++].trim();
                    String value = values[i].trim();
                    if (type.equals(inFileOpt)) {
                        inFile = new File(value.trim());
                    } else if (type.equals(formatOpt)) {
                        format = getFormat(value.trim());
                    }
                }
                if (inFile == null) {
                    throw new IllegalArgumentException("Not setting input file!");
                }

                System.out.println("Begin converting " + inFile.getAbsolutePath() + " which format is " + format.name() + " to mztab file.");
                MZTabFile tabFile = convert(inFile, format);
                MZTabFileConverter checker = new MZTabFileConverter();
                checker.check(tabFile);
                if (checker.getErrorList().isEmpty()) {
                    System.out.println("Begin writing mztab file.");
                    tabFile.printMZTab(out);
                } else {
                    System.out.println("There are errors in mztab file.");
                    checker.getErrorList().print(out);
                }
            }

            System.out.println("Finish!");
            System.out.println();
            out.close();
        }
    }

    private static MZTabFile convert(File inFile, MassSpecFileFormat format) {
        MZTabFile resultFile;
            if(format == null) {
                throw new NullPointerException("Source file format is null");
            } else {
                switch (format) {
                    case PRIDE:
                        resultFile = (new PRIDEMzTabConverter(new PrideXmlControllerImpl(inFile))).getMZTabFile();
                        break;
                    case MZIDENTML:
                        resultFile =  (new MzIdentMLMzTabConverter(new MzIdentMLControllerImpl(inFile, true))).getMZTabFile();
                        break;
                    default:
                        throw new IllegalArgumentException("Can not convert " + format + " to mztab.");
                }
            }
        return resultFile;
    }

    /**
     * TODO
     * WARNING - NOTICE We needed the help message coming from the tool to give the user actual working parameters on
     * how to use this command line tool. The problem is that this package is using a really old version of commons-cli
     * from Apache, where OptionBuilder has been deprecated (to my understanding after some time browsing documentation
     * on the internet), and the new versions of commons-cli are in mvnrepository, instead of Maven Central. This means
     * the package needs a lot more detailed/deeper work regarding command line options, thus, having into account that
     * I only need the help message to match the wiki, which contains working instructions, and nothing else is being
     * added to this software in this iteration, plus the fact that we are in the process of reorganizing our maven
     * infrastructure, the problem with apache commons-cli will be addressed later on when either an extension is being
     * implemented or deployment integration works are being carried out on this package regarding our new infrastructure.
     */
    private static void printHelpHack() {
        System.out.println("usage: java -cp ms-data-core-api.jar uk.ac.ebi.pride.utilities.data.exporters.MzTabConverterTool\n" +
                " -check inFile=<inFile>                     Choose a file from input directory. This\n" +
                "                                            parameter should not be null!\n" +
                " -convert inFile=<inFile> format=<format>   Converts the given format file (PRIDE or MZIDENTML) to an mztab\n" +
                "                                            file.\n" +
                " -h,--help                                  print help message\n" +
                " -message code=<code>                       print Error/Warn detail message based on code\n" +
                "                                            number.\n" +
                " -outFile <arg>                             Dump output data to the given file. If\n" +
                "                                            not set, output data will be dumped on stdout");
    }



}

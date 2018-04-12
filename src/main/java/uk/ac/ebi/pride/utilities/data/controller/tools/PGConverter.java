package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.exporters.MzTabConverterTool;

import java.util.*;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

/**
 * This is the main class for the tool, which parses command line arguments and starts validating files.
 *
 * @author Tobias Ternent
 */
public class PGConverter {
    private static final Logger log = LoggerFactory.getLogger(PGConverter.class);

    /**
     * Main class that gets run. Parses command line arguments, starts either the converter or validation operations.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting application...");
        log.info("Program arguments: " + Arrays.toString(args));
        try {
            CommandLine cmd = PGConverter.parseArgs(args);
            if (args.length > 0) {
                if (cmd.hasOption(ARG_HELP)) {
                    printHelpHack();
                }else  if (cmd.hasOption(ARG_CODE)) {
                  MzTabConverterTool.printErrorCode(cmd);
                }else if (cmd.hasOption(ARG_VALIDATION)) {
                    Validator.startValidation(cmd);
                } else if (cmd.hasOption(ARG_CONVERSION)) {
                    Converter.startConversion(cmd);
                } else if (cmd.hasOption(ARG_MESSAGE)) {
                    Messenger.handleMessages(cmd);
                } else if (cmd.hasOption(ARG_CHECK)) { // // TODO: later, better to integrate this into ARG_VALIDATION option
                    MzTabConverterTool.check(cmd);
                } else if (cmd.hasOption(ARG_CONVERT)) { // TODO: later, better to integrate this into ARG_CONVERSION option
                    MzTabConverterTool.convert(cmd);
                } else {
                    log.error("Did not find validation, conversion, or messaging mode arguments.");
                    Arrays.stream(args).forEach(log::error);
                }
            }
            exitCleanly(cmd);
        } catch (Exception exception) {
            exitedUnexpectedly(exception);
        }
    }

    /**
     * This method parses sets up and all the command line arguments to a CommandLine object.
     *
     * @param args the command line arguments.
     * @return a CommandLine object of the parsed command line arguments.
     * @throws ParseException if there are problems parsing the command line arguments.
     */
    public static CommandLine parseArgs(String[] args) throws ParseException{
        Options options = new Options();
        options.addOption(ARG_VALIDATION, false, "start to validate a file");
        options.addOption(ARG_CONVERSION, false, "start to convert a file");
        options.addOption(ARG_MESSAGE, false, "start to message redis");
        options.addOption(ARG_MZID, true, "mzid file");
        options.addOption(ARG_PEAK, true, "peak file");
        options.addOption(ARG_PEAKS, true, "peak files");
        options.addOption(ARG_PRIDEXML, true, "pride xml file");
        options.addOption(ARG_MZTAB, true, "mztab file");
        options.addOption(ARG_PROBED, true, "probed file");
        options.addOption(ARG_OUTPUTFILE, true, "exact output file");
        options.addOption(ARG_OUTPUTTFORMAT, true, "exact output file format");
        options.addOption(ARG_INPUTFILE, true, "exact input file");
        options.addOption(ARG_CHROMSIZES, true, "chrom sizes file");
        options.addOption(ARG_REPORTFILE, true, "report file");
        options.addOption(ARG_REDIS, false, "Will message redis");
        options.addOption(ARG_REDIS_SERVER, true, "Redis server");
        options.addOption(ARG_REDIS_PORT, true, "Redis port");
        options.addOption(ARG_REDIS_PASSWORD, true, "Redis password");
        options.addOption(ARG_REDIS_CHANNEL, true, "Redis channel");
        options.addOption(ARG_REDIS_MESSAGE, true, "Redis message");
        options.addOption(ARG_SKIP_SERIALIZATION, false, "Redis message");
        options.addOption(ARG_SCHEMA_VALIDATION, false, "XML Schema validation");
        options.addOption(ARG_SCHEMA_ONLY_VALIDATION, false, "XML Schema-only validation");
        options.addOption(ARG_BED_COLUMN_FORMAT, true, "BED column format");
        options.addOption(ARG_HELP, false, "print help message");
        options.addOption(ARG_CODE, false, "print Error/Warn detail message based on code number.");
        options.addOption(ARG_LEVEL, true, "Choose validate level(Info, Warn, Error), default level is Error!");
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }


    //TODO: Complete this
        private static void printHelpHack() {
        System.out.println("usage: java -cp ms-data-core-api.jar uk.ac.ebi.pride.utilities.data.exporters.MzTabConverterTool\n" +
                " -check inputfile=<inputfile>                     Choose a file from input directory. This\n" +
                "                                                  parameter should not be null!\n" +
                " -convert inputfile=<inputfile> " +
                "          format=<format>   Converts the given format file (PRIDE or MZIDENTML) to an mztab\n" +
                "                                            file.\n" +
                " -h,--help                                  print help message\n" +
                " -message code=<code>                       print Error/Warn detail message based on code\n" +
                "                                            number.\n" +
                " -outputFile <arg>                          Dump output data to the given file. If\n" +
                "                                            not set, output data will be dumped on stdout");
    }
}

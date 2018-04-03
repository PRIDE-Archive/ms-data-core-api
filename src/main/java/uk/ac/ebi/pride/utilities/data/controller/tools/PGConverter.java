package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility;

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
                if (cmd.hasOption(ARG_VALIDATION)) {
                    Validator.startValidation(cmd);
                } else if (cmd.hasOption(ARG_CONVERSION)) {
                    Converter.startConversion(cmd);
                } else if (cmd.hasOption(ARG_MESSAGE)) {
                    if (cmd.hasOption(ARG_REDIS) && cmd.hasOption(ARG_REDIS_SERVER) && cmd.hasOption(ARG_REDIS_PORT) && cmd.hasOption(ARG_REDIS_CHANNEL) && cmd.hasOption(ARG_REDIS_MESSAGE)) {
                        Utility.notifyRedisChannel(cmd.getOptionValue(ARG_REDIS_SERVER), cmd.getOptionValue(ARG_REDIS_PORT),
                                cmd.hasOption(ARG_REDIS_PASSWORD) ? cmd.getOptionValue(ARG_REDIS_PASSWORD) : "", cmd.getOptionValue(ARG_REDIS_CHANNEL), cmd.getOptionValue(ARG_REDIS_MESSAGE));
                    } else {
                        log.error("Insufficient parameters provided for sending Redis message.");
                        Arrays.stream(args).forEach(log::error);
                    }
                } else {
                    log.error("Did not find validation, conversion, or messaging mode arguments.");
                    Arrays.stream(args).forEach(log::error);
                }
            }
            Utility.exitCleanly(cmd);
        } catch (Exception e) {
            exitedUnexpectedly(e);
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
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
}

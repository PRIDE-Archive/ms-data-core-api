package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

/**
 * This class create for handling methods related with redis messages.
 */
public class Messenger {

  private static final Logger log = LoggerFactory.getLogger(Messenger.class);

  /**
   * This method notifies to the redis channel
   *
   * @param cmd Command-line arguments
   * @return Return false if any of the parameters is missing in the command-line arguments
   */
  public static boolean handleMessages(CommandLine cmd) {
    boolean isValid = true;
    if (hasRedisOptions(cmd)) {
      notifyRedisChannel(
              cmd.getOptionValue(ARG_REDIS_SERVER),
              cmd.getOptionValue(ARG_REDIS_PORT),
              cmd.hasOption(ARG_REDIS_PASSWORD) ? cmd.getOptionValue(ARG_REDIS_PASSWORD) : "",
              cmd.getOptionValue(ARG_REDIS_CHANNEL), cmd.getOptionValue(ARG_REDIS_MESSAGE));
    } else {
      isValid = false;
      log.error("Insufficient parameters provided for sending Redis message.");
    }
    return isValid;
  }

  /**
   * Check if all the required redis parameters provided as command-line arguments
   *
   * @param cmd Command-line arguments
   * @return If all the parameters are provided, return true, otherwise false
   */
  private static boolean hasRedisOptions(CommandLine cmd) {
    return cmd.hasOption(ARG_REDIS) &&
            cmd.hasOption(ARG_REDIS_SERVER) &&
            cmd.hasOption(ARG_REDIS_PORT) &&
            cmd.hasOption(ARG_REDIS_CHANNEL) &&
            cmd.hasOption(ARG_REDIS_MESSAGE);
  }
}
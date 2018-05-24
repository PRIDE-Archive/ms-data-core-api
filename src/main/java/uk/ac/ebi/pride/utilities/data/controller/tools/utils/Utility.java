package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

import java.util.*;

import static redis.clients.jedis.Protocol.DEFAULT_TIMEOUT;

/**
 * This class provides general utilities for the PGConverter tool, such as:
 * arguments, supported file types, Redis messaging, and handling exiting the application.
 *
 * @author Tobias Ternent
 */
public class Utility {
  private static final Logger log = LoggerFactory.getLogger(Utility.class);

  // Main functionality
  public static final String ARG_VALIDATION = "v";
  public static final String ARG_CONVERSION = "c";
  public static final String ARG_MESSAGE = "m";
  public static final String ARG_CHECK = "check";
  public static final String ARG_CONVERT= "convert";
  public static final String ARG_ERROR_CODE = "error";
  public static final String ARG_HELP = "h";
  //Command line arguments arguments
  public static final String ARG_MZID = "mzid";
  public static final String ARG_PEAK = "peak";
  public static final String ARG_PEAKS = "peaks";
  public static final String ARG_PRIDEXML = "pridexml";
  public static final String ARG_MZTAB = "mztab";
  public static final String ARG_PROBED = "probed";
  public static final String ARG_BIGBED = "bigbed";
  public static final String ARG_OUTPUTFILE = "outputfile";
  public static final String ARG_INPUTFILE = "inputfile";
  public static final String ARG_OUTPUTTFORMAT = "outputformat";
  public static final String ARG_CHROMSIZES = "chromsizes";
  public static final String ARG_ASQLFILE = "asqlfile";
  public static final String ARG_ASQLNAME = "asqlname";
  public static final String ARG_BIGBEDCONVERTER = "bigbedconverter";
  public static final String ARG_REPORTFILE = "reportfile";
  public static final String ARG_REDIS = "redis";
  public static final String ARG_REDIS_SERVER = "redisserver";
  public static final String ARG_REDIS_PORT = "redisport";
  public static final String ARG_REDIS_PASSWORD = "redispassword";
  public static final String ARG_REDIS_CHANNEL = "redischannel";
  public static final String ARG_REDIS_MESSAGE = "redismessage";
  public static final String ARG_SKIP_SERIALIZATION = "skipserialization";
  public static final String ARG_SCHEMA_VALIDATION = "schema";
  public static final String ARG_SCHEMA_ONLY_VALIDATION = "schemaonly";
  public static final String ARG_BED_COLUMN_FORMAT = "columnformat";
  public static final String ARG_LEVEL = "level";
  public static final String ARG_FORMAT = "format";
  public static final String ARG_CODE = "code";
  public static final String ARG_FAST_VALIDATION = "fastvalidation";
  // Other constants
  public static final String MS_INSTRUMENT_MODEL_NAME = "instrument model";
  public static final String MS_INSTRUMENT_MODEL_AC = "MS:1000031";
  public static final String MS_SOFTWARE_AC = "MS:1000531";
  public static final String MS_CONTACT_EMAIL_AC = "MS:1000589";
  public static final String MS_SOFTWARE_NAME = "software";
  public static final String STRING_SEPARATOR = "##";

  /**
   * The supported file types.
   */
  public enum FileType {
    MZID("mzid"),
    MZTAB("mztab"),
    PRIDEXML("xml"),
    ASQL("as"),
    PROBED("pro.bed"),
    BIGBED("bb"),
    UNKNOWN("");

    private String format;

    FileType(String format) {
      this.format = format;
    }

    public String toString() {
      return format;
    }
  }

  /**
   * The supported BED Data types in an ASQL file
   */
  public enum AsqlDataType {
    STRING("string"),
    INT("int"),
    UINT("uint"),
    CHAR_ONE("char[1]"),
    INT_BLOCKCOUNT("int[blockCount]"),
    DOUBLE("double");

    private String format;

    AsqlDataType(String format) {
      this.format = format;
    }

    public String toString() {
      return format;
    }
  }

  /**
   * Handles exiting cleanly from the tool, and potentially messages Redis if set.
   *
   * @param cmd command line arguments.
   */
  public static void exitCleanly(CommandLine cmd) {
    if (cmd.hasOption(ARG_REDIS)) {
      notifyRedisChannel(
              cmd.getOptionValue(ARG_REDIS_SERVER),
              cmd.getOptionValue(ARG_REDIS_PORT),
              cmd.hasOption(ARG_REDIS_PASSWORD) ? cmd.getOptionValue(ARG_REDIS_PASSWORD) : "",
              cmd.getOptionValue(ARG_REDIS_CHANNEL), cmd.getOptionValue(ARG_REDIS_MESSAGE));
    }
    log.info("Exiting application.");
  }

  /**
   * Handles exiting unexpectedly from the tool.
   *
   * @param e Caught exception during processing
   */
  public static void exitedUnexpectedly(Exception e) {
    log.error("Exception while processing files: ", e);
    System.exit(-1);
  }

  /**
   * Publishes a success or failure message to the specified Redis channel according to the credentials used.
   *
   * @param jedisServer   the Redis server name.
   * @param jedisPort     the Redis server port.
   * @param jedisPassword the Redis password
   * @param assayChannel  the Redis channel to post a message to.
   * @param message       the message content.
   */
  public static void notifyRedisChannel(String jedisServer, String jedisPort, String jedisPassword, String assayChannel, String message) {
    try {
      log.info("Connecting to Redis channel:" + assayChannel);
      Set<HostAndPort> jedisClusterNodes = new HashSet<>();
      if (jedisServer.contains(STRING_SEPARATOR)) {
        String[] servers = jedisServer.split(STRING_SEPARATOR);
        String[] ports;
        if (jedisPort.contains(STRING_SEPARATOR)) {
          ports = jedisPort.split(STRING_SEPARATOR);
        } else {
          ports = new String[]{jedisPort};
        }
        if (ports.length != 1 && ports.length != servers.length) {
          log.error("Mismatch between provided Redis ports and servers. Should either have 1 port for all servers, or 1 port per server");
        }
        for (int i = 0; i < servers.length; i++) {
          String serverPort = ports.length == 1 ? ports[0] : ports[i];
          jedisClusterNodes.add(new HostAndPort(servers[i], Integer.parseInt(serverPort)));
          log.info("Added Jedis node: " + servers[i] + " " + serverPort);
        }
      } else {
        jedisClusterNodes.add(new HostAndPort(jedisServer, Integer.parseInt(jedisPort))); //Jedis Cluster will attempt to discover cluster nodes automatically
        log.info("Added Jedis node: " + jedisServer + " " + jedisPort);
      }
      final int DEFAULT_REDIRECTIONS = 5;
      final JedisPoolConfig DEFAULT_CONFIG = new JedisPoolConfig();
      JedisCluster jedisCluster = StringUtils.isNotEmpty(jedisPassword) ?
              new JedisCluster(jedisClusterNodes, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_REDIRECTIONS, jedisPassword, DEFAULT_CONFIG) :
              new JedisCluster(jedisClusterNodes, DEFAULT_CONFIG);
      log.info("Publishing message to Redis: , " + message);
      jedisCluster.publish(assayChannel, message);
      log.info("Published message to Redis, closing connection");
      jedisCluster.close();
    } catch (Exception e) {
      log.error("Exception while publishing message to Redis channel.", e);
    }
  }

  /**
   * This method outputs the cache sizes for debugging purposes.
   *
   * @param cachedDataAccessController the data access controller for the assay file
   */
  private static void calcCacheSizses(CachedDataAccessController cachedDataAccessController) {
    Arrays.stream(CacheEntry.values()).forEach(cacheEntry -> log.debug("Cache entry: " + cacheEntry.name() + " Size: " + (
            (cachedDataAccessController.getCache().get(cacheEntry) == null ?
                    "null" :
                    cachedDataAccessController.getCache().get(cacheEntry) instanceof Map ?
                            ((Map) cachedDataAccessController.getCache().get(cacheEntry)).size() :
                            cachedDataAccessController.getCache().get(cacheEntry) instanceof Collection ?
                                    ((Collection) cachedDataAccessController.getCache().get(cacheEntry)).size() :
                                    "null"))));
  }
}
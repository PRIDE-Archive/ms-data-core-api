package uk.ac.ebi.pride.utilities.data.controller.tools;

import net.ishiis.redis.unit.RedisCluster;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility;


import java.util.LinkedList;
import java.util.List;

/**
 * This class tests the messaging functionality, which is using Redis cluster.
 */
public class MessengerTest {

  private static final Logger log = LoggerFactory.getLogger(MessengerTest.class);

  private RedisCluster cluster;
  private List<String> hosts;
  private Integer[] ports;

  /**
   * Sets up the Redis test cluster using pre-defined ports.
   */
  @Before
  public void setup() {
    String localhost = "127.0.0.1"; // pre-defined, running locally
    hosts = new LinkedList<>(); // 3 instances pre-defined
    hosts.add(localhost);
    hosts.add(localhost);
    hosts.add(localhost);
    ports = new Integer[3]; // 3 pre-defined ports
    ports[0] = 6279;
    ports[1] = 6280;
    ports[2] = 6281;
    cluster = new RedisCluster(ports);
    cluster.start();
    try {
      Thread.sleep(10000); // allow time for cluster to be setup
    } catch (InterruptedException e) {
      log.error("Unable to sleep thread", e);
    }
  }

  /**
   * Tests publishing a message to a Redis channel.
   */
  @Test
  public void testMessagingRedis() {
    Assert.assertTrue("Cluster should be active.", cluster.isActive());
    Utility.notifyRedisChannel(StringUtils.join(hosts, Utility.STRING_SEPARATOR), StringUtils.join(ports, Utility.STRING_SEPARATOR), "", "channel", "message");
  }

  /**
   * Tears down after all tests have finished, i.e. stops the Redis test cluster.
   */
  @After
  public void tearDown() {
    if (cluster!=null) {
      cluster.stop();
    }
  }
}
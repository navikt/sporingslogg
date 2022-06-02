package no.nav.sporingslogg.kafkatestclient;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;

/////////////////////////////// KOPIERT FRA https://www.javatips.net/api/examples-master/kafka-streams/src/test/java/io/confluent/examples/streams/kafka/KafkaEmbedded.java



/**
 * Runs an in-memory, "embedded" instance of a Kafka broker, which listens at `127.0.0.1:9092` by
 * default.
 *
 * Requires a running ZooKeeper instance to connect to.  By default, it expects a ZooKeeper instance
 * running at `127.0.0.1:2181`.  You can specify a different ZooKeeper instance by setting the
 * `zookeeper.connect` parameter in the broker's configuration.
 */
public class KafkaEmbedded {

	public static final String KAFKA_BROKER_HOST = "127.0.0.1";
	public static final String KAFKA_BROKER_PORT = "9092";

	private static final Logger log = LoggerFactory.getLogger(KafkaEmbedded.class);

  private static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

  private final Properties effectiveConfig;
  private final File logDir;
  private final TemporaryFolder tmpFolder;
  private final KafkaServer kafka;

  /**
   * Creates and starts an embedded Kafka broker.
   *
   * @param config Broker configuration settings.  Used to modify, for example, on which port the
   *               broker should listen to.  Note that you cannot change some settings such as
   *               `log.dirs`, `port`.
   */
  public KafkaEmbedded(Properties config) throws IOException {
    tmpFolder = new TemporaryFolder();
    tmpFolder.create();
    logDir = tmpFolder.newFolder();
    effectiveConfig = effectiveConfigFrom(config);
    boolean loggingEnabled = true;

    KafkaConfig kafkaConfig = new KafkaConfig(effectiveConfig, loggingEnabled);
    log.debug("Starting embedded Kafka broker (with log.dirs={} and ZK ensemble at {}) ...",
        logDir, zookeeperConnect());
    kafka = TestUtils.createServer(kafkaConfig, Time.SYSTEM);
    log.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
        brokerList(), zookeeperConnect());
  }

  private  Properties effectiveConfigFrom(Properties initialConfig) throws IOException {
    Properties effectiveConfig = new Properties();
    effectiveConfig.put(KafkaConfig.BrokerIdProp(), 0);
    effectiveConfig.put("HostName", KAFKA_BROKER_HOST);
    effectiveConfig.put("Port", KAFKA_BROKER_PORT);
    effectiveConfig.put(KafkaConfig.NumPartitionsProp(), 1);
    effectiveConfig.put(KafkaConfig.AutoCreateTopicsEnableProp(), true);
    effectiveConfig.put(KafkaConfig.MessageMaxBytesProp(), 1000000);
    effectiveConfig.put(KafkaConfig.ControlledShutdownEnableProp(), true);

    effectiveConfig.putAll(initialConfig);
    effectiveConfig.setProperty(KafkaConfig.LogDirProp(), logDir.getAbsolutePath());
    return effectiveConfig;
  }

  /**
   * This broker's `metadata.broker.list` value.  Example: `127.0.0.1:9092`.
   *
   * You can use this to tell Kafka producers and consumers how to connect to this instance.
   */
  public  String brokerList() {
    return kafka.config() + ":"+Integer.toString(kafka.boundPort(ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT)));
  }


  /**
   * The ZooKeeper connection string aka `zookeeper.connect`.
   */
  public  String zookeeperConnect() {
    return effectiveConfig.getProperty("zookeeper.connect", DEFAULT_ZK_CONNECT);
  }

  /**
   * Stop the broker.
   */
  public  void stop() {
    log.debug("Shutting down embedded Kafka broker at {} (with ZK ensemble at {}) ...",
        brokerList(), zookeeperConnect());
    kafka.shutdown();
    kafka.awaitShutdown();
    log.debug("Removing temp folder {} with logs.dir at {} ...", tmpFolder, logDir);
    tmpFolder.delete();
    log.debug("Shutdown of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
        brokerList(), zookeeperConnect());
  }
}




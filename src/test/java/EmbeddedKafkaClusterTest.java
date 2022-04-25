import consumers.AlertConsumer;
import domain.AlertKeySerde;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.apache.kafka.test.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import producers.AlertProducer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EmbeddedKafkaClusterTest {
    private static final int BROKER_NUMBER = 3;
    private static final int PARTITION_NUMBER = 3;
    private static final int REPLICATION_NUMBER = 3;
    private static final String TOPIC = "kinaction_alert";

    public static final EmbeddedKafkaCluster CLUSTER = new EmbeddedKafkaCluster(BROKER_NUMBER);

    private Properties producerProps;
    private Properties consumerProps;

    @BeforeClass
    public static void startCluster() throws IOException {
        CLUSTER.start();
    }

    @AfterClass
    public static void closeCluster() {
        CLUSTER.stop();
    }

    @Before
    public void setUpBeforeClass() throws Exception {
        CLUSTER.createTopic(TOPIC, PARTITION_NUMBER, REPLICATION_NUMBER);
        producerProps = TestUtils.producerConfig(CLUSTER.bootstrapServers(), AlertKeySerde.class, StringSerializer.class);
        consumerProps = TestUtils.consumerConfig(CLUSTER.bootstrapServers(), AlertKeySerde.class, StringDeserializer.class);
    }

    @Test
    public void testAlertPartitioner() throws InterruptedException {
        final var producer = new AlertProducer();
        try {
            producer.sendMessage(producerProps);
        } catch (ExecutionException e) {
            fail("kinaction_error EmbeddedKafkaCluster exception" + e.getMessage());
        }

        final var consumer = new AlertConsumer();
        final var records = consumer.getAlertMessages(consumerProps);
        final var partition = new TopicPartition(TOPIC, 0);
        final var results = records.records(partition);

        assertEquals(0, results.get(0).partition());
        assertEquals("Stage 1 stopped", results.get(0).value());
    }
}

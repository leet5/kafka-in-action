package consumers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Slf4j
public class AuditConsumer {
    public static final String TOPIC = "kinaction_audit";
    private volatile boolean keepConsuming = true;

    public static void main(String[] args) {
        final var props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:19093,localhost:29093,localhost:39093");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(GROUP_ID_CONFIG, "audit_group");
        props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");

        final var consumer = new AuditConsumer();
        consumer.consume(props);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));
    }

    private void consume(Properties props) {
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of(TOPIC));
            while (keepConsuming) {
                final var records = consumer.poll(Duration.ofMillis(250));
                for (final var record : records) {
                    log.info("kinaction_info offset = {}, kinaction_value = {}", record.offset(), record.value());
                    commitOffset(consumer, record);
                }
            }
        }
    }

    private void commitOffset(KafkaConsumer<String, String> consumer, ConsumerRecord<String, String> record) {
        final var offsetMeta = new OffsetAndMetadata(record.offset() + 1, "");
        final var kaOffsetMap = new HashMap<TopicPartition, OffsetAndMetadata>();
        kaOffsetMap.put(new TopicPartition(TOPIC, record.partition()), offsetMeta);

        consumer.commitSync(kaOffsetMap);
    }

    private void shutdown() {
        keepConsuming = false;
    }
}

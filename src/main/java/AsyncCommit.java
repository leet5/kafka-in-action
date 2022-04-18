import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class AsyncCommit {
    public static final String TOPIC_NAME = "kinaction_views";
    private boolean keepConsuming = true;

    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19093, localhost:29093, localhost:39093");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kinaction_group_views");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        final AsyncCommit consumer = new AsyncCommit();
        consumer.consume(props);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));
    }

    private void consume(final Properties properties) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.assign(List.of(new TopicPartition(TOPIC_NAME, 1), new TopicPartition(TOPIC_NAME, 2)));
            while (keepConsuming) {
                final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(250));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("kinaction_info offset = {}, value = {}", record.offset(), record.value());
                    commitOffset(record.offset(), record.partition(), consumer);
                }
            }
        }
    }

    private static void commitOffset(long offset, int partition, KafkaConsumer<String, String> consumer) {
        final OffsetAndMetadata offsetMeta = new OffsetAndMetadata(++offset, "");
        final Map<TopicPartition, OffsetAndMetadata> kaOffsetMap = new HashMap<>();
        kaOffsetMap.put(new TopicPartition(TOPIC_NAME, partition), offsetMeta);

        consumer.commitAsync(kaOffsetMap, (map, e) -> {
            if (e != null) {
                for (TopicPartition key : map.keySet()) {
                    log.info("kinaction_error: offset {}", map.get(key).offset());
                }
            } else {
                for (TopicPartition key : map.keySet()) {
                    log.info("kinaction_info: offset {}", map.get(key).offset());
                }
            }
        });
    }

    private void shutdown() {
        this.keepConsuming = false;
    }
}

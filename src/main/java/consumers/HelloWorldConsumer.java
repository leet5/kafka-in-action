package consumers;

import domain.Alert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Slf4j
public class HelloWorldConsumer {
    private volatile boolean keepConsuming = true;

    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:19093");
        props.put(GROUP_ID_CONFIG, "group-1");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", "http://localhost:8081");

        final HelloWorldConsumer consumer = new HelloWorldConsumer();
        consumer.consume(props);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));
    }

    private void consume(Properties props) {
        try (final KafkaConsumer<Long, Alert> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("kinaction_schematest"));
            while (keepConsuming) {
                final ConsumerRecords<Long, Alert> records = consumer.poll(Duration.ofMillis(250));
                for (final ConsumerRecord<Long, Alert> record : records) {
                    log.info("kinaction_info offset = {}, kinaction_value = {}", record.offset(), record.value());
                }
            }
        }
    }

    private void shutdown() {
        keepConsuming = false;
    }
}

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Slf4j
public class HelloWorldProducer {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put(ACKS_CONFIG, "all");
        props.put(RETRIES_CONFIG, "3");
        props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");

        props.put("schema.registry.url", "http://localhost:8081");

        try (final Producer<Long, Alert> producer = new KafkaProducer<>(props)) {
            final Alert alert = new Alert(10L, Instant.now().toEpochMilli(), AlertStatus.Warning);
            log.info("kinaction_info Alert -> {}", alert);

            final ProducerRecord<Long, Alert> record = new ProducerRecord<>("kinaction_schematest", alert.getSensorId(), alert);

            final RecordMetadata result = producer.send(record).get();
            log.info("kinaction_info offset = {}, topic = {}, timestamp = {}", result.offset(), result.topic(), result.timestamp());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

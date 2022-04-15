import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Instant;
import java.util.Properties;

@Slf4j
public class HelloWorldProducer {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", "http://localhost:8081");

        try (Producer<Long, Alert> producer = new KafkaProducer<>(props)) {
            final Alert alert = new Alert(10L, Instant.now().toEpochMilli(), AlertStatus.Warning);
            log.info("kinaction_info Alert -> {}", alert);

            final ProducerRecord<Long, Alert> record = new ProducerRecord<>("kinaction_schematest", alert.getSensorId(), alert);
            producer.send(record);
        }
    }
}

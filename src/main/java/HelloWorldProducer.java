import domain.AlertCallback;
import domain.AlertKeySerde;
import domain.AlertLevelPartitioner;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Slf4j
public class HelloWorldProducer {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:19093");
        props.put(PARTITIONER_CLASS_CONFIG, AlertLevelPartitioner.class.getName());
        props.put(KEY_SERIALIZER_CLASS_CONFIG, AlertKeySerde.class.getName());
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ACKS_CONFIG, "all");
        props.put(RETRIES_CONFIG, "3");
        props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");

        props.put("schema.registry.url", "http://localhost:8081");

        try (final Producer<domain.Alert, String> producer = new KafkaProducer<>(props)) {
            final domain.Alert alert = new domain.Alert(0, "Stage 0", "CRITICAL", "Stage 0 stopped CRITICAL");
            final ProducerRecord<domain.Alert, String> record = new ProducerRecord<>("kinaction_schematest", alert, alert.getAlertMessage());
            producer.send(record, new AlertCallback());
        }
    }
}

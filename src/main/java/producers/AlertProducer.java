package producers;

import domain.Alert;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class AlertProducer {
    public static final String TOPIC = "kinaction_alert";

    public void sendMessage(Properties properties) throws InterruptedException, ExecutionException {
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "partitioners.AlertLevelPartitioner");
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, AlertProducerInterceptor.class.getName());

        try (var producer = new KafkaProducer<Alert, String>(properties)) {
            final var alert = new Alert(1, "Stage 1", "CRITICAL", "Stage 1 stopped");
            final var record = new ProducerRecord<>(TOPIC, alert, alert.getAlertMessage());
            producer.send(record).get();
        }
    }
}

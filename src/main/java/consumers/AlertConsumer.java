package consumers;

import domain.Alert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
public class AlertConsumer {
    public static final String TOPIC = "kinaction_alert";

    public ConsumerRecords<Alert, String> getAlertMessages(Properties properties) {
        try (var consumer = new KafkaConsumer<Alert, String>(properties)) {
            consumer.subscribe(List.of(TOPIC));
            return consumer.poll(Duration.ofMillis(2500));
        }
    }
}

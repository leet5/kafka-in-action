package producers;

import domain.Alert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class AlertProducerInterceptor implements ProducerInterceptor<Alert, String> {
    @Override
    public ProducerRecord<Alert, String> onSend(ProducerRecord<Alert, String> record) {
        final var headers = record.headers();
        final var kinactionTraceId = UUID.randomUUID().toString();

        headers.add("kinactionTraceId", kinactionTraceId.getBytes(StandardCharsets.UTF_8));
        log.info("kinaction_info Created kinactionTraceId: {}", kinactionTraceId);

        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception e) {
        if (e != null) {
            log.info("kinaction_error " + e.getMessage());
        } else {
            log.info("kinaction_info topic = {}, offset = {}", metadata.topic(), metadata.offset());
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}

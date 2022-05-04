package consumers;

import domain.Alert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

@Slf4j
public class AlertConsumerInterceptor implements ConsumerInterceptor<Alert, String> {
    @Override
    public ConsumerRecords<Alert, String> onConsume(ConsumerRecords<Alert, String> records) {
        if (records.isEmpty()) {
            return records;
        } else {
            records.forEach(record -> record.headers().forEach(header -> {
                if ("kinactionTraceId".equals(header.key())) {
                    log.info("kinactionTraceId is: " + new String(header.value()));
                }
            }));
        }
        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {

    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}

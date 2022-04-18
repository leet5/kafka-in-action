package producers;

import domain.AlertCallback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

public class FlumeProducer {
    public static void main(String[] args) {
        final Properties props = readConfig();
        final String topic = props.getProperty("topic");
        props.remove("topic");

        try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, "event");
            producer.send(record, new AlertCallback());
        }
    }

    private static Properties readConfig() {
        final Path path = Paths.get("src/main/resources/kafkasink.conf");
        final Properties props = new Properties();
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEachOrdered(line -> determineProperty(line, props));
        } catch (IOException e) {
            System.out.println("kinaction_error " + e);
            e.printStackTrace();
        }
        return props;
    }

    private static void determineProperty(String line, Properties props) {
        if (line.contains("bootstrap")) {
            props.put("bootstrap.servers", line.split("=")[1]);
        } else if (line.contains("acks")) {
            props.put("acks", line.split("=")[1]);
        } else if (line.contains("compression.type")) {
            props.put("compression.type", line.split("=")[1]);
        } else if (line.contains("topic")) {
            props.put("topic", line.split("=")[1]);
        }

        props.putIfAbsent("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.putIfAbsent("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }
}

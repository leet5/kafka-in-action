package partitioners;

import domain.Alert;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.Random;

public class AlertLevelPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object objectKey, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        final int criticalLevelPartition = findCriticalPartitionNumber(cluster, topic);
        return isCriticalLevel(((Alert) objectKey).getAlertLevel()) ? criticalLevelPartition : findRandomPartition(cluster, topic, objectKey);
    }

    public int findCriticalPartitionNumber(Cluster cluster, String topic) {
        return 0;
    }

    public int findRandomPartition(Cluster cluster, String topic, Object objectKey) {
        final var partitionMetaList = cluster.availablePartitionsForTopic(topic);
        final var randomPart = new Random();
        return randomPart.nextInt(partitionMetaList.size());
    }

    public boolean isCriticalLevel(String level) {
        return level.toUpperCase().contains("CRITICAL");
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}

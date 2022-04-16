package domain;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AlertLevelPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int criticalLevelPartition = findCriticalPartitionNumber(cluster, topic);
        return isCriticalLevel(((domain.Alert)key).getAlertLevel()) ? criticalLevelPartition : findRandomPartition(cluster, topic, value);
    }

    private int findCriticalPartitionNumber(Cluster cluster, String topic) {
        return 0;
    }

    private int findRandomPartition(Cluster cluster, String topic, Object value) {
        final List<PartitionInfo> partitionInfos = cluster.availablePartitionsForTopic(topic);

        final Random random = new Random();
        return random.nextInt(partitionInfos.size());
    }

    private boolean isCriticalLevel(String level) {
        return level.toUpperCase(Locale.ROOT).equals("CRITICAL");
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}

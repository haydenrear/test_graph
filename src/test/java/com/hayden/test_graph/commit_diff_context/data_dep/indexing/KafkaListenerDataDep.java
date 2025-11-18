package com.hayden.test_graph.commit_diff_context.data_dep.indexing;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.util.Lists;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@Slf4j
@Profile("crawl")
@RequiredArgsConstructor
public class KafkaListenerDataDep implements CommitDiffContextIndexingDataDepNode {

    public static final int CAPACITY = 1024;
    public static final int KAFKA_POLL_MILLIS = 100;
    private final Assertions assertions;

    private KafkaProperties kafkaProperties;

    private KafkaConsumer<Object, Object> consumer;

    public record ExpectedConsumerRecords(
            CommitDiffContextIndexingDataDepCtx.ExpectKafka expectKafka,
            ConsumerRecords<Object, Object> records) {}

    Queue<ExpectedConsumerRecords> retrieved = new ArrayBlockingQueue<>(CAPACITY);

    private boolean stop = false;

    private final ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

    public void initializeKafkaTopic(List<CommitDiffContextIndexingDataDepCtx.ExpectKafka> expected, String topic) {
        consumer = new KafkaConsumer<>(kafkaProperties.buildConsumerProperties());
        consumer.seekToEnd(Lists.newArrayList(new TopicPartition(topic, 1)));
        service.submit(() -> {
            while (!stop) {
                var polled = consumer.poll(Duration.ofMillis(KAFKA_POLL_MILLIS));
                if (!polled.isEmpty()) {
                    if (retrieved.size() >= CAPACITY) {
                        var removed = retrieved.remove();
                        log.warn("Exceeded capacity in kafka - {}.", removed);
                    }

                    for (var e : expected) {
                        retrieved.add(new ExpectedConsumerRecords(e, polled));
                    }
                }
            }

            log.info("Finished consuming kafka.");
        });
    }

    @Override
    public CommitDiffContextIndexingDataDepCtx exec(CommitDiffContextIndexingDataDepCtx c, MetaCtx h) {

        if (!c.isKafkaEnabled())
            return c;
        else if (c.getKafkaQueues().res().optional().isEmpty()) {
            assertions.assertThat(c.getKafkaQueues().res().optional().isEmpty())
                    .withFailMessage("Kafka queues were supposed to be set in step definition.")
                    .isFalse();
        }

        var qMatcher = c.getKafkaQueues().res()
                .optional()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(CommitDiffContextIndexingDataDepCtx.ExpectKafka::queue));

        for (var ek : qMatcher.entrySet()) {
            initializeKafkaTopic(ek.getValue(), ek.getKey());
        }

        c.getKafkaRecords().swap(retrieved);
        return c;
    }

}

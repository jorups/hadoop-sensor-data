package com.jo.htest.simulator;

import junit.framework.TestCase;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimulatorTest extends TestCase {
    public class MockProducer implements Producer {
        @Override
        public Future<RecordMetadata> send(ProducerRecord producerRecord) {
            return new Future<RecordMetadata>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return true;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public RecordMetadata get() throws InterruptedException, ExecutionException {
                    return new RecordMetadata(new TopicPartition("dummy", 0), 0, 0, 0, 0, 0, 0);
                }

                @Override
                public RecordMetadata get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return new RecordMetadata(new TopicPartition("dummy", 0), 0, 0, 0, 0, 0, 0);
                }
            };
        }

        @Override
        public Future<RecordMetadata> send(ProducerRecord producerRecord, Callback callback) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public List<PartitionInfo> partitionsFor(String s) {
            return null;
        }

        @Override
        public Map<MetricName, ? extends Metric> metrics() {
            return null;
        }

        @Override
        public void close() {

        }

        @Override
        public void close(long l, TimeUnit timeUnit) {

        }
    }
    public void testSimulator() {
        Simulator sim = new Simulator(new MockProducer(), 5, 20, 1);
        sim.start();
        try {
            while (sim.isRunning()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Total data samples processed: " + sim.samplesObserved());
        assertEquals(100, sim.samplesObserved());
    }
}

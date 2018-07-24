package com.jo.htest.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/* Represents class to construct
stream consumer for Local broker */

public class LocalStreamConsumer {
    private static final String SERVERS = "localhost:9092";
    public static JavaInputDStream<ConsumerRecord<Long, String>> createStream() {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", SERVERS);
        kafkaParams.put("key.deserializer", LongDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "SensorDataConsumers");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        // Subscribe to "senor-data" topic
        Collection<String> topics = Arrays.asList("sensor-data");
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("SensorDataConsumer");
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(1));

        return KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<Long, String>Subscribe(topics, kafkaParams)
                );
    }

}

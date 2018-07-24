package com.jo.htest.consumer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* Represents Sensor data stream processor.
Consumes data from message broker, do data
transformation and persist in HBase table
 */

public final class StreamProcessor {
    // Define HBase table name, CFamilies & column descriptors
    private final static TableName TABLE_NAME = TableName.valueOf("sensor-data");
    private final static byte[] DATA_COLUMN_FAMILY = Bytes.toBytes("data");
    private final static byte[] DATA_COLUMN_DEVICEID = Bytes.toBytes("deviceID");
    private final static byte[] DATA_COLUMN_TEMPERATURE = Bytes.toBytes("temperature");
    private final static byte[] DATA_COLUMN_LATITUDE = Bytes.toBytes("latitude");
    private final static byte[] DATA_COLUMN_LONGITUDE = Bytes.toBytes("longitude");
    private final static byte[] DATA_COLUMN_TIME = Bytes.toBytes("time");
    // CFamily for raw json data
    private final static byte[] RAWDATA_COLUMN_FAMILY = Bytes.toBytes("raw");
    // CFamily for parsed data
    private final static byte[] RAWDATA_COLUMN_DATA = Bytes.toBytes("data");
    private static Table table;
    public static void main(String args[]) {
        if (!prepareHBase()) {
            throw new RuntimeException("Couldn't prepare HBase table");
        }
        // Create DStream
        JavaInputDStream<ConsumerRecord<Long, String>> stream = LocalStreamConsumer.createStream();

        // Iterate through stream
        stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<Long, String>>>() {
            public void call(JavaRDD<ConsumerRecord<Long, String>> consumerRecordJavaRDD) throws Exception {
                consumerRecordJavaRDD.foreach(new VoidFunction<ConsumerRecord<Long, String>>() {
                    public void call(ConsumerRecord<Long, String> longStringConsumerRecord) throws Exception {
                        // Parse json received from broker
                        JSONObject json = (new JSONObject(longStringConsumerRecord.value())).getJSONObject("data");
                        Put put = new Put(Bytes.toBytes(String.valueOf(longStringConsumerRecord.key())));
                        SimpleDateFormat dateFormat = new SimpleDateFormat();
                        try {
                            dateFormat = new SimpleDateFormat("yyyy-MM-dd-'T'-HH:mm:ssXXX");
                        } catch (IllegalArgumentException exception) {
                            System.out.println(exception);
                        }
                        // Set everything up to push into HBase
                        String formattedDate = dateFormat.format(new Date(json.getLong("time")));
                        put.addColumn(RAWDATA_COLUMN_FAMILY, RAWDATA_COLUMN_DATA, Bytes.toBytes(longStringConsumerRecord.value()));
                        put.addColumn(DATA_COLUMN_FAMILY, DATA_COLUMN_DEVICEID, Bytes.toBytes(json.getString("deviceId")));
                        put.addColumn(DATA_COLUMN_FAMILY, DATA_COLUMN_TEMPERATURE, Bytes.toBytes(json.getInt("temperature")));
                        JSONObject location = json.getJSONObject("location");
                        put.addColumn(DATA_COLUMN_FAMILY, DATA_COLUMN_LATITUDE, Bytes.toBytes(location.getDouble("latitude")));
                        put.addColumn(DATA_COLUMN_FAMILY, DATA_COLUMN_LONGITUDE, Bytes.toBytes(location.getDouble("longitude")));
                        put.addColumn(DATA_COLUMN_FAMILY, DATA_COLUMN_TIME, Bytes.toBytes(formattedDate));
                        table.put(put);
                    }
                });
            }
        });
        StreamingContext context = stream.context();
        context.start();
        context.awaitTermination();
    }

    private static boolean prepareHBase() {
        // Create HBase table if doesn't exist
        try {
            Configuration conf = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(conf);
            Admin admin = connection.getAdmin();
            if (!admin.tableExists(TABLE_NAME)) {
                System.out.println("Creating HBase table");
                HTableDescriptor descriptor = new HTableDescriptor(TABLE_NAME);
                descriptor.addFamily(new HColumnDescriptor(DATA_COLUMN_FAMILY));
                descriptor.addFamily(new HColumnDescriptor(RAWDATA_COLUMN_FAMILY));
                admin.createTable(descriptor);
                table = connection.getTable(TABLE_NAME);
            } else {
                table = connection.getTable(TABLE_NAME);
            }
            return admin.isTableAvailable(TABLE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

CREATE EXTERNAL TABLE sensor_data (
       id STRING,
       device_id STRING,
       temperature INT,
       latitude DOUBLE,
       longitude DOUBLE,
       time STRING
       )
     STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
     WITH SERDEPROPERTIES (
       "hbase.columns.mapping" =
       ":key,data:deviceID,data:temperature#b,data:latitude#b,
       data:longitude#b,data:time"
     )

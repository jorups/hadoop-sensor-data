package com.jo.htest.simulator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

/* Represents data point from sensor device */
public class DataSample {
    private long id;
    private UUID deviceID;
    private int temperature;
    private double latitude;
    private double longitude;
    private long timestamp;

    public DataSample(UUID deviceID, int temperature, long latitude, long longitude, long timestamp) {
        this.id = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
        this.deviceID = deviceID;
        this.temperature = temperature;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    public DataSample(UUID deviceID) {
        this(deviceID, 0, 0, 0, 0);
    }

    public long getId() {
        return id;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void randomizeData() {
        this.temperature = randomInt(-40, 40);
        this.latitude = (ThreadLocalRandom.current().nextDouble() * 180.0) - 90.0;
        this.longitude = (ThreadLocalRandom.current().nextDouble() * 360.0) - 180.0;
        this.timestamp = ((long) ThreadLocalRandom.current().nextInt(1514764800, 1531726065)) * 1000;
    }
    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public String serializeToJSONString() {
        // TODO: use JSON builder
        return "{" +
                "   \"data\": {\n" +
                "       \"deviceId\":" + this.deviceID + ",\n" +
                "       \"temperature\":" + this.temperature + ",\n" +
                "       \"location\": {\n" +
                "           \"latitude\":" + this.latitude + ",\n" +
                "           \"longitude\":" + this.longitude + "\n" +
                "       },\n" +
                "       \"time\":" + this.timestamp + "\n" +
                "   }\n" +
                "}";

    }
}

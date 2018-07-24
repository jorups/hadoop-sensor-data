package com.jo.htest.simulator;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/* Represents Simulator that contains multiple devices,
which produces data points that are sent to Kafka broker */

public class Simulator implements DataSampleObserver {
    private int deviceCount = 5;
    private int samplesPerDevice = 0;
    private int samplesPerSecond = 10;
    private int samplesObserved = 0;
    private String topic = "sensor-data";
    private ArrayList<Device> devices = new ArrayList<>();
    Producer<Long, String> producer;

    public Simulator(Producer<Long, String> producer, int deviceCount) {
        this(producer, deviceCount, 0, 50);
    }
    public Simulator(Producer<Long, String> producer, int deviceCount, int samplesPerDevice, int samplesPerSecond) {
        this.producer = producer;
        this.deviceCount = deviceCount;
        this.samplesPerDevice = samplesPerDevice;
        this.samplesPerSecond = samplesPerSecond;
        this.addDevices();
    }
    private void addDevices() {
        // create devices
        for (int i = 0; i < this.deviceCount; i++) {
            Device d = new Device(this.samplesPerDevice, this.samplesPerSecond);
            d.addObserver(this);
            this.devices.add(d);
        }
    }

    public boolean isRunning() {
        for (Device dev: this.devices) {
            if (dev.isOn()) {
                return true;
            }
        }
        return false;
    }

    public void start() {
        for (Device dev: this.devices) {
            dev.turnOn();
        }
    }

    public void stop() {
        for (Device dev: this.devices) {
            dev.turnOff();
        }
    }

    @Override
    // Get's called whenever device has new data point
    public synchronized void newSample(Device dev, DataSample sample) {
        if (this.send(sample)) {
            this.samplesObserved++;
        }
    }

    @Override
    public int samplesObserved() {
        return this.samplesObserved;
    }

    private boolean send(DataSample sample) {
        try {
            // Create record and send to Kafka
            ProducerRecord<Long, String> rec = this.createRecord(sample);
            RecordMetadata meta = this.producer.send(rec).get();
            return true;
        } catch (InterruptedException e) {
            System.out.println("INTR: Failed to send sample to kafka: " + e.toString());
        } catch (ExecutionException e) {
            System.out.println("EXEC: Failed to send sample to kafka: " + e.toString());
        }
        return false;
    }

    private ProducerRecord<Long, String> createRecord(DataSample sample) {
        return new ProducerRecord<>(this.topic, sample.getId(), sample.serializeToJSONString());
    }
}

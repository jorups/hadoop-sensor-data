package com.jo.htest.simulator;

import javax.xml.crypto.Data;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/* Represents Device/Sensor which generates DataSample and notifies Observers */
public class Device implements Runnable, Observable {
    private UUID id;
    private long samples;
    private int samplesPerSecond;
    private List<DataSampleObserver> observers;
    private Thread generatorThread;
    private long samplesGenerated = 0;
    private double failureRate;
    private int lowTemperature = ThreadLocalRandom.current().nextInt(-40, 39);
    private int hiTemperature = ThreadLocalRandom.current().nextInt(lowTemperature, 40);

    public Device(long samples, int samplesPerSecond) {
        this.samples = samples;
        this.samplesPerSecond = samplesPerSecond;
        this.observers = new ArrayList<DataSampleObserver>();
        this.id = UUID.randomUUID();
        // Inject some failure
        this.failureRate = ThreadLocalRandom.current().nextDouble(0, 0.1);
    }

    public UUID getId() {
        return id;
    }

    public void turnOn() {
        this.generatorThread = new Thread(this);
        this.generatorThread.start();
    }

    public void turnOff() {
        if (this.generatorThread != null) {
            this.generatorThread.interrupt();
        }
    }

    public boolean isOn() {
        if (this.generatorThread == null) {
            return false;
        }
        return this.generatorThread.isAlive();
    }

    public long getSamplesGenerated() {
        return this.samplesGenerated;
    }

    @Override
    public void run() {
        try {
            long samplesToGo = this.samples == 0 ? Long.MAX_VALUE : this.samples;
            while(!Thread.interrupted() && (this.samples == 0 || samplesToGo > 0)) {
                if (this.failureRate > 0 && samplesToGo % ((int)(1/this.failureRate)) == 0) {
                    samplesToGo--;
                    continue;
                }
                // Keep the rate accordingly to samplesPerSecond param
                Thread.sleep(1000 / samplesPerSecond);
                samplesToGo--;
                this.samplesGenerated++;
                DataSample sample = new DataSample(this.id);
                sample.randomizeData();
                // generate temperature for specific Device temp range
                sample.setTemperature(ThreadLocalRandom.current().nextInt(this.lowTemperature, this.hiTemperature));
                // Let Observers know that we have new data point
                this.notifyObserver(sample);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Device loop interrupted: " + this.id);
        }
    }

    @Override
    public void addObserver(DataSampleObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void notifyObserver(DataSample sample) {
        for (DataSampleObserver o : this.observers) {
            o.newSample(this, sample);
        }
    }
}

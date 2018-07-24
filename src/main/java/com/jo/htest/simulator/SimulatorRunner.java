package com.jo.htest.simulator;

import org.apache.kafka.clients.producer.Producer;

public class SimulatorRunner {
    public static void main(String[] args) {
        Producer<Long, String> producer = LocalKafkaProducer.createProducer();
        // default Runner params
        int samplesPerDevice = 0; // 0 - infinite samples
        int samplesPerSecond = 1;
        int deviceCount = 13;
        if (args.length == 3) {
            deviceCount = Integer.valueOf(args[0]);
            samplesPerDevice = Integer.valueOf(args[1]);
            samplesPerSecond = Integer.valueOf(args[2]);
        }
        Simulator sim = new Simulator(producer, deviceCount, samplesPerDevice, samplesPerSecond);
        sim.start();
        try {
            while (sim.isRunning()) {
                Thread.sleep(1000);
                System.out.println("Data samples processed so far: " + sim.samplesObserved());
            }
        } catch (InterruptedException e) {
        }
        System.out.println("Total data samples processed: " + sim.samplesObserved());
    }
}

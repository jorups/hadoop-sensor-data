package com.jo.htest.simulator;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class DeviceTest extends TestCase {
    private class MockupObserver implements DataSampleObserver {
        private int samplesReceived = 0;
        Map<UUID, Integer> deviceSamplesReceived = new HashMap<>();

        @Override
        public synchronized void newSample(Device dev, DataSample data) {
            Integer count = this.deviceSamplesReceived.get(dev.getId());
            Integer inc;
            if (count == null) {
                count = new Integer(0);
            }
            count = new Integer(count.intValue() + 1);
            this.deviceSamplesReceived.put(dev.getId(), count);
            this.samplesReceived++;
        }

        @Override
        public int samplesObserved() {
            return this.samplesReceived;
        }

        public void printDeviceStats() {
            System.out.println(this.deviceSamplesReceived);
        }
    }
    public void testDeviceSamples()
    {
        Device dev = new Device(10, 1);
        dev.turnOn();
        while (dev.isOn()) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
        assertEquals(10, dev.getSamplesGenerated());
    }
    public void testDeviceInterrupt()
    {
        Device dev = new Device(10, 1);
        dev.turnOn();
        int steps = 0;
        while (dev.isOn()) {
            try {
                sleep(100);
                // Interrupt after 2s (20 * 100ms)
                if (steps > 20) {
                    dev.turnOff();
                }
                steps++;
            } catch (InterruptedException e) {
                break;
            }
        }
        assertEquals(2, dev.getSamplesGenerated());
    }
    public void testDeviceObserver() {
        MockupObserver observer = new MockupObserver();
        ArrayList<Device> devices = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Device dev = new Device(1000, 100);
            dev.addObserver(observer);
            devices.add(dev);
        }
        for (Device dev: devices) {
            dev.turnOn();
        }
        boolean deviceAlive = true;
        while(deviceAlive) {
            deviceAlive = false;
            System.out.println("Observed " + observer.samplesObserved() + " so far");
            for (Device dev: devices) {
                if (dev.isOn()) {
                    deviceAlive = true;
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Sleep interrupted");
                    }
                    break;
                }
            }
        }
        System.out.println("Total samples " + observer.samplesObserved() + " observed");
        observer.printDeviceStats();
        assertEquals(5000, observer.samplesObserved());
    }
}

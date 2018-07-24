package com.jo.htest.simulator;

/* Interface for Data Sample Observers */
public interface DataSampleObserver {
    // Device call newSample when ne data point available
    void newSample(Device dev, DataSample data);
    // Observer samples by observer
    int samplesObserved();
}

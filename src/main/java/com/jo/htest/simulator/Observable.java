package com.jo.htest.simulator;

/* Defines interface for objects that can be observed by Observers.
 * each Observable migh have one or many Observers. */
public interface Observable {
    void addObserver(DataSampleObserver observer);
    void notifyObserver(DataSample sample);
}

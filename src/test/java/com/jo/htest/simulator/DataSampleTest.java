package com.jo.htest.simulator;

import junit.framework.TestCase;

import java.util.UUID;

public class DataSampleTest extends TestCase {
    public void testJson() {
        UUID id = UUID.randomUUID();
        DataSample sample = new DataSample(id);
        sample.randomizeData();
        System.out.println(sample.serializeToJSONString());
    }
}

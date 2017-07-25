package com.aimbrain.sdk;


import com.aimbrain.sdk.exceptions.InvalidMemoryUsageLimit;
import com.aimbrain.sdk.mock.MemoryLimiterMock;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MemoryLimiterTest {

    @Test
    public void testSetMemoryUsageLimitInKilobytes() {
        MemoryLimiterMock memoryUtil = new MemoryLimiterMock();
        memoryUtil.setMemoryUsageLimitInKilobytes(10);
        assertEquals("Failed kilobytes to bytes conversion. Converted value is " + memoryUtil.getEnforceMemoryUsageLimit() + " Should be 10240", 10240, memoryUtil.getEnforceMemoryUsageLimit());
    }

    @Test(expected = InvalidMemoryUsageLimit.class)
    public void testSetMemoryUsageLimitInKilobytesValueNegative() {
        MemoryLimiterMock memoryUtil = new MemoryLimiterMock();
        memoryUtil.setMemoryUsageLimitInKilobytes(-100);
    }
}

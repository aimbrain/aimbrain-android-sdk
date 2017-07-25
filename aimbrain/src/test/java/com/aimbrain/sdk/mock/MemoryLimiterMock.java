package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.memory.MemoryLimiter;


public class MemoryLimiterMock extends MemoryLimiter {
    public MemoryLimiterMock() {
        super();
    }

    public int getEnforceMemoryUsageLimit() {
        return super.getEnforceMemoryUsageLimit();
    }
}

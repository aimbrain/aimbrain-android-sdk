package com.aimbrain.sdk.memory;


import android.support.annotation.VisibleForTesting;

import com.aimbrain.sdk.collectors.MotionEventCollector;
import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.exceptions.InvalidMemoryUsageLimit;
import com.aimbrain.sdk.util.Logger;

import static com.aimbrain.sdk.Manager.MEMORY_USAGE_UNLIMITED;

public class MemoryLimiter {
    private final String TAG = getClass().getName();
    private int enforceMemoryUsageLimit = MEMORY_USAGE_UNLIMITED;
    private static MemoryLimiter instance;

    public static MemoryLimiter getInstance() {
        if (instance == null)
            instance = new MemoryLimiter();
        return instance;
    }

    @VisibleForTesting
    protected MemoryLimiter() {
    }

    /**
     * Set data collection memory limit
     *
     * @param memoryUsageLimit in kilobytes. Pass Manager.MEMORY_USAGE_UNLIMITED param for unlimited memory usage
     */
    public void setMemoryUsageLimitInKilobytes(int memoryUsageLimit) {
        if (memoryUsageLimit < 0) {
            throw new InvalidMemoryUsageLimit("Memory limit should be greater than 0");
        }
        this.enforceMemoryUsageLimit = memoryUsageLimit * 1024;
    }

    public void collectedEvent() {
        if (enforceMemoryUsageLimit != MEMORY_USAGE_UNLIMITED) {
            reduceMemoryUsage();
        }
    }

    private void reduceMemoryUsage() {
        int allMemoryUsage = MotionEventCollector.getInstance().sizeOfElements() + SensorEventCollector.getInstance().sizeOfElements() + TextEventCollector.getInstance().sizeOfElements();
        if (allMemoryUsage >= enforceMemoryUsageLimit) {
            int needToRemoveBytes = (int) (allMemoryUsage - enforceMemoryUsageLimit + enforceMemoryUsageLimit * 0.3); // remove more 30% than enforceMemoryUsageLimit
            int allRemovedBytes = 0;
            while (allRemovedBytes < needToRemoveBytes) {
                long oldestTimestamp = getTimestampOfOldestEvent() + 500; //add 500 ms tolerance to remove more bytes in one iteration
                int sensorRemovedBytes = SensorEventCollector.getInstance().removeElementsOlderThan(oldestTimestamp);
                int textEventsRemovedBytes = TextEventCollector.getInstance().removeElementsOlderThan(oldestTimestamp);
                int touchEventsRemovedBytes = MotionEventCollector.getInstance().removeElementsOlderThan(oldestTimestamp);
                allRemovedBytes += sensorRemovedBytes + textEventsRemovedBytes + touchEventsRemovedBytes;
            }
            Logger.v(TAG, "all data size in bytes = " + allMemoryUsage);
            Logger.v(TAG, "removed data size in bytes = " + allRemovedBytes);
        }
    }

    private long getTimestampOfOldestEvent() {
        long sensorTimestamp = SensorEventCollector.getInstance().getOldestEventTimestamp();
        long textEventsTimestamp = TextEventCollector.getInstance().getOldestEventTimestamp();
        long touchEventsTimestamp = MotionEventCollector.getInstance().getOldestEventTimestamp();
        return Math.min(Math.min(sensorTimestamp, textEventsTimestamp), touchEventsTimestamp);
    }

    @VisibleForTesting
    protected int getEnforceMemoryUsageLimit() {
        return enforceMemoryUsageLimit;
    }
}

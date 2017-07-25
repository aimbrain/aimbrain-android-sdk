package com.aimbrain.sdk.collectors;

import com.aimbrain.sdk.memory.MemoryLimiter;
import com.aimbrain.sdk.models.EventModel;

import java.util.LinkedList;
import java.util.List;


public abstract class EventCollector {

    private LinkedList<EventModel> mQueue;

    public EventCollector() {
        this.mQueue = new LinkedList<>();
    }

    protected synchronized void addCollectedData(EventModel model) {
        this.mQueue.add(model);
        MemoryLimiter.getInstance().collectedEvent();
    }

    public synchronized List<EventModel> getCollectedData() {
        List<EventModel> collectedData = this.mQueue;
        this.mQueue = new LinkedList<>();
        return collectedData;
    }

    public boolean hasData() {
        if (mQueue.size() > 0)
            return true;
        return false;
    }

    private void removeOldestElements(int elementSize) {
        if (mQueue != null && mQueue.size() >= elementSize) {
            mQueue = new LinkedList<>(mQueue.subList(elementSize, mQueue.size()));
        }
    }

    public synchronized int removeElementsOlderThan(long timestamp) {
        int count = 0;
        for (EventModel model : mQueue) {
            if (model.timestamp <= timestamp) {
                count++;
            } else {
                break;
            }
        }
        removeOldestElements(count);
        return sizeOfElements(count);
    }

    public long getOldestEventTimestamp() {
        if (mQueue != null && mQueue.size() > 0) {
            return mQueue.get(0).timestamp;
        } else {
            return System.currentTimeMillis();
        }
    }

    public int getCountOfElements() {
        return mQueue.size();
    }

    abstract int sizeOfElements(int count);

    public abstract int sizeOfElements();

}

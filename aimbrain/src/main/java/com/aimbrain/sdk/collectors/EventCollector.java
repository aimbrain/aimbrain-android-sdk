package com.aimbrain.sdk.collectors;

import com.aimbrain.sdk.models.EventModel;

import java.util.LinkedList;
import java.util.List;


public class EventCollector {

    private LinkedList<EventModel> mQueue;

    public EventCollector() {
        this.mQueue = new LinkedList<>();
    }

    protected synchronized void addCollectedData(EventModel model) {
        this.mQueue.add(model);
    }

    public synchronized List<EventModel> getCollectedData() {
        List<EventModel> collectedData = this.mQueue;
        this.mQueue = new LinkedList<>();
        return collectedData;
    }

    public boolean hasData() {
        if(mQueue.size() > 0)
            return true;
        return false;
    }

}

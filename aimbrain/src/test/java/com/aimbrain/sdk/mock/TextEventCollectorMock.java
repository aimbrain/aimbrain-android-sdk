package com.aimbrain.sdk.mock;

import android.widget.EditText;

import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.textEvent.TextEventListener;

import java.util.WeakHashMap;


public class TextEventCollectorMock extends TextEventCollector {
    public TextEventCollectorMock() {
        super();
    }

    public WeakHashMap<EditText, TextEventListener> getEventListeners() {
        return getListeners();
    }
}

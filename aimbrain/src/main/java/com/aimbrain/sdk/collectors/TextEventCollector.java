package com.aimbrain.sdk.collectors;

import android.view.View;
import android.widget.EditText;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.models.TextEventModel;
import com.aimbrain.sdk.textEvent.TextEventListener;
import com.aimbrain.sdk.util.Logger;

import java.util.WeakHashMap;


public class TextEventCollector extends EventCollector {
    private static final String TAG = TextEventCollector.class.getSimpleName();

    private WeakHashMap<EditText, TextEventListener> mListeners;
    private static TextEventCollector mCollector;
    /**
     * average text event size in bytes
     */
    private final static int APPROXIMATE_TEXT_EVENT_SIZE_BYTES = 24;

    public static TextEventCollector getInstance() {
        if (mCollector == null) {
            mCollector = new TextEventCollector();
        }
        return mCollector;
    }

    protected TextEventCollector() {
        this.mListeners = new WeakHashMap<>();
    }

    public void textChanged(String text, long timestamp, View view) {
        Logger.v(TAG, "text changed in " + view);
        addCollectedData(new TextEventModel(text, timestamp, view));
    }

    public void attachTextChangedListener(EditText editText) {
        if (!mListeners.containsKey(editText) && !Manager.getInstance().isViewIgnored(editText)) {
            TextEventListener textEventListener = new TextEventListener(editText);
            mListeners.put(editText, textEventListener);
            editText.addTextChangedListener(textEventListener);
            Logger.v(TAG, "add text changed listener to " + editText);
        }

        if (mListeners.containsKey(editText) && Manager.getInstance().isViewIgnored(editText)) {
            editText.removeTextChangedListener(mListeners.get(editText));
            mListeners.remove(editText);
            Logger.v(TAG, "remove text changed listener to " + editText);
        }
    }

    public void stop() {
        Logger.v(TAG, "stop with " + mListeners.size() + " listeners");
        for (EditText editText : mListeners.keySet()) {
            editText.removeTextChangedListener(mListeners.get(editText));
        }
        mListeners.clear();
    }

    protected WeakHashMap<EditText, TextEventListener> getListeners() {
        return mListeners;
    }

    @Override
    public int sizeOfElements() {
        return getCountOfElements() * APPROXIMATE_TEXT_EVENT_SIZE_BYTES;
    }

    @Override
    int sizeOfElements(int count) {
        return count * APPROXIMATE_TEXT_EVENT_SIZE_BYTES;
    }
}

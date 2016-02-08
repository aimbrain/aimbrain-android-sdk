package com.aimbrain.sdk.collectors;

import android.view.View;
import android.widget.EditText;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.models.TextEventModel;
import com.aimbrain.sdk.textEvent.TextEventListener;

import java.util.WeakHashMap;


public class TextEventCollector extends EventCollector {

    private WeakHashMap<EditText, TextEventListener> mListeners;
    public static TextEventCollector mCollector;

    public static TextEventCollector getInstance(){
        if(mCollector == null){
            mCollector = new TextEventCollector();
        }
        return mCollector;
    }

    private TextEventCollector() {
        this.mListeners = new WeakHashMap<>();
    }

    public void textChanged(String text, long timestamp, View view) {
        addCollectedData(new TextEventModel(text, timestamp, view));
    }

    public void attachTextChangedListener(EditText editText) {
        if(!mListeners.containsKey(editText) && !Manager.getInstance().isViewIgnored(editText)){
            TextEventListener textEventListener = new TextEventListener(editText);
            mListeners.put(editText, textEventListener);
            editText.addTextChangedListener(textEventListener);
        }
        if(mListeners.containsKey(editText) && Manager.getInstance().isViewIgnored(editText)) {
            editText.removeTextChangedListener(mListeners.get(editText));
            mListeners.remove(editText);
        }
    }

    public void stop() {
        for(EditText editText : mListeners.keySet()) {
            editText.removeTextChangedListener(mListeners.get(editText));
        }
        mListeners.clear();
    }
}

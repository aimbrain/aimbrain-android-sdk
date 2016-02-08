package com.aimbrain.sdk.textEvent;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.collectors.TextEventCollector;


public class TextEventListener implements TextWatcher {

    private View view;

    public TextEventListener(View view) {
        this.view = view;
    }

    @Override

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        SensorEventCollector.getInstance().startCollectingData(500);
        TextEventCollector.getInstance().textChanged(s.toString(), System.currentTimeMillis(), view);
        Log.i("TEXT: ", s.toString());
    }
}

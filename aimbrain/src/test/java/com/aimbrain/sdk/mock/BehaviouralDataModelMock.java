package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.BehaviouralDataModel;
import com.aimbrain.sdk.models.EventModel;

import java.util.List;


public class BehaviouralDataModelMock extends BehaviouralDataModel {
    public BehaviouralDataModelMock(List<EventModel> textModels, List<EventModel> accelerometerModels, List<EventModel> touchModels, byte[] metadata) {
        super(textModels, accelerometerModels, touchModels, metadata);
    }

    public void setBase64Helper(Base64Helper base64) {
        setBase64(base64);
    }
}

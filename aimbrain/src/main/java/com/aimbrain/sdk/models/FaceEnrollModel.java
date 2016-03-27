package com.aimbrain.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public class FaceEnrollModel {

    private int imagesCount;

    public FaceEnrollModel(int imagesCount) {
        this.imagesCount = imagesCount;
    }

    public int getImagesCount() {
        return imagesCount;
    }

}

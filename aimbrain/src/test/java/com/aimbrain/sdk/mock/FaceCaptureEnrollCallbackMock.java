package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.android.volley.VolleyError;


public class FaceCaptureEnrollCallbackMock extends FaceCapturesEnrollCallback {

    @Override
    public void success(FaceEnrollModel response) {

    }

    @Override
    public void failure(VolleyError error) {

    }

    public void setBase64Helper(Base64Helper base64Helper) {
        setBase64(base64Helper);
    }
}

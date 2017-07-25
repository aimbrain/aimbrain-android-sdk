package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.FaceAuthenticateModel;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.android.volley.VolleyError;


public class FaceCaptureAuthenticateCallbackMock extends FaceCapturesAuthenticateCallback {

    @Override
    public void success(FaceAuthenticateModel response) {

    }

    @Override
    public void failure(VolleyError error) {

    }

    public void setBase64Helper(Base64Helper base64Helper) {
        setBase64(base64Helper);
    }
}

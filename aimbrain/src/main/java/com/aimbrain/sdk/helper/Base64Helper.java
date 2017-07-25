package com.aimbrain.sdk.helper;

import android.util.Base64;


public class Base64Helper {

    public String encodeToString(byte[] input, int flags) {
        return Base64.encodeToString(input, flags);
    }

    public byte[] decode(String input, int flags) {
        return Base64.decode(input, flags);
    }

}

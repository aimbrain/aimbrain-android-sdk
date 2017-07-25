package com.aimbrain.sdk.util;

import org.json.JSONObject;
import org.junit.Assert;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class JsonUtil {

    private final static double epsilon = 5.96e-08;

    public static void assertJSONFloatEquals(double first, float second) {
        assertTrue("float not equals, difference is too big", Math.abs(first - second) < epsilon);
    }

    public static void assertJsonHasKey(JSONObject o, String param) {
        Assert.assertTrue("require " + param + " parameter", o.has(param));
    }

    public static void assertJsonDoesNotHaveKey(JSONObject o, String param) {
        assertFalse("unnecessary " + param + " parameter", o.has(param));
    }
}

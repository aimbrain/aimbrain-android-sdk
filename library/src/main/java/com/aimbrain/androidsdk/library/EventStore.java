package com.aimbrain.androidsdk.library;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Static EventStore class for storing interactions with the app
 */
public class EventStore {

    private static JSONArray eventList = null;
    private static final Object sync = new Object();

    public static void addEvent(MotionEvent event, String context) {
        synchronized (sync) {
            if (eventList == null) {
                eventList = new JSONArray();
            }
        }

        JSONObject eventJson = new JSONObject();

        JSONArray eventArray = new JSONArray();

        final int historySize = event.getHistorySize();
        final int pointerCount = event.getPointerCount();

        // Iterate through MotionEvent history, used in case of swipe
        for (int h = 0; h < historySize; h++) {
            for (int p = 0; p < pointerCount; p++) {
                try {
                    JSONObject eventObj = new JSONObject()
                            .put("a", actionToString(event.getAction()))
                            .put("t", event.getHistoricalEventTime(h))
                            .put("i", event.getPointerId(p))
                            .put("x", event.getHistoricalX(p, h))
                            .put("y", event.getHistoricalY(p, h))
                            .put("s", event.getHistoricalSize(p, h))
                            .put("j", event.getHistoricalTouchMajor(p, h))
                            .put("n", event.getHistoricalTouchMinor(p, h))
                            .put("p", event.getHistoricalPressure(p, h))
                            .put("o", event.getHistoricalOrientation(p, h));

                    eventArray.put(eventObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // Parse current MotionEvent
        for (int p = 0; p < pointerCount; p++) {
            try {
                JSONObject eventObj = new JSONObject()
                        .put("a", actionToString(event.getAction()))
                        .put("t", event.getEventTime())
                        .put("i", event.getPointerId(p))
                        .put("x", event.getX(p))
                        .put("y", event.getY(p))
                        .put("s", event.getSize(p))
                        .put("j", event.getTouchMajor(p))
                        .put("n", event.getTouchMinor(p))
                        .put("p", event.getPressure(p))
                        .put("o", event.getOrientation(p));

                eventArray.put(eventObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            eventJson.put("c", context);
            eventJson.put("e", eventArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        synchronized (sync) {
            eventList.put(eventJson);
        }
    }

    public static void addEvent(int keyCode, KeyEvent event, String context) {
        synchronized (sync) {
            if (eventList == null) {
                eventList = new JSONArray();
            }
        }
        JSONObject eventJson = new JSONObject();
        try {
            eventJson = new JSONObject()
                    .put("a", actionToString(event.getAction()))
                    .put("t", event.getEventTime())
                    .put("k", keyCode)
                    .put("d", event.getDownTime())
                    .put("c", context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        synchronized (sync) {
            eventList.put(eventJson);
        }
    }

    public static void addEvent(long time, int i, String context) {
        synchronized (sync) {
            if (eventList == null) {
                eventList = new JSONArray();
            }
        }
        JSONObject eventJson = new JSONObject();
        try {
            eventJson = new JSONObject()
                    .put("t", time)
                    .put("l", i)
                    .put("c", context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        synchronized (sync) {
            eventList.put(eventJson);
        }
    }

    public static synchronized String getEvents() {
        return eventList.toString();
    }

    private static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "ACTION_HOVER_EXIT";
        }
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }
}

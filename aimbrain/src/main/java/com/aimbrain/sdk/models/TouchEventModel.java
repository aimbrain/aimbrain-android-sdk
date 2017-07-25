package com.aimbrain.sdk.models;

import android.graphics.PointF;
import android.support.annotation.VisibleForTesting;
import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.util.Logger;
import com.aimbrain.sdk.viewManager.ViewIdMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;


public class TouchEventModel extends EventModel {
    private static final String TAG = TouchEventModel.class.getSimpleName();
    private LinkedList<String> viewPath;
    private int groupId;
    private int touchId;
    private PointF absolutePoint;
    private PointF relativePoint;
    private float force;
    private float radius;
    private int phase;

    public TouchEventModel(int touchId, int groupId, MotionEvent event, long timestamp, int pointerIndex, View view, boolean sensitive) {
        this.touchId = touchId;
        this.groupId = groupId;
        if(!sensitive)
            this.absolutePoint = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
        else
            this.absolutePoint = new PointF(0,0);
        this.viewPath = ViewIdMapper.getInstance().extractViewPath(view);
        int[] viewCoords = new int[2];
        view.getLocationOnScreen(viewCoords);
        this.relativePoint = new PointF(event.getX() - viewCoords[0], event.getY() - viewCoords[1]);
        this.force = event.getPressure(pointerIndex);
        this.radius = event.getTouchMajor(pointerIndex);
        this.phase = convertToIosTouchPhase(event.getActionMasked());
        this.timestamp = timestamp;
        Logger.v(TAG, "touchId:" + touchId + " groupId:" + groupId + " absolutePoint:" + absolutePoint + " relativePoint:" + relativePoint + " force:"+force + " radius:" + radius + " phase:" + phase + " timestamp:" + timestamp );
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ids", new JSONArray(viewPath));
        jsonObject.put("gid", groupId);
        jsonObject.put("tid", touchId);
        jsonObject.put("t", timestamp);
        jsonObject.put("r", radius);
        jsonObject.put("x", absolutePoint.x);
        jsonObject.put("y", absolutePoint.y);
        jsonObject.put("rx", relativePoint.x);
        jsonObject.put("ry", relativePoint.y);
        jsonObject.put("f", force);
        jsonObject.put("p", phase);
        return jsonObject;
    }

    @VisibleForTesting
    protected int convertToIosTouchPhase(int actionMasked) {
        switch(actionMasked) {
            case MotionEvent.ACTION_DOWN:
                return 0;
            case MotionEvent.ACTION_POINTER_DOWN:
                return 0;
            case MotionEvent.ACTION_POINTER_UP:
                return 3;
            case MotionEvent.ACTION_UP:
                return 3;
            case MotionEvent.ACTION_MOVE:
                return 1;
            case MotionEvent.ACTION_CANCEL:
                return 4;
            default:
                return actionMasked;
        }
    }
}

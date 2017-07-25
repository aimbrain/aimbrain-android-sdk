package com.aimbrain.sdk.collectors;

import android.support.annotation.VisibleForTesting;
import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.models.TouchEventModel;
import com.aimbrain.sdk.privacy.SensitiveViewGuard;
import com.aimbrain.sdk.util.Logger;

import java.util.HashMap;


public class MotionEventCollector extends EventCollector {
    private static final String TAG = MotionEventCollector.class.getSimpleName();

    private int mTouchIdCount;
    private int mPointerCount;
    private HashMap<Integer, Integer> mPointerIdMap;
    private static MotionEventCollector mCollector;
    /**
     * average motion event size in bytes
     */
    private final static int APPROXIMATE_MOTION_EVENT_SIZE_BYTES = 48;

    public static MotionEventCollector getInstance() {
        if (mCollector == null) {
            mCollector = new MotionEventCollector();
        }
        return mCollector;
    }

    @VisibleForTesting
    protected MotionEventCollector() {
        this.mTouchIdCount = 0;
        this.mPointerCount = 0;
        this.mPointerIdMap = new HashMap<>();
    }

    private void processMotionEvent(MotionEvent motionEvent, View view, long timestamp, int pointerIndex) {
        int pointerId = motionEvent.getPointerId(pointerIndex);
        int touchId;

        boolean ignored = Manager.getInstance().isViewIgnored(view);
        boolean sensitive = SensitiveViewGuard.isViewSensitive(view);

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchId = this.mTouchIdCount + mPointerCount;
                mPointerIdMap.put(pointerId, mPointerCount++);
                if (!ignored) {
                    addCollectedData(new TouchEventModel(touchId, touchId, motionEvent, timestamp, pointerIndex, view, sensitive));
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                touchId = this.mTouchIdCount + mPointerCount;
                mPointerIdMap.put(pointerId, mPointerCount++);
                if (!ignored) {
                    addCollectedData(new TouchEventModel(touchId, touchId, motionEvent, timestamp, pointerIndex, view, sensitive));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPointerIdMap.get(pointerId) == null) {
                    mPointerIdMap.put(pointerId, mPointerCount++);
                }
                touchId = mTouchIdCount + mPointerIdMap.get(pointerId);
                if (!ignored) {
                    addCollectedData(new TouchEventModel(touchId, touchId, motionEvent, timestamp, pointerIndex, view, sensitive));
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mPointerIdMap.get(pointerId) == null) {
                    mPointerIdMap.put(pointerId, mPointerCount++);
                }
                touchId = this.mTouchIdCount + mPointerIdMap.get(pointerId);
                if (!ignored) {
                    addCollectedData(new TouchEventModel(touchId, touchId, motionEvent, timestamp, pointerIndex, view, sensitive));
                }
                mPointerIdMap.remove(pointerId);
                break;
            case MotionEvent.ACTION_UP:
                if (mPointerIdMap.get(pointerId) == null) {
                    mPointerIdMap.put(pointerId, mPointerCount++);
                }
                touchId = this.mTouchIdCount + mPointerIdMap.get(pointerId);
                if (!ignored) {
                    addCollectedData(new TouchEventModel(touchId, touchId, motionEvent, timestamp, pointerIndex, view, sensitive));
                }
                mPointerIdMap.clear();
                mTouchIdCount += mPointerCount;
                mPointerCount = 0;
                break;
            default:
                break;
        }
    }

    public void processMotionEvent(MotionEvent motionEvent, View view, long timestamp) {
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN) {
            int pointerIndex = motionEvent.getActionIndex();
            Logger.v(TAG, "motion event, action:" + action + " pointerIndex:" + pointerIndex + " pointerId:" + motionEvent.getPointerId(pointerIndex));
            processMotionEvent(motionEvent, view, timestamp, pointerIndex);
        } else {
            int pointerCount = motionEvent.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                Logger.v(TAG, "motion event, action:" + action + " pointerIndex:" + i + " pointerId:" + motionEvent.getPointerId(i));
                processMotionEvent(motionEvent, view, timestamp, i);
            }
        }
    }

    @VisibleForTesting
    protected HashMap<Integer, Integer> getmPointerIdMap() {
        return mPointerIdMap;
    }

    @Override
    public int sizeOfElements() {
        return getCountOfElements() * APPROXIMATE_MOTION_EVENT_SIZE_BYTES;
    }

    @Override
    int sizeOfElements(int count) {
        return count * APPROXIMATE_MOTION_EVENT_SIZE_BYTES;
    }

}

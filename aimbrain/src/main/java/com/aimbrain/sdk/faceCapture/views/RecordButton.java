package com.aimbrain.sdk.faceCapture.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.aimbrain.R;

public class RecordButton extends View {

    private static final int FRAMES_PER_SECOND = 60;

    private static final int RECTANGLE_ANIMATION_FRAMES = FRAMES_PER_SECOND / 7;
    private static final int CIRCLE_ANIMATION_FRAMES = FRAMES_PER_SECOND / 5;
    private static final int ANIMATING_CIRCLES = 7;

    private static final int STATE_IDLE = 0;
    private static final int STATE_IDLE_TOUCHED = 1;
    private static final int STATE_RECORDING = 2;

    private int state = STATE_IDLE;

    private static final float CIRCLE_RADIUS_RATIO = 0.7f;
    private static final float CENTER_CIRCLE_RADIUS_RATIO = 0.35f;

    private int frame = 1;
    RectF centerRect;

    public RecordButton(Context context) {
        super(context);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startRecording() {
        state = STATE_RECORDING;
    }

    public void stopRecording() {
        state = STATE_IDLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float radius = centerX * CIRCLE_RADIUS_RATIO;
        float smallRadius = radius * CENTER_CIRCLE_RADIUS_RATIO;

        if (state == STATE_IDLE || state == STATE_IDLE_TOUCHED) {
            drawStartRecordingButton(canvas, centerX, centerY, radius, smallRadius);
            frame = 1;
        } else {

            if (centerRect == null) {
                float left = centerX - smallRadius;
                float right = centerX + smallRadius;
                float top = centerY - smallRadius;
                float bottom = centerY + smallRadius;
                centerRect = new RectF(left, top, bottom, right);
            }

            drawRecordingButton(canvas, centerX, centerY, radius, smallRadius, centerRect, frame);
            frame++;
            if (state == STATE_RECORDING) {
                this.postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
            }
        }
    }


    private void drawRecordingButton(Canvas canvas, float centerX,
                                     float centerY, float radius,
                                     float smallRadius, RectF rect,
                                     int frame) {

        drawAnimatingCircles(canvas, centerX, centerY, frame);

        drawCenterCircles(canvas, centerX, centerY, radius, smallRadius, rect, frame);
    }

    private void drawCenterCircles(Canvas canvas, float centerX, float centerY,
                                   float radius, float smallRadius, RectF rect, int frame) {
        Paint paint = getPaint();
        paint.setColor(getButtonColor());
        canvas.drawCircle(centerX, centerY, radius, paint);

        paint.setColor(getResources().getColor(R.color.color_record_center));

        float rectangleRadius = 0;
        if (frame < RECTANGLE_ANIMATION_FRAMES) {
            float duration = RECTANGLE_ANIMATION_FRAMES / (float) frame;
            rectangleRadius = smallRadius - (smallRadius / duration);
        }
        canvas.drawRoundRect(rect, rectangleRadius, rectangleRadius, paint);
    }

    private void drawAnimatingCircles(Canvas canvas, float centerX, float centerY, int frame) {
        int realFrame = frame;
        if (realFrame > CIRCLE_ANIMATION_FRAMES) {
            realFrame = (frame % CIRCLE_ANIMATION_FRAMES) + 1;
        }

        float duration = (float) realFrame / CIRCLE_ANIMATION_FRAMES;
        float radiusCorrection = (float) (centerX * 0.05 * duration);

        for (int i = 1; i <= ANIMATING_CIRCLES; i++) {
            drawAnimatingCircle(canvas, centerX, centerY, radiusCorrection, duration, i);
        }
    }


    private void drawAnimatingCircle(Canvas canvas, float centerX,
                                     float centerY, float radiusCorrection,
                                     float duration, int circleNumber) {

        Paint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.color_record_rings));
        paint.setStrokeWidth((float) (centerX * 0.015));

        float maxAlpha = 255 - ((255 / ANIMATING_CIRCLES) * (circleNumber - 1));
        float minAlpha = 255 - ((255 / ANIMATING_CIRCLES) * circleNumber);
        paint.setAlpha((int) (maxAlpha - ((maxAlpha - minAlpha) * duration)));
        float radius = (float) (centerX * (0.6 + (circleNumber * 0.05)) + radiusCorrection);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    private void drawStartRecordingButton(Canvas canvas, float centerX,
                                          float centerY, float radius, float smallRadius) {
        Paint paint = getPaint();
        paint.setColor(getButtonColor());
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setColor(getResources().getColor(R.color.color_record_center));
        canvas.drawCircle(centerX, centerY, smallRadius, paint);
    }


    private Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        return paint;
    }

    private int getButtonColor() {
        switch (state) {
            case STATE_IDLE:
                return getResources().getColor(R.color.color_record_button_idle);
            case STATE_IDLE_TOUCHED:
                return getResources().getColor(R.color.color_record_button_touched);
            case STATE_RECORDING:
                return getResources().getColor(R.color.color_record_button_recording);
            default:
                return getResources().getColor(R.color.color_record_button_idle);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (state == STATE_IDLE) {
                    state = STATE_IDLE_TOUCHED;
                }

                break;
            case MotionEvent.ACTION_UP:
                if (state == STATE_IDLE_TOUCHED) {
                    state = STATE_IDLE;
                }
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
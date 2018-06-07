package com.aimbrain.sdk.faceCapture.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.aimbrain.R;

public class RecordButton extends View {
    private static final int FRAMES_PER_SECOND = 20;

    private static final int STATE_IDLE = 0;
    private static final int STATE_IDLE_TOUCHED = 1;
    private static final int STATE_PREPARING_RECORDING = 2;
    private static final int STATE_RECORDING = 3;
    private static final int STATE_RECORDED = 4;

    private int state = STATE_IDLE;

    private static final float RADIUS_RATIO = 0.95f;
    private static final float INNER_RADIUS_RATIO = 0.4f;

    private long animationStart;
    private int animationDuration;
    private int recordDuration;

    private RectF innerRect = new RectF();
    private RectF outerBorderRect = new RectF();
    private Paint paint;

    public RecordButton(Context context) {
        super(context);
        initView();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void startRecording(int prepare, int record) {
        recordDuration = record;
        animationStart = System.currentTimeMillis();

        if (prepare > 0) {
            animationDuration = prepare;
            state = STATE_PREPARING_RECORDING;
        }
        else {
            animationDuration = record;
            state = STATE_RECORDING;
        }
    }

    public void stopRecording() {
        if (state != STATE_RECORDED) {
            state = STATE_RECORDED;
            animationStart = System.currentTimeMillis();
            animationDuration = 500;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float radius = Math.min(cx, cy) * RADIUS_RATIO;
        float innerRadius = radius * INNER_RADIUS_RATIO;

        innerRect.left = cx - innerRadius;
        innerRect.right = cx + innerRadius;
        innerRect.top = cy - innerRadius;
        innerRect.bottom = cy + innerRadius;

        float border = 3.0f;
        outerBorderRect.left = cx - radius - border;
        outerBorderRect.right = cx + radius + border;
        outerBorderRect.top = cy - radius - border;
        outerBorderRect.bottom = cy + radius + border;

        Resources res = getResources();

        if (state == STATE_IDLE) {
            paint.setColor(res.getColor(R.color.color_record_button_idle));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(res.getColor(R.color.color_record_center));
            canvas.drawCircle(cx, cy, innerRadius, paint);
        }
        else if (state == STATE_IDLE_TOUCHED) {
            paint.setColor(res.getColor(R.color.color_record_button_touched));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(res.getColor(R.color.color_record_center));
            canvas.drawCircle(cx, cy, innerRadius, paint);
        }
        else if (state == STATE_PREPARING_RECORDING) {
            float progress = currentAnimationProgress();
            int green = res.getColor(R.color.color_record_button_idle);
            int red = res.getColor(R.color.color_record_button_recording);
            int color = interpolateColor(green, red, progress);
            paint.setColor(color);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(res.getColor(R.color.color_record_center));
            float rectangleRadius = innerRadius - (innerRadius * progress);
            canvas.drawRoundRect(innerRect, rectangleRadius, rectangleRadius, paint);
            if (progress >= 1.0) {
                state = STATE_RECORDING;
                animationStart = System.currentTimeMillis();
                animationDuration = recordDuration;
            }
            postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
        }
        else if (state == STATE_RECORDING) {
            float progress = currentAnimationProgress();
            paint.setColor(res.getColor(R.color.color_record_button_progress));
            paint.setStrokeWidth(2.0f);
            canvas.drawArc(outerBorderRect, 0, progress * 360.0f, true, paint);
            paint.setColor(res.getColor(R.color.color_record_button_recording));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(res.getColor(R.color.color_record_center));
            canvas.drawRoundRect(innerRect, 0, 0, paint);
            postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
            if (progress >= 1.0) {
                state = STATE_RECORDED;
                animationStart = System.currentTimeMillis();
                animationDuration = 500;
            }
        }
        else if (state == STATE_RECORDED) {
            int red = res.getColor(R.color.color_record_button_recording);
            int redFinal = res.getColor(R.color.color_record_button_recorded);
            float progress = currentAnimationProgress();
            int color = interpolateColor(red, redFinal, progress);
            paint.setColor(color);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(res.getColor(R.color.color_recorded_center));
            canvas.drawRoundRect(innerRect, 0, 0, paint);
            if (progress < 1.0f) {
                postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
            }
        }
    }

    private float currentAnimationProgress() {
        return Math.min(1.0f, (System.currentTimeMillis() - animationStart) / (float)animationDuration);
    }

    private float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    private int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
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
        int heightMeasure = widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasure);
    }
}
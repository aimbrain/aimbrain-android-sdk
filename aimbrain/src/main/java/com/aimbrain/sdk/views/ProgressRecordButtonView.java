package com.aimbrain.sdk.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.voiceCapture.RecordButtonView;

import static android.support.v4.content.ContextCompat.getColor;

public class ProgressRecordButtonView extends RecordButtonView {

    private static final int FRAMES_PER_SECOND = 50;

    private static final int RECTANGLE_ANIMATION_FRAMES = FRAMES_PER_SECOND / 7;

    private static final int STATE_IDLE = R.color.color_record_button_idle;
    private static final int STATE_IDLE_TOUCHED = R.color.color_record_button_touched;
    private static final int STATE_RECORDING = R.color.color_record_button_recording;

    private static final float CIRCLE_RADIUS_RATIO = 0.7f;
    private static final float CENTER_CIRCLE_RADIUS_RATIO = 0.35f;
    private int state = STATE_IDLE;
    private int frame = 1;

    private long startTime;
    private long endTime;

    private PointF center = new PointF();
    private RectF buttonBounds = new RectF();
    private RectF iconBounds = new RectF();
    private float buttonRadius;
    private float iconRadius;

    private Paint progressPaint;
    private Paint iconPaint;
    private Paint buttonPaint;

    public ProgressRecordButtonView(Context context) {
        super(context);
        init(context);
    }

    public ProgressRecordButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressRecordButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(getColor(context, R.color.color_record_button_progress));
        progressPaint.setStrokeWidth(dpToPx(3));

        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setColor(getColor(context, R.color.color_record_center));

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        initDimensions();
    }

    private void initDimensions() {
        center.set(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        buttonRadius = center.x * CIRCLE_RADIUS_RATIO;
        float circleSize = buttonRadius + dpToPx(1);
        buttonBounds.set(
                center.x - circleSize, center.y - circleSize,
                center.x + circleSize, center.y + circleSize);
        iconRadius = buttonRadius * CENTER_CIRCLE_RADIUS_RATIO;
        iconBounds.set(
                center.x - iconRadius, center.y - iconRadius,
                center.x + iconRadius, center.y + iconRadius);
    }

    @Override
    public void showRecordingStarted(int recordingTimeSeconds) {
        state = STATE_RECORDING;
        startTime = System.currentTimeMillis();
        endTime = startTime + (recordingTimeSeconds * 1000);
        invalidate();
    }

    @Override
    public void showRecordingStopped() {
        state = STATE_IDLE;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (state == STATE_IDLE || state == STATE_IDLE_TOUCHED) {
            drawStartRecordingButton(canvas);
            frame = 1;
        } else {
            drawRecordingButton(canvas);
            frame++;
            if (state == STATE_RECORDING) {
                this.postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
            }
        }
    }

    private void drawRecordingButton(Canvas canvas) {
        drawProgressCircle(canvas);
        drawCenterCircles(canvas);
    }

    private void drawCenterCircles(Canvas canvas) {
        canvas.drawCircle(center.x, center.y, buttonRadius, getButtonBgPaint());
        float rectangleRadius = 0;
        if (frame < RECTANGLE_ANIMATION_FRAMES) {
            float duration = RECTANGLE_ANIMATION_FRAMES / (float) frame;
            rectangleRadius = iconRadius - (iconRadius / duration);
        }
        canvas.drawRoundRect(iconBounds, rectangleRadius, rectangleRadius, iconPaint);
    }

    private void drawProgressCircle(Canvas canvas) {
        float past = System.currentTimeMillis() - startTime;
        float goal = endTime - startTime;
        float progress = past / goal;
        canvas.drawArc(buttonBounds, -88, (360 * progress), true, progressPaint);
    }

    private void drawStartRecordingButton(Canvas canvas) {
        canvas.drawCircle(center.x, center.y, buttonRadius, getButtonBgPaint());
        canvas.drawCircle(center.x, center.y, iconRadius, iconPaint);
    }

    private Paint getButtonBgPaint() {
        buttonPaint.setColor(getColor(getContext(), state));
        return buttonPaint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (STATE_RECORDING != state) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                state = STATE_IDLE_TOUCHED;
            } else if (MotionEvent.ACTION_UP == event.getAction()) {
                state = STATE_IDLE;
            }
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    public void setProgressColorResource(@ColorRes int colorResource) {
        progressPaint.setColor(getColor(getContext(), colorResource));
        invalidate();
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    private float dpToPx(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
                Resources.getSystem().getDisplayMetrics());
    }
}
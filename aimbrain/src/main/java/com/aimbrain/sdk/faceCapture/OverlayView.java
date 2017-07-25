package com.aimbrain.sdk.faceCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    private Paint backgroundPaint;
    private Paint transparentPaint;
    private Bitmap bitmap;
    private Canvas canvas;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(128);

        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (bitmap == null) {
            createBitmap(w, h);
        } else if (w != oldw || h != oldh) {
            createBitmap(w, h);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void createBitmap(int w, int h) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawRect(new RectF(0, 0, w, h), backgroundPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clipRoundRect(getWidth(), getHeight());
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public RectF getMaskBounds(int w, int h) {
        float maskWidth = getMaskWidth(w);
        float maskHeight = getMaskHeight(w);
        float horizontalMargin = (float) (((float) w - maskWidth) / 2.0);
        float verticalMargin = (float) (((float) h - maskHeight) / 2.0);
        return new RectF(horizontalMargin, verticalMargin, horizontalMargin + maskWidth, verticalMargin + maskHeight);
    }

    public float getMaskHeight(int w) {
        return (float) (FaceCaptureActivity.BOX_RATIO * getMaskWidth(w));
    }

    public float getMaskWidth(int w) {
        return (float) (w * FaceCaptureActivity.BOX_WIDTH);
    }

    private void clipRoundRect(int w, int h) {
        RectF maskBounds = getMaskBounds(w, h);
        canvas.drawRoundRect(maskBounds, maskBounds.top, maskBounds.top, transparentPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bitmap = null;
        canvas = null;
    }
}

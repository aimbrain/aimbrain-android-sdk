package com.aimbrain.sdk.faceCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bmp = drawBitmap(getMeasuredWidth(), getMeasuredHeight());
        canvas.drawBitmap(bmp, 0, 0, null);
    }

    public RectF getMaskBounds() {
        float maskWidth = getMaskWidth();
        float maskHeight = getMaskHeight();
        float horizontalMargin = (float) (((float) getMeasuredWidth() - maskWidth) / 2.0);
        float verticalMargin = (float) (((float) getMeasuredHeight() - maskHeight) / 2.0);
        return new RectF(horizontalMargin, verticalMargin, horizontalMargin + maskWidth, verticalMargin + maskHeight);
    }

    public float getMaskHeight() {
        return (float) (FaceCaptureActivity.BOX_RATIO * getMaskWidth());
    }

    public float getMaskWidth() {
        return (float) (getMeasuredWidth() * FaceCaptureActivity.BOX_WIDTH);
    }

    @NonNull
    private Paint getOverlayBackgroundPaint() {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(128);
        return backgroundPaint;
    }

    @NonNull
    private Paint getTransparentPaint() {
        Paint transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setStyle(Paint.Style.FILL);
        return transparentPaint;
    }

    private Bitmap drawBitmap(int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        RectF maskBounds = getMaskBounds();
        canvas.drawRect(new RectF(0, 0, (float) getMeasuredWidth(), (float) getMeasuredHeight()), getOverlayBackgroundPaint());
        canvas.drawRoundRect(maskBounds, maskBounds.top, maskBounds.top, getTransparentPaint());
        return bitmap;
    }
}

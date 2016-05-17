package com.aimbrain.sdk.faceCapture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import static android.view.ViewGroup.*;

public class OverlaySurfaceView extends AutoFitSurfaceView implements SurfaceHolder.Callback {

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    public OverlaySurfaceView(Context context) {
        super(context);
        setDefaultOptions();
    }

    public OverlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultOptions();
    }

    public OverlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaultOptions();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!hasValidAspectRatio()) {
            // leave old code for compatibility wit picture taking mode
            PreviewManager.setPreviewTopBottomMargins(height, this);
        }

        mSurfaceWidth = width;
        mSurfaceHeight = height;

        Canvas canvas = holder.lockCanvas();
        Context context = getContext();
        if (context instanceof FaceCaptureActivity) {
            ((FaceCaptureActivity) context).refreshOverlayElements();
        }

        RectF content = getClippedContentRect();

        int overdraw = 3; // draw larger rect to avoid seams on the sides
        canvas.drawRect(new RectF(content.left - overdraw, content.top - overdraw, content.right + overdraw, content.bottom + overdraw), getOverlayBackgroundPaint());

        RectF mask = getMaskCanvasRect();
        float maskHeight = mask.height();

        canvas.drawRoundRect(mask, mask.top, mask.top, getTransparentPaint());
        canvas.drawLine(mask.left, (float) (mask.top + maskHeight / 2.7), mask.right, (float) (mask.top + maskHeight / 2.7), getStrokePaint());
        canvas.drawLine(mask.left, (float) (mask.top + maskHeight / 2.0), mask.right, (float) (mask.top + maskHeight / 2.0), getStrokePaint());

        holder.unlockCanvasAndPost(canvas);
    }

    public RectF getMaskBounds() {
        return getMaskBoundsOld();
    }

    private RectF getMaskCanvasRect() {
        RectF contentRect = getClippedContentRect();
        float width = (float) (FaceCaptureActivity.BOX_WIDTH * contentRect.width());
        float height = (float) (FaceCaptureActivity.BOX_RATIO * width);
        float mh = contentRect.width() - width;
        float mv = contentRect.height() - height;
        return new RectF(contentRect.left + mh / 2, contentRect.top + mv / 2, contentRect.right - mh / 2, contentRect.bottom - mv / 2);
    }

    /**
     * Calculate visible rect in surface coordiantes.
     */
    private RectF getClippedContentRect() {
        /*
            This view assumes it is displayed full screen with
            negative margins used for clipping.
         */
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        float scale = getMeasuredWidth() / (float) mSurfaceWidth;
        float left = 0.0f;
        if (lp.leftMargin < 0) {
            left -= lp.leftMargin / scale;
        }
        float right = mSurfaceWidth;
        if (lp.rightMargin < 0) {
            right += lp.rightMargin / scale;
        }
        float top = 0.0f;
        if (lp.topMargin < 0) {
            top -= lp.topMargin / scale;
        }
        float bottom = mSurfaceHeight;
        if (lp.bottomMargin < 0.0) {
            bottom += lp.bottomMargin / scale;
        }
        return new RectF(left, top, right, bottom);
    }

    @Deprecated
    @NonNull
    private RectF getMaskBoundsOld() {
        // todo: remove this and move everything to aspect ratio based scaling
        SurfaceHolder holder = getHolder();
        float maskWidth = getMaskWidth();
        float maskHeight = getMaskHeight();
        float horizontalMargin = (float) (((float) getHolder().getSurfaceFrame().width() - maskWidth) / 2.0);
        float verticalMargin = (float) (((float) holder.getSurfaceFrame().height() - maskHeight) / 2.0);
        return new RectF(horizontalMargin, verticalMargin, horizontalMargin + maskWidth, verticalMargin + maskHeight);
    }

    @Deprecated
    public float getMaskHeight() {
        return (float) (FaceCaptureActivity.BOX_RATIO * getMaskWidth());
    }

    @Deprecated
    public float getMaskWidth() {
        return (float) (getHolder().getSurfaceFrame().width() * FaceCaptureActivity.BOX_WIDTH);
    }

    @NonNull
    private Paint getStrokePaint() {
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(1);
        strokePaint.setStyle(Paint.Style.STROKE);
        return strokePaint;
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
        Paint transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setStyle(Paint.Style.FILL);
        return transparentPaint;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void setDefaultOptions() {
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
        setZOrderMediaOverlay(true);
    }
}

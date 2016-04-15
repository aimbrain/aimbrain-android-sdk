package com.aimbrain.sdk.faceCapture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OverlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback{
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
        Canvas canvas = holder.lockCanvas();

        RectF maskBounds = getMaskBounds();
        float maskHeight = getMaskHeight();
        float maskWidth = getMaskWidth();

        Context context = getContext();
        if(context instanceof FaceCaptureActivity)
        {
            ((FaceCaptureActivity)context).refreshOverlayElements();
        }
        canvas.drawRect(new RectF(0, 0, (float) holder.getSurfaceFrame().width(), (float) holder.getSurfaceFrame().height()), getOverlayBackgroundPaint());
        canvas.drawRoundRect(maskBounds, maskBounds.top, maskBounds.top, getTransparentPaint());

        canvas.drawLine(maskBounds.left , (float) (maskBounds.top + maskHeight / 2.7), maskBounds.right, (float)(maskBounds.top + maskHeight/2.7), getStrokePaint());
        canvas.drawLine(maskBounds.left, (float) (maskBounds.top + maskHeight / 2.0), maskBounds.right, (float) (maskBounds.top + maskHeight / 2.0), getStrokePaint());

        holder.unlockCanvasAndPost(canvas);
      }

      public RectF getMaskBounds() {
        SurfaceHolder holder = getHolder();
        float maskWidth = getMaskWidth();
        float maskHeight = getMaskHeight();
        float horizontalMargin = (float) (((float)getHolder().getSurfaceFrame().width() - maskWidth) / 2.0);
        float verticalMargin = (float) (((float)holder.getSurfaceFrame().height() - maskHeight) / 2.0);
        return new RectF(horizontalMargin, verticalMargin, horizontalMargin+maskWidth, verticalMargin+maskHeight);
      }

      public float getMaskHeight() {
        return (float) (FaceCaptureActivity.BOX_RATIO * getMaskWidth());
      }

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

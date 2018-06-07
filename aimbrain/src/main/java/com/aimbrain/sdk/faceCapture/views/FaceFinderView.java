package com.aimbrain.sdk.faceCapture.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FaceFinderView extends View {
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private Paint mBackgroundPaint;
    private Paint mTransparentPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mFinderWidth;
    private int mFinderHeight;

    public FaceFinderView(Context context) {
        super(context);
        initView();
    }

    public FaceFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FaceFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    public boolean hasValidAspectRatio() {
        return mRatioWidth > 0 && mRatioHeight > 0;
    }

    public float getAspectRatio() {
        return mRatioWidth / (float) mRatioHeight;
    }


    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }


    public void setFinderSize(int width, int height) {
        mFinderWidth = width;
        mFinderHeight = height;
    }

    private void initView() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setAlpha(128);
        mTransparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mTransparentPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (!hasValidAspectRatio()) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioHeight / mRatioWidth) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mBitmap == null) {
            createBitmap(w, h);
        } else if (w != oldw || h != oldh) {
            createBitmap(w, h);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void createBitmap(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawRect(new RectF(0, 0, w, h), mBackgroundPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getWidth();
        int height = getHeight();
        int left = (width - mFinderWidth) / 2;
        int top = (height - mFinderHeight) / 2;

        RectF bounds = new RectF(left, top, left + mFinderWidth, top + mFinderHeight);
        float radius = Math.min(bounds.width() / 2, bounds.height() / 2);
        mCanvas.drawRoundRect(bounds, radius, radius, mTransparentPaint);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap = null;
        mCanvas = null;
    }
}

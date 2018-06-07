package com.aimbrain.sdk.faceCapture.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class CameraUiView extends ViewGroup {
    private static final String TAG = CameraUiView.class.getSimpleName();

    private String mUpperText = "";
    private String mLowerText = "";
    private String mProgressText = "";
    private String mRecordHintText = "";

    private OnRecordClickListener mOnRecordClickListener;
    private OnFaceFinderResizeListener mOnFaceFinderResizeListener;
    private Rect mCameraPosition = new Rect();
    private long mCountdownStart;
    private int mCountdownDuration;

    private RecordButton mRecordButton;
    private TextView mUpperTextView;
    private TextView mLowerTextView;
    private TextView mHintTextView;
    private TextView mProgressTextView;

    public CameraUiView(Context context) {
        super(context);
        initView();
    }

    public CameraUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraUiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraUiView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        Context context = getContext();
        mRecordButton = new RecordButton(context);
        mRecordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnRecordClickListener != null) {
                    mOnRecordClickListener.onRecordClick(mRecordButton);
                }
            }
        });
        addView(mRecordButton);

        Typeface typefaceLight = Typeface.create("sans-serif-light", Typeface.NORMAL);
        Typeface typefaceBold = Typeface.create("sans-serif-bold", Typeface.NORMAL);

        mUpperTextView = new TextView(context);
        mUpperTextView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        mUpperTextView.setGravity(Gravity.CENTER);
        mUpperTextView.setTextColor(Color.WHITE);
        mUpperTextView.setMaxLines(3);
        mUpperTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mUpperTextView.setEllipsize(TextUtils.TruncateAt.END);
        mUpperTextView.setText(mUpperText);
        mUpperTextView.setTypeface(typefaceLight);
        addView(mUpperTextView);

        mLowerTextView = new TextView(context);
        mLowerTextView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        mLowerTextView.setGravity(Gravity.CENTER);
        mLowerTextView.setTextColor(Color.WHITE);
        mLowerTextView.setMaxLines(3);
        mLowerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mLowerTextView.setEllipsize(TextUtils.TruncateAt.END);
        mLowerTextView.setText(mLowerText);
        mLowerTextView.setTypeface(typefaceLight);
        addView(mLowerTextView);

        mHintTextView = new TextView(context);
        mHintTextView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        mHintTextView.setGravity(Gravity.CENTER);
        mHintTextView.setTextColor(Color.WHITE);
        mHintTextView.setMaxLines(3);
        mHintTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        mHintTextView.setEllipsize(TextUtils.TruncateAt.END);
        mHintTextView.setText(mRecordHintText);
        mHintTextView.setTypeface(typefaceBold);
        mHintTextView.setVisibility(INVISIBLE);
        addView(mHintTextView);

        mProgressTextView = new TextView(context);
        mProgressTextView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        mProgressTextView.setGravity(Gravity.CENTER);
        mProgressTextView.setTextColor(Color.WHITE);
        mProgressTextView.setMaxLines(3);
        mProgressTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mProgressTextView.setEllipsize(TextUtils.TruncateAt.END);
        mProgressTextView.setText(mProgressText);
        mProgressTextView.setVisibility(INVISIBLE);
        mProgressTextView.setTypeface(typefaceLight);
        addView(mProgressTextView);
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.mOnRecordClickListener = listener;
    }

    public void setOnFaceFinderResizeListener(OnFaceFinderResizeListener listener) {
        this.mOnFaceFinderResizeListener = listener;
    }

    public void setRecordStarted(int prepareDuration, final int duration) {
        mLowerTextView.setVisibility(INVISIBLE);
        mHintTextView.setVisibility(INVISIBLE);
        mRecordButton.startRecording(prepareDuration, duration);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                beginsShowingProgress(duration);
            }
        }, prepareDuration);
    }

    private void beginsShowingProgress(int duration) {
        mProgressTextView.setVisibility(VISIBLE);
        mLowerTextView.setVisibility(INVISIBLE);
        mHintTextView.setVisibility(VISIBLE);
        mCountdownStart = System.currentTimeMillis();
        mCountdownDuration = duration;
        setInitialProgress();
        startCountdownUpdates();
    }

    private void setInitialProgress() {
        int remainSec = Math.max(mCountdownDuration / 1000, 1);
        mProgressTextView.setText(mProgressText.replace("{0}", String.valueOf(remainSec)));
    }

    private void startCountdownUpdates() {
        mCountdownHandler.removeCallbacks(mUpdateCountdown);
        mCountdownHandler.postDelayed(mUpdateCountdown, 250);
    }

    private Handler mCountdownHandler = new Handler();
    private Runnable mUpdateCountdown = new Runnable() {
        @Override
        public void run() {
            long timeElapsed = System.currentTimeMillis() - mCountdownStart;
            float timeLeft = (Math.max(0, mCountdownDuration - timeElapsed) / 1000.0f);
            int timeSecLeft = Math.round(timeLeft + 0.4f);
            mProgressTextView.setText(mProgressText.replace("{0}", String.valueOf(timeSecLeft)));
            if (timeLeft > 0) {
                mCountdownHandler.removeCallbacks(mUpdateCountdown);
                mCountdownHandler.postDelayed(mUpdateCountdown, 250);
            }
        }
    };

    public void setRecordEnded() {
        mRecordButton.stopRecording();
        mProgressTextView.setVisibility(INVISIBLE);
        mLowerTextView.setVisibility(INVISIBLE);
        mHintTextView.setVisibility(INVISIBLE);
    }

    public void setUpperText(String value) {
        mUpperText = value;
        if (mUpperTextView != null) {
            mUpperTextView.setText(value);
        }
    }

    public void setLowerText(String value) {
        mLowerText = value;
        if (mLowerTextView != null) {
            mLowerTextView.setText(value);
        }
    }

    public void setRecordProgressText(String value) {
        mProgressText = value;
        if (mProgressTextView != null) {
            mProgressTextView.setText(value);
        }
    }

    public void setRecordHintText(String value) {
        mRecordHintText = value;
        if (mHintTextView != null) {
            mHintTextView.setText(value);
        }
    }

    public void setCameraPosition(int left, int top, int right, int bottom) {
        mCameraPosition.left = left;
        mCameraPosition.right = right;
        mCameraPosition.top = top;
        mCameraPosition.bottom = bottom;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int buttonSize = dpToPx(72);
        mRecordButton.measure(makeMeasureSpec(buttonSize, EXACTLY), makeMeasureSpec(buttonSize, EXACTLY));

        int minWidth = mRecordButton.getMeasuredWidth() * 2;
        int minHeight = mRecordButton.getMeasuredHeight() * 2;

        int measuredWidth = resolveSize(minWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(minHeight, heightMeasureSpec);

        int maxTextWidth = (int) (getMeasuredWidth() * 0.75);
        mUpperTextView.measure(makeMeasureSpec(maxTextWidth, AT_MOST), makeMeasureSpec(buttonSize, UNSPECIFIED));
        mLowerTextView.measure(makeMeasureSpec(maxTextWidth, AT_MOST), makeMeasureSpec(buttonSize, UNSPECIFIED));
        mHintTextView.measure(makeMeasureSpec(maxTextWidth, AT_MOST), makeMeasureSpec(buttonSize, UNSPECIFIED));
        mProgressTextView.measure(makeMeasureSpec(maxTextWidth, UNSPECIFIED), makeMeasureSpec(buttonSize, UNSPECIFIED));

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cx = getMeasuredWidth() / 2;

        if (mCameraPosition.width() == 0 || mCameraPosition.height() == 0) {
            return;
        }

        int spaceBelowPreview = getMeasuredHeight() - mCameraPosition.bottom;
        boolean recordBelowCamera = spaceBelowPreview > mRecordButton.getMeasuredHeight() * 1.3;

        if (recordBelowCamera) {
            int recordW = mRecordButton.getMeasuredWidth();
            int recordH = mRecordButton.getMeasuredHeight();

            int textSpacing = dpToPx(20);
            int topH = mUpperTextView.getMeasuredHeight() + textSpacing * 2;
            int bottomH = Math.max(mLowerTextView.getMeasuredHeight(), mHintTextView.getMeasuredHeight()) + textSpacing * 2;
            int textH = Math.max(topH, bottomH);

            int topTextCY = textH / 2;
            int topTextW = mUpperTextView.getMeasuredWidth();
            int topTextH = mUpperTextView.getMeasuredHeight();

            mUpperTextView.layout(cx - topTextW / 2, topTextCY - topTextH / 2, cx + topTextW / 2, topTextCY + topTextH / 2);

            int recordLeft = cx - recordW / 2;
            int recMidY = mCameraPosition.bottom + spaceBelowPreview / 2;
            mRecordButton.layout(recordLeft, recMidY - recordH / 2, cx + recordW / 2, recMidY + recordH / 2);

            int bottomTextCY = mCameraPosition.bottom - textH / 2;
            int lowerTextW = mLowerTextView.getMeasuredWidth();
            int lowerTextH = mLowerTextView.getMeasuredHeight();
            mLowerTextView.layout(cx - lowerTextW / 2, bottomTextCY - lowerTextH / 2,
                    cx + lowerTextW / 2, bottomTextCY + lowerTextH / 2);

            int hintTextW = mHintTextView.getMeasuredWidth();
            int hintTextH = mHintTextView.getMeasuredHeight();
            mHintTextView.layout(cx - hintTextW / 2, bottomTextCY - hintTextH / 2,
                    cx + hintTextW / 2, bottomTextCY + hintTextH / 2);

            int progressW = mProgressTextView.getMeasuredWidth();
            int progressH = mProgressTextView.getMeasuredHeight();
            int progressCX = recordLeft / 2;
            mProgressTextView.layout(progressCX - progressW / 2, recMidY - progressH / 2,
                    progressCX + progressW / 2, recMidY + progressH / 2);

            int finderH = mCameraPosition.height() - textH * 2;
            mOnFaceFinderResizeListener.onFaceFinderResize((int) (finderH * 0.7), finderH);
        }
        else {
            int recordW = mRecordButton.getMeasuredWidth();
            int recordH = mRecordButton.getMeasuredHeight();

            int lowerTextH = mLowerTextView.getMeasuredHeight();
            int hintTextH = mHintTextView.getMeasuredHeight();
            int bottomTextH = Math.max(lowerTextH, hintTextH);

            int bottomSpacing = dpToPx(20);
            int bottomControlH = bottomSpacing * 3 + recordH + bottomTextH;

            int recordTop = getMeasuredHeight() - bottomSpacing - recordH;
            int recordLeft = cx - recordW / 2;
            mRecordButton.layout(recordLeft, recordTop, recordLeft + recordW, recordTop + recordH);

            int bottomTextCY = recordTop - bottomSpacing - bottomTextH / 2;

            int lowerTextW = mLowerTextView.getMeasuredWidth();
            mLowerTextView.layout(cx - lowerTextW / 2, bottomTextCY - lowerTextH / 2, cx + lowerTextW / 2, bottomTextCY + lowerTextH / 2);

            int hintTextW = mHintTextView.getMeasuredWidth();
            mHintTextView.layout(cx - hintTextW / 2, bottomTextCY - hintTextH / 2, cx + hintTextW / 2, bottomTextCY + hintTextH / 2);

            int progressCX = recordLeft / 2;
            int progressCY = recordTop + recordH / 2;
            int progressW = mProgressTextView.getMeasuredWidth();
            int progressH = mProgressTextView.getMeasuredHeight();
            mProgressTextView.layout(progressCX - progressW / 2, progressCY - progressH / 2, progressCX + progressW / 2, progressCY + progressH / 2);

            int upperW = mUpperTextView.getMeasuredWidth();
            int upperH = mUpperTextView.getMeasuredHeight();

            boolean alignToBottom = bottomControlH < getMeasuredHeight() * 0.25;

            if (alignToBottom) {
                int upperTextTop = (bottomControlH - upperH) / 2;
                mUpperTextView.layout(cx - upperW / 2, upperTextTop,cx + upperW / 2, upperTextTop + upperH);

                int finderW = (int) (getMeasuredWidth() * 0.7);
                int finderH = getMeasuredHeight() - bottomControlH * 2;
                mOnFaceFinderResizeListener.onFaceFinderResize(finderW, finderH);
            }
            else {
                int finderW = (int) (getMeasuredWidth() * 0.7);
                int topSpacing = dpToPx(24);

                int finderH = getMeasuredHeight() - (upperH + topSpacing * 2) * 2;
                if (finderH > finderW * 1.6) {
                    finderH = (int) (finderW * 1.6);
                    topSpacing = ((getMeasuredHeight() - finderH) / 2 - upperH) / 2;
                }

                mUpperTextView.layout(cx - upperW / 2, topSpacing,cx + upperW / 2, topSpacing + upperH);
                mOnFaceFinderResizeListener.onFaceFinderResize(finderW, finderH);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public interface OnRecordClickListener {
        void onRecordClick(RecordButton view);
    }

    public interface OnFaceFinderResizeListener {
        void onFaceFinderResize(int w, int h);
    }
}

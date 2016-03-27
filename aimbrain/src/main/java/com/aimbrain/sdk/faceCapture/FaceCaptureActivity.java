package com.aimbrain.sdk.faceCapture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.server.FaceActions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * This activty allows to capture face images.
 * The result can obtained in  <code>onActivityResult</code> method from  <code>FaceCaptureActivity.images</code> static field.
 *
 */
public class FaceCaptureActivity extends Activity {
    /**
     * specify text in upper hint view on camera overlay
     */
    public static final String EXTRA_UPPER_TEXT = "upperText";
    /**
     * specify text in lower hint view on camera overlay
     */
    public static final String EXTRA_LOWER_TEXT = "lowerText";
    /**
     * specify number of images taken. Default is 3
     */
    public static final String EXTRA_BATCH_SIZE = "batchSize";
    /**
     * specify delay in ms after each photo. Default is 300
     */
    public static final String EXTRA_DELAY = "delay";

    /**
     * Array of taken images. Access this field in <code>onActivityResult</code>
     */
    public static ArrayList<Bitmap> images;
    private SurfaceView preview;
    private SurfaceHolder previewHolder;
    private SurfaceHolder overlaySurfaceHolder;
    private Camera camera;
    private boolean inPreview;
    private Bitmap bmp;
    private static Bitmap mutableBitmap;
    private LayoutInflater controlInflater;
    private DisplayMetrics windowSize = new DisplayMetrics();
    private OverlaySurfaceView overlaySurface;
    private ExecutorService adjustPhotosExecutor;
    private int photoTakenAmount = 0;
    private TextView lowerTextView;
    private TextView upperTextView;

    private int batchSize;
    private int delay;
    private RelativeLayout lowerTextRelativeLayout;
    private RelativeLayout upperTextRelativeLayout;
    private ImageButton photoButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenParameters();
        setContentView(R.layout.activity_face_capture);
        setupCameraPreview();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(windowSize);
        String upperText = getIntent().getStringExtra(EXTRA_UPPER_TEXT);
        String lowerText = getIntent().getStringExtra(EXTRA_LOWER_TEXT);
        batchSize = getIntent().getIntExtra(EXTRA_BATCH_SIZE, 3);
        delay = getIntent().getIntExtra(EXTRA_DELAY, 300);

        setupOverlay(upperText, lowerText);
        photoButton = (ImageButton) findViewById(R.id.photoButton);
    }

    private void setupOverlay(String upperText, String lowerText) {
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.camera_overlay, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        overlaySurface = (OverlaySurfaceView) findViewById(R.id.overlaySurfaceView);
        overlaySurfaceHolder = overlaySurface.getHolder();

        upperTextRelativeLayout = (RelativeLayout) findViewById(R.id.upperTextRelativeLayout);
        upperTextView = (TextView) findViewById(R.id.upperTextView);
        if (upperText != null) {
            upperTextView.setText(upperText);
            upperTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        lowerTextRelativeLayout = (RelativeLayout) findViewById(R.id.lowerTextRelativeLayout);
        lowerTextView = (TextView) findViewById(R.id.lowerTextView);
        if (lowerText != null) {
            lowerTextView.setText(lowerText);
            lowerTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.photoProgressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void setupCameraPreview() {
        preview = (SurfaceView) findViewById(R.id.faceCaptureSurface);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
    }

    private void setScreenParameters() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            camera = Camera.open(getFrontCameraIndex());
            setupSurfaceViewsSize();
        } catch (RuntimeException e) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Camera unavailable.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            FaceCaptureActivity.this.finish();
                        }
                    })
                    .show();
        }


    }

    private void setupSurfaceViewsSize() {
        if (camera != null) {
            Camera.Size cameraSize = getBestPreviewSize(windowSize.widthPixels, windowSize.heightPixels, camera.getParameters());
            float sizeRatio = (float) cameraSize.width / (float) cameraSize.height;
            previewHolder.setFixedSize(windowSize.widthPixels, (int) (windowSize.widthPixels * sizeRatio));
            overlaySurfaceHolder.setFixedSize(windowSize.widthPixels, (int) (windowSize.widthPixels * sizeRatio));
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {
            if (inPreview) {
                camera.stopPreview();
            }
            camera.release();
            camera = null;
        }
        inPreview = false;
        super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    public void refreshOverlayElements() {
        RelativeLayout.LayoutParams lowerTextLayoutParams = (RelativeLayout.LayoutParams) lowerTextRelativeLayout.getLayoutParams();
        lowerTextLayoutParams.height = getLowerTextHeight();
        lowerTextLayoutParams.setMargins(0, 0, 0, getLowerTextBottomMargin());
        RelativeLayout.LayoutParams upperTextLayoutParams = (RelativeLayout.LayoutParams) upperTextView.getLayoutParams();
        upperTextLayoutParams.height = (int)overlaySurface.getMaskBounds().top;

        RelativeLayout.LayoutParams photoButtonLayoutParams = (RelativeLayout.LayoutParams) photoButton.getLayoutParams();
        photoButtonLayoutParams.setMargins(0, 0, 0, getPhotoButtonBottomMargin());
    }

    private int getLowerTextHeight() {
        return windowSize.heightPixels - (int)overlaySurface.getMaskBounds().top - (int) overlaySurface.getMaskBounds().height() - getLowerTextBottomMargin();
    }

    private int getLowerTextBottomMargin() {
        int margin_size =  10 + windowSize.heightPixels - overlaySurfaceHolder.getSurfaceFrame().height();
        int photoButtonSpinnerTop = getPhotoButtonBottomMargin() + photoButton.getHeight() + 12;
        return margin_size > photoButtonSpinnerTop ? margin_size : photoButtonSpinnerTop;
    }

    private int getPhotoButtonBottomMargin() {
        return (int)(25.0f * (float)windowSize.heightPixels/1280);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (camera != null) {
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(previewHolder);
                }
            } catch (Throwable t) {
                Log.e("surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(FaceCaptureActivity.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height,
                        parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    camera.startPreview();
                    inPreview = true;
                }
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    public Bitmap onPictureTake(byte[] data) {
        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        return PictureManager.getCroppedAndRotatedPhoto(mutableBitmap, overlaySurface.getMaskBounds());
    }

    private Integer getFrontCameraIndex() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return 0;
    }

    public void photoButtonPressed(View view) {
        ImageButton photoButton = (ImageButton) view;
        photoButton.setEnabled(false);
        photoButton.setClickable(false);
        takePhotos(this.batchSize, this.delay);
    }

    private void takePhotos(final int photosAmount, final int photoInterval) {
        Thread thread = new Thread() {
            ArrayList<Bitmap> photos = new ArrayList<>();

            @Override
            public void run() {
                super.run();
                FaceCaptureActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressBar progressBar = (ProgressBar) FaceCaptureActivity.this.findViewById(R.id.photoProgressBar);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                terminateExecutor(photoInterval, photosAmount);
                adjustPhotosExecutor = Executors.newFixedThreadPool(photosAmount);
                final List<FutureTask<Bitmap>> pictureTasks = new ArrayList<>();
                try {
                    photoTakenAmount = 0;
                    takePicture(pictureTasks);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Take photos", e.getMessage());
                }
            }

            private void takePicture(final List<FutureTask<Bitmap>> pictureTasks) throws InterruptedException {
                if(photoTakenAmount < photosAmount) {
                    try {
                    if (camera == null) {
                        Camera.open(getFrontCameraIndex());
                    }
                    camera.startPreview();
                    camera.takePicture(shutterCallback, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] data, final Camera camera) {
                            try {
                                sleep(photoInterval);
                                photoTakenAmount++;
                                takePicture(pictureTasks);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            FutureTask getPicture = new FutureTask<Bitmap>(new Callable<Bitmap>() {
                                @Override
                                public Bitmap call() throws Exception {
                                    return onPictureTake(data);
                                }
                            });
                            pictureTasks.add(getPicture);
                            adjustPhotosExecutor.execute(getPicture);
                            if (photoTakenAmount == photosAmount) {
                                collectAndFinishWithPhotos(pictureTasks);
                            }
                        }
                        });
                    } catch (RuntimeException e) {
                        if (camera != null) {
                            camera.release();
                            camera = null;
                        }
                        new AlertDialog.Builder(FaceCaptureActivity.this)
                                .setTitle("Error")
                                .setMessage("Camera unavailable.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        FaceCaptureActivity.this.finish();
                                    }
                                })
                                .show();
                    }
                }

            }

            private void collectAndFinishWithPhotos(List<FutureTask<Bitmap>> pictureTasks) {

                adjustPhotosExecutor.shutdown();
                for(FutureTask<Bitmap> pictureTask : pictureTasks) {

                    try {
                        photos.add(pictureTask.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
                FaceCaptureActivity.images = photos;
                setResult(RESULT_OK);
                finish();
            }
        };
        thread.start();
    }

    private void terminateExecutor(int photoInterval, int photosAmount) {
        if (adjustPhotosExecutor != null && !adjustPhotosExecutor.isTerminated()) {
            adjustPhotosExecutor.shutdown();
            try {
                adjustPhotosExecutor.awaitTermination(photoInterval * photosAmount, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!adjustPhotosExecutor.isTerminated()) {
                adjustPhotosExecutor.shutdownNow();
            }
        }
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
            int shutterSound = soundPool.load(FaceCaptureActivity.this, R.raw.camera_shutter_click, 0);
            soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);
        }
    };

}

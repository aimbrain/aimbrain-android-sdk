package com.aimbrain.sdk.faceCapture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.util.Logger;

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
 * The result can obtained in  <code>onActivityResult</code> method from  <code>PhotoFaceCaptureActivity.images</code> static field.
 */
public class PhotoFaceCaptureActivity extends FaceCaptureActivity {
    private static final String TAG = PhotoFaceCaptureActivity.class.getSimpleName();

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
    private Bitmap bmp;
    private static Bitmap mutableBitmap;
    private ExecutorService adjustPhotosExecutor;
    private int photoTakenAmount = 0;

    private int batchSize;
    private int delay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        batchSize = getIntent().getIntExtra(EXTRA_BATCH_SIZE, 3);
        delay = getIntent().getIntExtra(EXTRA_DELAY, 300);
    }

    public Bitmap onPictureTake(byte[] data) {
        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        return PictureManager.getCroppedAndRotatedPhoto(mutableBitmap);
    }

    @Override
    protected void captureData() {
        takePhotos(this.batchSize, this.delay);
    }

    private void takePhotos(final int photosAmount, final int photoInterval) {
        Thread thread = new Thread() {
            ArrayList<Bitmap> photos = new ArrayList<>();

            @Override
            public void run() {
                super.run();

                terminateExecutor(photoInterval, photosAmount);
                adjustPhotosExecutor = Executors.newFixedThreadPool(photosAmount);
                final List<FutureTask<Bitmap>> pictureTasks = new ArrayList<>();
                try {
                    photoTakenAmount = 0;
                    takePicture(pictureTasks);
                } catch (Exception e) {
                    Logger.e(TAG, "take photos", e);
                }
            }

            private void takePicture(final List<FutureTask<Bitmap>> pictureTasks) throws InterruptedException {
                if (photoTakenAmount < photosAmount) {
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
                                    Logger.w(TAG, "pic delay", e);
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
                        new AlertDialog.Builder(PhotoFaceCaptureActivity.this)
                                .setTitle("Error")
                                .setMessage("Camera unavailable.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        PhotoFaceCaptureActivity.this.finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                }

            }

            private void collectAndFinishWithPhotos(List<FutureTask<Bitmap>> pictureTasks) {
                adjustPhotosExecutor.shutdown();
                for (FutureTask<Bitmap> pictureTask : pictureTasks) {
                    try {
                        photos.add(pictureTask.get());
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.w(TAG, "collect photos", e);
                    }

                }
                PhotoFaceCaptureActivity.images = photos;
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
                Logger.w(TAG, "await termiantion", e);
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
            int shutterSound = soundPool.load(PhotoFaceCaptureActivity.this, R.raw.camera_shutter_click, 0);
            soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmp = null;
        mutableBitmap = null;
    }
}

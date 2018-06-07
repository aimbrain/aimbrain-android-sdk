package com.aimbrain.sdk.faceCapture;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Base64;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.faceCapture.views.FaceFinderUtil;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.server.FaceActions;
import com.aimbrain.sdk.util.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PictureManager {
    public static final String TAG = PictureManager.class.getSimpleName();

    public final static int MAX_PHOTO_HEIGHT = 300;
    public final static int COMPRESSION_RATIO = 100;

    public static Bitmap cropBitmap(Bitmap photo) {

        int photo_w = photo.getWidth();
        int photo_h = photo.getHeight();
        int box_w = (int) (photo_w * FaceFinderUtil.BOX_WIDTH);
        int box_h = (int) (FaceFinderUtil.BOX_RATIO * box_w);
        int p_left = (int) ((photo_w - box_w) / 2 - box_w * 0.1);
        int p_width = (int) (box_w * 1.2);
        int p_top = (int) ((photo_h - box_h) / 2 - box_h * 0.1);
        int p_height = (int) (box_h * 1.2);

        return Bitmap.createBitmap(photo, p_left, p_top, p_width, p_height);
    }

    public static Bitmap adjustSize(Bitmap photo, int maxHeight) {
        if(photo.getHeight() > maxHeight){
            float ratio = (float)photo.getWidth() / (float)photo.getHeight();
            return Bitmap.createScaledBitmap(photo, (int) (maxHeight * ratio), maxHeight, false);
        }
        return photo;
    }

    public static Bitmap rotatePhoto(Bitmap photo, int degrees){
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(photo , 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
    }

    public static StringListDataModel adjustProvidedPhotos(List<Bitmap> photos) {
        List<String> adjustedPhotos = new ArrayList<>();
        for (Bitmap photo : photos) {
            photo = PictureManager.adjustSize(photo, 300);
            ByteArrayOutputStream compressedPhotoStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, PictureManager.COMPRESSION_RATIO, compressedPhotoStream);
            adjustedPhotos.add(Base64.encodeToString(compressedPhotoStream.toByteArray(), Base64.NO_WRAP));
        }
        StringListDataModel adjustedData = new StringListDataModel();
        adjustedData.setData(adjustedPhotos);
        return adjustedData;
    }

    public static String getEncodedCompressedPhoto(Bitmap bmp){
        ByteArrayOutputStream compressedPhotoStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, PictureManager.COMPRESSION_RATIO, compressedPhotoStream);
        String encodedPhoto =  Base64.encodeToString(compressedPhotoStream.toByteArray(), Base64.NO_WRAP);
        return encodedPhoto;
    }

    public static Bitmap getCroppedAndRotatedPhoto(Bitmap bmp) {
        Bitmap croppedPhoto = null;
        try {
            Bitmap adjustedPhoto = PictureManager.rotatePhoto(bmp, -90);
            adjustedPhoto = PictureManager.cropBitmap(adjustedPhoto);
            adjustedPhoto = PictureManager.adjustSize(adjustedPhoto, PictureManager.MAX_PHOTO_HEIGHT);
            croppedPhoto = adjustedPhoto;
        } catch (Exception e) {
            Logger.w(TAG, "crop", e);
        }
        return croppedPhoto;
    }
}

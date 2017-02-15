package com.aimbrain.sdk.faceCapture.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import com.aimbrain.sdk.util.Logger;

import java.util.Arrays;

public class CameraChoiceStrategy {
    private static final String TAG = CameraChoiceStrategy.class.getSimpleName();

    public enum CameraChoice {
        CAMERA_LEGACY,
        CAMERA2
    }

    public static CameraChoice chooseCamera(Context context) {
        int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= Build.VERSION_CODES.LOLLIPOP) {
            return canUseCamera2(context) ? CameraChoice.CAMERA2 : CameraChoice.CAMERA_LEGACY;
        } else {
            return CameraChoice.CAMERA_LEGACY;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean canUseCamera2(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getPreferredCameraId(manager);
            CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
            Integer level = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            Logger.d(TAG, "supported camera level " + level);
            return level != null && level != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;

        } catch (CameraAccessException e) {
            Logger.d(TAG, "Error picking camera api", e);
            return false;
        }
    }

    /**
     * @return Camera id to use, try to use front camera if possible.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getPreferredCameraId(CameraManager manager) throws CameraAccessException {
        String[] availableIds = manager.getCameraIdList();
        Logger.d(TAG, "Available cameras: " + Arrays.toString(availableIds));
        for (String id : availableIds) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer lensFlag = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (lensFlag != null && lensFlag == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return availableIds[0];
    }
}

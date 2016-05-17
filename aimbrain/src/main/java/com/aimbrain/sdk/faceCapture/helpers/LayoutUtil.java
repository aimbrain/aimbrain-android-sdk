package com.aimbrain.sdk.faceCapture.helpers;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


public class LayoutUtil {

    /* Get real screen size to use for all resolution choices.
       Important in devices with software button bar, because
       in this cause height is smaller and this causes to wrong
       preview size picking. */
    public static VideoSize getScreenSize(Activity activity) {
        WindowManager w = activity.getWindowManager();
        Display display = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        /* safe default, since SDK_INT = 1 */
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        /* includes window decorations (statusbar bar/menu bar) */
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                width = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                height = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {
            }
        }

        /* includes window decorations (statusbar bar/menu bar) */
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                width = realSize.x;
                height = realSize.y;
            } catch (Exception ignored) {
            }
        }

        return new VideoSize(width, height);
    }
}

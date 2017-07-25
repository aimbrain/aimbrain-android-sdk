package com.aimbrain.sdk.privacy;

import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewParent;

import com.aimbrain.sdk.util.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;


public class SensitiveViewGuard {

    private static final int SALT_BYTES = 128;
    public static final String TAG = SensitiveViewGuard.class.getSimpleName();

    private Set<WeakReference<View>> sensitiveViews;
    private byte[] salt;

    private static SensitiveViewGuard instance;

    @VisibleForTesting
    protected SensitiveViewGuard() {
        sensitiveViews = new HashSet<>();
        salt = generateRandomSalt();
    }

    public static SensitiveViewGuard getInstance() {
        if (instance == null)
            instance = new SensitiveViewGuard();
        return instance;
    }

    public static void addView(View view) {
        if (view != null) {
            getInstance().sensitiveViews.add(new WeakReference<>(view));
        }
    }


    public static boolean isViewSensitive(View view) {
        if (view == null) {
            return false;
        }

        for(WeakReference<View> reference : getInstance().sensitiveViews)
        {
            if (reference.get() == view)
                return true;
        }

        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof View)
            return isViewSensitive((View) parent);

        return false;
    }

    public static void setSalt(byte[] salt) {
        if (salt.length == SALT_BYTES)
            getInstance().salt = salt;
        else
            throw new InvalidParameterException("Provided salt must be of length " + SALT_BYTES);
    }

    public static byte[] generateRandomSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        sr.nextBytes(salt);
        return salt;
    }

    public static byte[] getSaltBytes() {
        return getInstance().salt;
    }

    public static String getSalt() {
        return bytesToString(getInstance().salt);
    }

    public static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return bytesToString(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Logger.w(TAG, "hash digest", e);
        }
        return null;
    }

    public static String bytesToString(byte[] input) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < input.length; i++) {
            String hex = Integer.toHexString(0xff & input[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @VisibleForTesting
    protected void setInstance() {
        instance = this;
    }
}

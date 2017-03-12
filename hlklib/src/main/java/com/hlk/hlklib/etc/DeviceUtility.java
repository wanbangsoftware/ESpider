package com.hlk.hlklib.etc;

import android.os.Build;

/**
 * <b>功能：</b>Utility methods related to physical devies and emulators.<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/21 08:27 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class DeviceUtility {

    /**
     * 判断当前设备是模拟器或真机
     */
    public static boolean isEmulator() {
        return null == Build.PRODUCT
                || Build.PRODUCT.contains("vbox")
                || (Build.FINGERPRINT.startsWith("generic") && Build.BOARD.startsWith("unknown"))
                || (Build.FINGERPRINT.startsWith("unknown") && Build.BOARD.startsWith("unknown"))
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.MANUFACTURER.contains("Android")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}

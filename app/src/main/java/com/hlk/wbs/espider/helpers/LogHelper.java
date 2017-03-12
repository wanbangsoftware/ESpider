package com.hlk.wbs.espider.helpers;

import android.util.Log;

import com.hlk.wbs.espider.BuildConfig;


/**
 * <b>功能：</b>打印log记录<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/14 12:47 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class LogHelper {

    private static void logcat(String tag, String string) {
        if (BuildConfig.LOGABLE) {
            Log.e(tag, StringHelper.format("%s", string));
        }
    }

    public static void log(String tag, String string) {
        log(tag, string, false);
    }

    public static void log(String tag, String string, Throwable e) {
        log(tag, string);
    }

    public static void log(String tag, String string, boolean replaceLineTag) {
        logcat(tag, string);
//        if (replaceLineTag) {
//            CrashHandler.getInstance().log(tag, string.replaceAll("\n", ""));
//        } else {
//            CrashHandler.getInstance().log(tag, string);
//        }
    }
}

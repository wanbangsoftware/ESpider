package com.hlk.wbs.espider.helpers;

import android.text.TextUtils;

import com.hlk.wbs.espider.applications.App;

import java.util.Locale;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/14 09:39 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class StringHelper {

    public static String getString(int resId) {
        return App.getInstance().getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return App.getInstance().getString(resId, formatArgs);
    }

    public static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals("null");
    }

    public static String format(String fmt, Object... args) {
        return String.format(Locale.getDefault(), fmt, args);
    }

    public static int getInteger(int resId) {
        return App.getInstance().getResources().getInteger(resId);
    }
}

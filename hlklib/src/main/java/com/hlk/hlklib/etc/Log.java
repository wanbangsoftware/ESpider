package com.hlk.hlklib.etc;

import java.util.Locale;

/**
 * 作者：Hsiang Leekwok on 2015/11/09 15:27<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class Log {

    public static void log(String string) {
        log("Log", string);
    }

    public static void log(String tag, String string) {
        android.util.Log.e(tag, string);
    }

    public static void log(String format, Object... args) {
        log(format(format, args));
    }

    public static String format(String format, Object... args) {
        return String.format(Locale.getDefault(), format, args);
    }
}

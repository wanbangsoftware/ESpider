package com.hlk.wbs.espider.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.hlk.wbs.espider.applications.App;

/**
 * 简单的配置数据
 * 作者：Hsiang Leekwok on 2015/08/26 10:56<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class PreferenceHelper {

    private static final String SHARED_NAME = "setting";

    /**
     * 保存简单的数据到Preference
     */
    public static void save(int key, String value) {
        save(App.getInstance().getString(key), value);
    }

    /**
     * 保存简单的数据到Preference
     */
    public static void save(String key, String value) {
        SharedPreferences.Editor editor = App.getInstance().getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE).edit();
        try {
            editor.putString(key, value);
        } finally {
            editor.apply();
        }
    }

    /**
     * 从Preference中获取字符串值
     */
    public static String get(int key) {
        return get(App.getInstance().getString(key));
    }

    /**
     * 从Preference中获取字符串值
     */
    public static String get(int key, String defaultValue) {
        return get(App.getInstance().getString(key), defaultValue);
    }

    /**
     * 从Preference中获取字符串值
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * 从Preference中获取字符串值
     */
    public static String get(String key, String defaultValue) {
        return App.getInstance().getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE).getString(key, defaultValue);
    }
}

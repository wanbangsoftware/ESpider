package com.hlk.wbs.espider.etc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.hlk.wbs.espider.applications.App;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * 一些常用的方法集合
 * <p/>
 * Created by Hsiang Leekwok on 2015/07/13.
 */
public class Utils {

    public static final String SUFFIX_PNG = "png";
    public static final String SUFFIX_JPG = "jpg";
    public static final String SUFFIX_JPEG = "jpeg";
    /**
     * 缩略图的最大尺寸
     */
    public static final int MAX_THUMBNAIL_SIZE = 300;

    /**
     * 头像按照屏幕宽度缩放比例
     */
    public static final float HEADER_ZOOM_MULTIPLES = 6.0f;
    public static final float CHATING_HEADER_ZOOM_SIZE = 7.5f;

    public static final String FMT_HHMM = "yyyy/MM/dd HH:mm";
    public static final String FMT_MDHM = "MM月dd日 HH:mm";
    public static final String FMT_YMD = "yyyy/MM/dd";
    public static final String FMT_HHMMSS = "yyyy/MM/dd HH:mm:ss";
    public static final String FMT_YYYYMMDDHHMM = "yyyyMMddHHmm";

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_HHMMSS,
                Locale.getDefault());
        return sdf.format(date);
    }

    public static String format(String fmt, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.getDefault());
        return sdf.format(date);
    }

    public static String format(long time) {
        long h = time / HOUR;
        long m = time % HOUR / MINUTE;
        long s = time % MINUTE / SECOND;
        long ms = time % SECOND;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", h, m, s, ms);
    }

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    /**
     * 一天的毫秒数
     */
    private static final long DAY = HOUR * 24;

    // private static final long MONTH = DAY * 30;

    /**
     * 获取指定时间与当前时间相比较的时间 <br>
     * 如果是在同一天内则显示时间<br>
     * 前一天显示昨天，再前一天显示前天，其余显示日期
     */
    @SuppressWarnings("deprecation")
    public static String formatDateBetweenNow(Date date) {
        Date d = new Date();
        long now = d.getTime();
        Date today = new Date(d.getYear(), d.getMonth(), d.getDay());
        long today0 = today.getTime();
        long then = date.getTime();
        if (then > (now + MINUTE * 2))
            return "->\u795e\u5947\u7684\u672a\u6765";
        if (then > today0)
            return format("HH:mm", date);
        if (then > today0 - DAY)
            return "\u6628\u5929";// yesterday
        if (then > today0 - DAY * 2)
            return "\u524d\u5929";// the day before yesterday
        return format("yyyy-MM-dd", date);
    }

    /**
     * 查看指定的包是否已安装
     */
    public static PackageInfo isInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static void hidingInputBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hidingInputBoard(View view) {
        hidingInputBoard(App.getInstance(), view);
    }

    /**
     * 关闭数据库连接
     */
    public static void clearDBHelpers() {
//        ChatDBHelper.getInstance().close();
//        DeptDBHelper.getInstance().close();
//        UserDBHelper.getInstance().close();
//        SettingDBHelper.getInstance().close();
    }

    /** 本地内置卡内缓存路径 */
    // private static final String SDCARD_PATH = "STOA";
    /**
     * 本地数据库缓存目录
     */
    public static final String DB_DIR = "database";
    /**
     * 本地相机拍照之后照片缓存目录
     */
    public static final String CAMERA_DIR = "camera";
    /**
     * 本地图片缓存目录
     */
    public static final String IMAGE_DIR = "images";
    /**
     * 本地缩略图缓存目录
     */
    public static final String THUMB_DIR = "thumbnails";
    /**
     * 本地语音缓存目录
     */
    public static final String VOICE_DIR = "voices";
    /**
     * 本地其他文件缓存目录
     */
    public static final String OTHER_DIR = "others";
    /**
     * 一般缓存目录
     */
    public static final String CACHE_DIR = "cache";

    /**
     * 返回外置卡根目录，末尾不包含/
     */
    public static String getExternalStoragePath() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    /**
     * 获取内置SD卡中app私有存储空间
     */
    public static String getInternalCacheDir() {
        return App.getInstance().getCacheDir().toString();
    }

    /**
     * 在外置缓存中获取指定的目录路径，末尾包含/ <br />
     * <i>如果没有外置卡访问权限则返回app自有空间的指定目录</i>
     */
    public static String getCachePath(String dir) {
        StringBuilder sb = new StringBuilder();
        if (Permission.hasPermission(App.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String sdcard = getExternalStoragePath();
            if (!isEmpty(sdcard)) {
                sb.append(sdcard).append("/Android/.").append(App.staticName()).append("/");
            } else {
                sb.append(getInternalCacheDir()).append("/");
            }
        } else {
            sb.append(getInternalCacheDir()).append("/");
        }
        sb.append(dir).append("/");
        createDirs(sb.toString());
        return sb.toString();
    }

    /**
     * 创建指定的文件目录
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createDirs(String dirs) {
        File file = new File(dirs);
        // 查看文件目录是否存在，不存在则创建
        if (!file.exists())
            file.mkdirs();
    }

    /**
     * 通过UUID返回随机字符串
     */
    public static String getRandomStringByUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取本地Image缓存文件夹路径，末尾包含/
     */
    public static String getLocalImageDir() {
        return getCachePath(IMAGE_DIR);
    }

    /**
     * 获取本地照相之后照片缓存目录
     */
    public static String getLocalCameraDir() {
        return getCachePath(CAMERA_DIR);
    }

    /**
     * 通过URL获取本地缓存的文件路径
     *
     * @param url 文件的网络URL
     * @param dir 文件本地缓存的目录
     */
    public static String getLocalFilePath(String url, String dir) {
        String suffix = url.substring(url.lastIndexOf('.'));
        return getCachePath(dir) + com.hlk.hlklib.etc.Cryptography.md5(url) + suffix;
    }

    /**
     * 通过string获取相应资源的id<br />
     * getResId("icon", context, Drawable.class);
     */
    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 判断字符串是否为空<br />null、""、"null"为true
     */
    public static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals("null");
    }
}

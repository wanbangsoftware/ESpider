package com.hlk.hlklib.etc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些基本的方法<br />
 * Created by Hsiang Leekwok on 2015/07/14.
 */
public class Utility {

    /**
     * 转换 dp 为 px 值
     */
    public static int ConvertDp(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 转换 px 为 dp 值
     */
    public static int ConvertPx(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * 将字节数组转换成对应的hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 格式化文件字节单位
     *
     * @param size 文件长度
     * @return 返回格式化的文件长度
     */
    public static String formatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    private static final String numerical = "[0-9]";

    /**
     * 计算两个时间之间的差
     */
    public static long TimesBetween(String long1, String long2) {
        long l1 = null == long1 ? 0 : (Pattern.matches(numerical, long1) ? Long.parseLong(long1) : 0);
        long l2 = null == long2 ? 0 : (Pattern.matches(numerical, long2) ? Long.parseLong(long2) : 0);
        return TimesBetween(l1, l2);
    }

    /**
     * 计算两个时间之间的差
     */
    public static long TimesBetween(long long1, long long2) {
        return Math.abs(long1 - long2);
    }

    /**
     * Integer 数字转换成16进制代码
     *
     * @param number  需要转换的 int 数据
     * @param hexSize 转换后的 hex 字符串长度，不足长度时左边补 0
     */
    public static String intToHex(int number, int hexSize) {
        String tmp = Integer.toHexString(number & 0xFF);
        while (tmp.length() < hexSize) {
            tmp = "0" + tmp;
        }
        return tmp;
    }

    /**
     * 将 int 型的 Color 转换成 html 格式可读的颜色值，比如：#FFFFFF
     */
    public static String colorToHexString(int color) {
        return "#" + intToHex(Color.red(color), 2) + intToHex(Color.green(color), 2) + intToHex(Color.blue(color), 2);
    }

    /**
     * 在字符串中查找指定的子字符串并将其改成指定的颜色
     *
     * @param orgString     原始字符串
     * @param replaceString 需要替换的子字符串
     * @param color         替换后的子字符串的颜色
     * @return 返回带有 html 标签的字符串
     */
    public static String addColor(String orgString, String replaceString, int color) {
        return orgString.replace(replaceString, "<font color=\""
                + colorToHexString(color) + "\">" + replaceString + "</font>");
    }

    /**
     * 清除文字中带的字体颜色的 html 标签
     */
    public static String clearColor(String orgString, int color) {
        return orgString.replace(
                "<font color=\"" + colorToHexString(color) + "\">", "").replace("</font>", "");
    }


    /**
     * 手机号码验证的正则
     */
    private static final String PHONE_MARCHER = "^(13[0-9]|15[012356789]|17[0-9]|18[0-9]|14[57])[0-9]{8}$";

    /**
     * 正则检测字符串是否为手机号码
     */
    public static boolean isItMobilePhone(String phone) {
        Pattern p = Pattern.compile(PHONE_MARCHER);
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    private static final double KM = 1000.0;

    public static String formatDistance(double distance) {
        if (distance < KM) {
            return String.format(Locale.getDefault(), "%d米", (int) distance);
        }
        return String.format(Locale.getDefault(), "%.2f公里", (distance / 1000));
    }

    /**
     * 隐藏键盘
     */
    public static void hidingInputBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

package com.hlk.wbs.tx.custom;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>功能</b>：一些基本转换方法<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:09 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class CustomConvert {
    /**
     * 二进制数据
     */
    public static byte Binary = 0x02;
    /**
     * 八进制数据
     */
    public static byte Octal = 0x08;
    /**
     * 十进制数据
     */
    public static byte Decimal = 0x0A;
    /**
     * 十六进制数据
     */
    public static byte Hex = 0x10;

    /**
     * 将一个 byte 数组扩展成另外一个长度的 byte 数组。
     *
     * @param src       需要扩展的原始数组。
     * @param newLength 扩展后的新数组长度。
     * @return 返回扩展后的数组。如果新长度大于或等于
     * 原始数组长度则新数组中包含全部原始数据，否则数据将会被截断。
     */
    public static byte[] expandArray(byte[] src, int newLength) {
        byte[] ret;
        if (src == null) {
            ret = new byte[newLength];
        } else {
            if (src.length <= newLength) {
                ret = new byte[newLength];
                System.arraycopy(src, 0, ret, 0, src.length);
            } else {
                ret = new byte[newLength];
                System.arraycopy(src, 0, ret, 0, newLength);
            }
        }
        return ret;
    }

    /**
     * 基本的十六进制数据列表。
     */
    private final static String scaleString = "0123456789ABCDEF";
    /**
     * 基本的 HEX 数据
     */
    private final static byte[] hex = scaleString.getBytes();

    /**
     * 将 int 整数转换成其对应的字节数组表示方式。
     *
     * @param integer 需要转换的数据。
     * @return 返回 i 所表示的字节数组。
     */
    public static byte[] intToBytes(int integer) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xff & integer);
        bytes[1] = (byte) ((0xff00 & integer) >> 8);
        bytes[2] = (byte) ((0xff0000 & integer) >> 16);
        bytes[3] = (byte) ((0xff000000 & integer) >> 24);
        return bytes;
    }

    /**
     * 将字节数组转换成 int 整数。
     *
     * @param bytes 需要转换的字节数组。
     * @param start 从 bytes 的哪一个下标开始转换。
     * @return 返回从 iStart 开始转换的 int 整数。
     */
    public static int bytesToInt(byte[] bytes, int start) {
        int integer = bytes[start] & 0xFF;
        integer |= ((bytes[start + 1] << 8) & 0xFF00);
        integer |= ((bytes[start + 2] << 16) & 0xFF0000);
        integer |= ((bytes[start + 3] << 24) & 0xFF000000);
        return integer;
    }

    /**
     * 转换 short 数据为字节数组方式。
     *
     * @param data 需要转换的数据。
     * @return 返回  i 所表示的字节数组。
     */
    public static byte[] shortToBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (0xff & data);
        bytes[1] = (byte) ((0xff00 & data) >> 8);
        return bytes;
    }

    /**
     * 将 byte[] 转换成 short 数据
     *
     * @param bytes 需要转换的数组，byte[] 型数据。
     * @param start 从哪一个下标开始转换，该索引不能小于 0 或大于 bytes 的长度。
     * @return 返回被转换成的 short 值
     */
    public static short bytesToShort(byte[] bytes, int start) {
        short add = (short) (bytes[start] & 0xFF);
        add |= ((bytes[start + 1] << 8) & 0xFF00);
        return add;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;

        return (c - '0') & 0x0f;
    }

    /**
     * 将一个字节数据转换成其对应的 16 进制字符串数据。
     *
     * @param data 需要转换的数据。
     * @return 返回 b 的 16 进制字符串表示方式。
     */
    public static String byteToHexString(byte data) {
        return new String(new byte[]{hex[(data >> 4) & 0x0F], hex[data & 0x0F]});
    }

    /**
     * 将一个字节数组转换成对应的 16 进制字符串。
     *
     * @param bytes 需要转换的字节数组。
     * @return 返回转换后的 16 进制字符串。
     */
    public static String bytesToHexString(byte[] bytes) {
        if (null == bytes || bytes.length < 1) return null;

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(byteToHexString(b));
        }
        return sb.toString();
    }

    /**
     * 将一个 16 进制字符串表示的数据转换成对应的字节数组。
     *
     * @param hexString 需要转换的 16 进制字符串，其长度不是 2 的倍数时将会在其最左边添加“0”补齐。
     * @return 返回 hexString 所表示的字节数组。
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (isEmpty(hexString)) {
            return new byte[0];
        }

        // 不足位时左补 0 补齐，否则会出错。
        if (hexString.length() % 2 != 0)
            hexString = "0" + "" + hexString;

        byte[] b = new byte[hexString.length() / 2];
        int j = 0, len = b.length;
        for (int i = 0; i < len; i++) {
            char c0 = hexString.charAt(j++);
            char c1 = hexString.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    /**
     * 将指定的数据转换成其他类型的数据。
     *
     * @param integer 指定的数据，最大为 int 型。
     * @param scale   要转换成的进制，通常为 2，8，10，16.
     * @param length  指定转换后数据的长度，不足长度时左补零补齐。
     * @return 返回转换后的字符串数据。
     */
    public static String IntToDigital(int integer, byte scale, int length) {
        int i = integer, j;
        StringBuilder sb = new StringBuilder();
        while (i >= scale && scale > 1) {
            j = i % scale;
            i = i / scale;
            sb.append(scaleString.charAt(j));
        }
        sb.append(scaleString.charAt(i));
        // 如果长度不足 length 则左补零补齐
        if (length > 0) {
            j = sb.length();
            for (i = 0; i < (length - j); i++) {
                sb.append('0');
            }
        }
        return sb.reverse().toString();
    }

    /**
     * 计算指定数据的乘方
     *
     * @param base 要计算乘方的数据。
     * @param pow  要乘以的次数。
     * @return 返回 base 的 pow 次方值。
     */
    private static int Pow(int base, int pow) {
        int ret = base;
        if (pow == 0) {
            ret = 1;
        }
        for (int i = 1; i < pow; i++) {
            ret *= base;
        }
        return ret;
    }

    /**
     * 将字符型的数据按照指定进制转换成整数
     *
     * @param digital 指定数据内容。
     * @param scale   指定的 digital 的进制类型。
     * @return 返回整形数据。
     */
    public static int DigitalToInt(String digital, byte scale) {
        int ret = 0, len;
        String data = digital.toUpperCase();
        len = data.length();
        for (int i = 0; i < len; i++) {
            ret = ret + scaleString.indexOf(data.charAt(len - 1 - i)) * Pow(scale, i);
        }
        return ret;
    }

    /**
     * 计算给定字节数组的校验和。
     *
     * @param bytes 需要计算校验和的字节数组。
     * @return 返回和校验。
     */
    public static byte GetCheckSum(byte[] bytes) {
        byte sum = 0x00;
        if (null == bytes || bytes.length < 1) return sum;

        for (byte b : bytes) {
            sum += b;
        }
        return sum;
    }

    /**
     * 计算给定数组的异或校验和。
     *
     * @param bytes 需要计算异或校验和的数组。
     * @return 返回异或校验值。
     */
    public static byte GetXor(byte[] bytes) {
        byte xor = 0x00;
        if (null == bytes || bytes.length < 1) return xor;

        for (byte b : bytes) {
            xor ^= b;
        }
        return xor;
    }

    /**
     * 计算两个日期之间相差的秒数
     *
     * @param date1 日期
     * @param date2 日期
     * @return 返回两个日期之间相差的秒数
     */
    public static long SecondsBetween(Date date1, Date date2) {
        return SecondsBetween(date1.getTime(), date2.getTime());
    }

    public static long SecondsBetween(long date1, long date2) {
        return Math.abs(date1 - date2) / 1000;
    }

    /**
     * 将"DD.MMMM"格式的 GPS 定位信息转换为 GPRMC 标准格式"DDMM.MMMM"
     *
     * @param ddmmmm 度分格式的 GPS 信息
     * @return 返回标准的 GPRMC 格式的 ddmm.mmmm 格式
     */
    public static double DD2GPRMC(double ddmmmm) {
        int dd = (int) ddmmmm;
        return (dd * 100) + (ddmmmm - dd) * 60.0;
    }

    /**
     * 将"DDMM.MMMM"格式的 GPRMC 数据转换为"DD.MMMM"格式
     *
     * @param gprmc GPRMC 格式的定位信息数据(DDMM.MMMM)
     * @return 返回度分格式的定位信息数据(DD.MMMM)
     */
    public static double GPRMC2DD(double gprmc) {
        int dd = (int) gprmc;
        double mm = gprmc - dd;
        dd = dd / 100;
        return (dd + mm / 60.0);
    }

    /**
     * 使用指定的正则检查字符串
     */
    public static boolean verify(String string, String regex) {
        if (isEmpty(string)) return false;
        if (isEmpty(regex)) return false;

        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(string);
        return matcher.find();
    }

    /**
     * 将字符串进行base64编码
     */
    public static String base64(String string) {
        if (isEmpty(string)) return null;
        return base64(string.getBytes());
    }

    /**
     * 将字节数组进行base64编码
     */
    public static String base64(byte[] bytes) {
        if (null == bytes || bytes.length < 1) return null;

        try {
            return new String(Base64.encode(bytes, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字符串进行URL转义编码
     */
    public static String urlEncode(String string) {
        return Uri.encode(string);
    }

    /**
     * 将URL转义编码的字符串复原
     */
    public static String urlDecode(String string) {
        return Uri.decode(string);
    }

    /**
     * 判断字符串是否为空<br />null、""、"null"为true
     */
    public static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals("null");
    }
}

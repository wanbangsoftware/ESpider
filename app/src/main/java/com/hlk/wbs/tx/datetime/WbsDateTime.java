package com.hlk.wbs.tx.datetime;

import com.hlk.wbs.tx.custom.CustomConvert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TX 通讯协议中时间的转换类
 */
public class WbsDateTime {
    /**
     * 将日期转换成字节数组之后数组的长度。
     */
    private final byte DATA_LENGTH = 4;
    /**
     * 转换过程中年份数所占二进制的位数。
     */
    private final byte YEAR_LENGTH = 6;
    /**
     * 转换过程中月份数所占二进制的位数。
     */
    private final byte MONTH_LENGTH = 4;
    /**
     * 转换过程中日期数所占二进制的位数。
     */
    private final byte DAY_LENGTH = 5;
    /**
     * 转换过程中小时数所占二进制的位数。
     */
    private final byte HOUR_LENGTH = DAY_LENGTH;
    /**
     * 转换过程中分钟数所占二进制的位数。
     */
    private final byte MINUTE_LENGTH = YEAR_LENGTH;
    /**
     * 转换过程中秒钟数所占二进制的位数。
     */
    private final byte SECOND_LENGTH = YEAR_LENGTH;
    /**
     * 标记是否从二进制转换成时间格式。
     */
    private boolean binToDateTime = false;
    private Calendar calendar;
    private byte[] bytes = new byte[DATA_LENGTH];

    /**
     * 建立默认的类实例：将当前系统时间转换成字节数组。
     */
    public WbsDateTime() {
        binToDateTime = false;
        calendar = Calendar.getInstance();
    }

    /**
     * 建立一个类实例并且指定以时间转换成字节数组的方式。
     *
     * @param date 指定要转换的时间。
     */
    public WbsDateTime(Date date) {
        this.setDate(date);
    }

    /**
     * 建立一个类实例并且指定以字节数组转换成时间的方式。
     *
     * @throws Exception 如果传入的 b 为 null 或者 b 的长度不足，则抛出此异常。
     */
    public WbsDateTime(byte[] b) throws Exception {
        this.setBytes(b);
    }

    /**
     * 输入时间以供转换成字节数组。
     *
     * @param value 指定时间。
     */
    public void setDate(Date value) {
        calendar = Calendar.getInstance();
        calendar.setTime(value);
        binToDateTime = false;
    }

    /**
     * 获取已经转换成时间格式的时间数据。
     */
    public Date getDate() {
        return calendar.getTime();
    }

    /**
     * 输入字节数组以供转换成 DateTime 格式。
     *
     * @param value 需要转换成 DateTime 的字节数组。
     * @throws Exception 如果传入的 b 为 null 或者 b 的长度不足，则抛出此异常。
     */
    public void setBytes(byte[] value) throws Exception {
        if (value == null || value.length != DATA_LENGTH)
            throw new WbsDateTimeNullException();
        else {
            System.arraycopy(value, 0, bytes, 0, DATA_LENGTH);
            binToDateTime = true;
            calendar = Calendar.getInstance();
        }
    }

    /**
     * 获取已经转换成字节数组的时间数据。
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * 将 DateTime 转换成字节数组。
     */
    private void ConvertDateTimeToBytes() {
        int year = calendar.get(Calendar.YEAR) - 2000, month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DATE), hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE), second = calendar.get(Calendar.SECOND);
        String binStr = "";
        binStr += CustomConvert.IntToDigital(year, CustomConvert.Binary, YEAR_LENGTH);
        binStr += CustomConvert.IntToDigital(month, CustomConvert.Binary, MONTH_LENGTH);
        binStr += CustomConvert.IntToDigital(day, CustomConvert.Binary, DAY_LENGTH);
        binStr += CustomConvert.IntToDigital(hour, CustomConvert.Binary, HOUR_LENGTH);
        binStr += CustomConvert.IntToDigital(minute, CustomConvert.Binary, MINUTE_LENGTH);
        binStr += CustomConvert.IntToDigital(second, CustomConvert.Binary, SECOND_LENGTH);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) CustomConvert.DigitalToInt(binStr.substring(i * 8, i * 8 + 8), CustomConvert.Binary);
        }
    }

    /**
     * 将字节数组转换成 DateTime 类型。
     */
    private void ConvertBytesToDateTime() {
        String binString = "";
        for (byte b : bytes) {
            binString += CustomConvert.IntToDigital(b & 0xFF, CustomConvert.Binary, 8);
        }
        int iIndex = 0;
        int year = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + YEAR_LENGTH), CustomConvert.Binary) + 2000;

        iIndex += YEAR_LENGTH;
        int month = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + MONTH_LENGTH), CustomConvert.Binary);
        if (month == 0)
            month = 1;

        iIndex += MONTH_LENGTH;
        int day = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + DAY_LENGTH), CustomConvert.Binary);
        if (day == 0)
            day = 1;

        iIndex += DAY_LENGTH;
        int hour = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + HOUR_LENGTH), CustomConvert.Binary);

        iIndex += HOUR_LENGTH;
        int minute = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + MINUTE_LENGTH), CustomConvert.Binary);

        iIndex += MINUTE_LENGTH;
        int second = CustomConvert.DigitalToInt(binString.substring(iIndex, iIndex + SECOND_LENGTH), CustomConvert.Binary);

        //iIndex += SECOND_LENGTH;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        // 将获取到的数据转换成时间格式
        Date date = new Date();
        try {
            date = sdf.parse(String.format(Locale.getDefault(), "%d-%d-%d %d:%d:%d", year, month, day, hour, minute, second));
        } catch (ParseException e) {
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            cal.set(1900 + 2000, 1, 1, 0, 0, 0);
            date.setTime(cal.getTimeInMillis());
        }
        calendar.setTime(date);
    }

    /**
     * 执行转换过程。
     */
    public void ConvertDateTime() {
        if (binToDateTime)
            ConvertBytesToDateTime();
        else
            ConvertDateTimeToBytes();
    }
}

package com.hlk.wbs.tx;

import com.hlk.wbs.tx.Packable;
import com.hlk.wbs.tx.custom.CustomConvert;
import com.hlk.wbs.tx.datetime.WbsDateTime;

import java.util.Date;

/**
 * <b>功能</b>：GPS定位信息记录<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:46 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Location extends Packable {
    /**
     * 定位方式
     */
    public static class Provider {
        /**
         * GPS定位
         */
        public static final byte GPS = 0;
        /**
         * 网络定位
         */
        public static final byte Network = 1;
    }

    /**
     * 经度
     */
    public double latitude;
    /**
     * 维度
     */
    public double longitude;
    /**
     * 定位时间
     */
    public Date time;
    /**
     * 标记是否为报警信息
     */
    public boolean alarm;

    /**
     * 定位方式
     */
    public byte provider;

    public Location() {
        super();
    }

    /**
     * 定位是否有效
     */
    public boolean available() {
        return (latitude > 0 && latitude <= 90) && (longitude > 0 && longitude <= 180);
    }

    /**
     * 打包定位信息
     */
    public void packageMessage() {
        int tmp;
        if (alarm) {
            // 7020报警信息的经纬度是按照正常顺序来的
            // 打包 latitude
            tmp = (int) (CustomConvert.DD2GPRMC(latitude) * 10000.0);
            packageData(CustomConvert.intToBytes(tmp));
            // 打包 longitude
            tmp = (int) (CustomConvert.DD2GPRMC(longitude) * 10000.0);
            packageData(CustomConvert.intToBytes(tmp));
        } else {
            // 7030的经纬度顺序是反过来的
            // 打包 longitude
            tmp = (int) (CustomConvert.DD2GPRMC(longitude) * 10000.0);
            packageData(CustomConvert.intToBytes(tmp));
            // 打包 latitude
            tmp = (int) (CustomConvert.DD2GPRMC(latitude) * 10000.0);
            packageData(CustomConvert.intToBytes(tmp));
        }
        // 打包 time
        WbsDateTime wdt = new WbsDateTime(time);
        wdt.ConvertDateTime();
        packageData(wdt.getBytes());
    }
}

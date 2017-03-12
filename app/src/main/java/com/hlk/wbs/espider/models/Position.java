package com.hlk.wbs.espider.models;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Table;

/**
 * <b>功能</b>：本地缓存的定位信息<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 07:29 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
@Table("position")
public class Position extends Model {

    /**
     * 表所有的列名
     */
    public static class Columns {
        public static final String Report = "report";
        public static final String Alarm = "alarm";
        public static final String Latitude = "latitude";
        public static final String Longitude = "longitude";
        public static final String GpsTime = "gpsTime";
        public static final String ReportTime = "reportTime";
        public static final String Provider = "type";
    }

    @Column(Columns.Report)
    public boolean report;

    @Column(Columns.Alarm)
    public byte alarm;

    @Column(Columns.Latitude)
    public double latitude;

    @Column(Columns.Longitude)
    public double longitude;

    @Column(Columns.GpsTime)
    public long gpsTime;

    @Column(Columns.ReportTime)
    public long reportTime;

    @Column(Columns.Provider)
    public String provider;

    /**
     * 定位是否有效
     */
    public boolean available() {
        return (latitude > 0 && latitude <= 90) && (longitude > 0 && longitude <= 180);
    }
}

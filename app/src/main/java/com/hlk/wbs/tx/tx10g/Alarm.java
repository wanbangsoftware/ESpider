package com.hlk.wbs.tx.tx10g;

/**
 * <b>功能</b>：TX10G的报警信息<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:31 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Alarm {
    /**
     * 无报警或报警已解除状态。
     */
    public static final byte NoAlarm = 0x00;
    /**
     * GPS服务关闭
     */
    public static final byte GpsOff = 0x01;
    /**
     * GPS服务打开
     */
    public static final byte GpsOn = 0x02;
    /**
     * 网络已断开
     */
    public static final byte NetworkOff = 0x03;
    /**
     * 网络已连接
     */
    public static final byte NetworkOn = 0x04;
    /**
     * 休息时间里的定位信息，估约2小时一次汇报
     */
    public static final byte Sleeping = 0x05;
    /**
     * 用户关闭了GPS访问权限
     */
    public static final byte GpsPermissionOff = 0x06;
    /**
     * 用户打开了GPS访问权限
     */
    public static final byte GpsPermissionOn = 0x07;
    /**
     * 充电电源断警报。
     */
    public static final byte ChargingOff = 0x20;
    /**
     * 充电
     */
    public static final byte ChargingOn = 0x21;
    /**
     * 停车超时警报。
     */
    public static final byte StopTimeout = 0x40;
    /**
     * 电池电量过低警报。
     */
    public static final byte BatteryLow = (byte) 0x80;
    /**
     * 没有GPS访问权限
     */
    public static final byte NoGpsPermission = (byte) 0xFD;
    /**
     * 设备关机
     */
    public static final byte Shutdown = (byte) 0xFE;
    /**
     * 软件被卸载
     */
    public static final byte Uninstalled = (byte) 0xFF;

    /**
     * 获取报警信息
     */
    public static String getAlarm(byte alarm) {
        switch (alarm) {
            case NoAlarm:
                return "Tracking";
            case GpsOff:
                return "GpsOff";
            case GpsOn:
                return "GpsOn";
            case NetworkOff:
                return "NetworkOff";
            case NetworkOn:
                return "NetworkOn";
            case Sleeping:
                return "Sleeping";
            case GpsPermissionOff:
                return "GpsPermissionOff";
            case GpsPermissionOn:
                return "GpsPermissionOn";
            case ChargingOff:
                return "ChargingOff";
            case ChargingOn:
                return "ChargingOn";
            case StopTimeout:
                return "StopTimeout";
            case BatteryLow:
                return "BatteryLow";
            case NoGpsPermission:
                return "NoGpsPermission";
            case Shutdown:
                return "Shutdown";
            case Uninstalled:
                return "Uninstalled";
            default:
                return "Unknown";
        }
    }
}

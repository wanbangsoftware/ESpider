package com.hlk.wbs.espider.services;

import android.location.LocationManager;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.tx.tx10g.Alarm;

/**
 * <b>功能：</b>提供基本定位服务操作的底层服务<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/08 11:20 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public abstract class LocationService extends GMSService {

    /**
     * 启动所有可以使用的位置服务
     */
    protected void startLocationService() {
        // 获取当前用户所处区域
        userBelong = PreferenceHelper.get(R.string.preference_tag_app_account_belong, Account.Belongs.MNG);
        //if (userBelong.equals(Account.Belongs.CHN)) {
        // 中国境内启动百度地图定位
        //    initializeBDLocationService();
        //} else {
        initializeGPSLocation();
        //}
    }

    /**
     * 停止所有位置服务
     */
    protected void stopLocationService() {
        stopGPSLocation();
        stopBDLocation();
        stopGooglePlayLocationService();
    }

    /**
     * 标记是否报告过GPS被禁用的报警
     */
    private boolean isGpsDisableAlarmReported = false;
    /**
     * 标记是否报告过GPS已启用的报警
     */
    private boolean isGpsEnableAlarmReported = true;

    /**
     * 检测定位状态改变
     */
    protected void checkLocationProviderStatus() {
        if (hasGPSAccessPermission()) {
            gpsProviderEnable = isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkProviderEnable = isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // 如果gps和network都不能用则上报一个报警
            if (!gpsProviderEnable && !networkProviderEnable) {
                reportGpsDisableAlarm();
                stopLocationService();
            } else {
                reportGpsEnableAlarm();
                // 停止旧的location service
                stopLocationService();
                // 开启新的location service
                startLocationService();
            }
            log(format("provider gps: %s, network provider: %s", gpsProviderEnable, networkProviderEnable));
        }
    }

    protected void reportGpsPermissionAlarm(boolean enable) {
        saveNewestPosition(true, enable ? Alarm.GpsPermissionOn : Alarm.GpsPermissionOff);
    }

    /**
     * 上报GPS启用的报警
     */
    private void reportGpsEnableAlarm() {
        if (!isGpsEnableAlarmReported) {
            isGpsEnableAlarmReported = true;
            // 启用过后再禁用时，需要上报禁用报警
            isGpsDisableAlarmReported = false;
            //saveNewestPosition(true, Alarm.GpsOn);
            log("location service enabled");
        }
    }

    /**
     * 上报GPS禁用的报警
     */
    private void reportGpsDisableAlarm() {
        if (!isGpsDisableAlarmReported) {
            isGpsDisableAlarmReported = true;
            // 禁用过后再启用时，需要上报启用报警
            isGpsEnableAlarmReported = false;
            saveNewestPosition(true, Alarm.GpsOff);
            log("location service disabled");
        }
    }
}

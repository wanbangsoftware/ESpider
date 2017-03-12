package com.hlk.wbs.espider.services;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.AppType;

/**
 * <b>功能</b>：提供GPS定位的服务<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 19:03 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class GPSService extends LocatableService {

    // Acquire a reference to the system Location Manager
    private LocationManager locationManager;

    private boolean isGpsLocationStarted = false;

    private int fetchingTimes = 0;

    /**
     * 启动GPS位置服务
     */
    protected void initializeGPSLocation() {
        // 只在非中国境内启动本app的时候启动GPS定位服务
        if (App.getInstance().client != AppType.EVERDIGM)
            return;

        initializeManager();
        requestLocationUpdate();
    }

    private void initializeManager() {
        if (null == locationManager) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /**
     * 检测指定的定位是否可用
     */
    protected boolean isProviderEnabled(String provider) {
        initializeManager();
        return null != locationManager && locationManager.isProviderEnabled(provider);
    }

    private void requestLocationUpdate() {
        if (hasGPSAccessPermission()) {
            // 网络可用时用网络定位，否则用GPS定位
            String provider = App.getInstance().isNetworkAvailable() ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
            if (null != locationManager) {
                // 先使用最佳定位方式
                if (isProviderEnabled(provider)) {
                    requestLocationUpdate(provider);
                } else {
                    if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                        if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            requestLocationUpdate(LocationManager.GPS_PROVIDER);
                        } else {
                            log(format("Provider %s is can not attached.", provider));
                        }
                    } else {
                        log(format("Provider %s is can not attached.", provider));
                    }
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdate(String provider) {
        currentUsedProvider = provider;
        setProviderEnabled(provider, true);
        if (!isGpsLocationStarted) {
            isGpsLocationStarted = true;
            fetchingTimes = 0;
            locationManager.requestLocationUpdates(provider, MIN_TIME, MID_DISTANCE, locationListener);
            log("GPS location start.");
        } else {
            log("GPS location is already started.");
        }
    }

    /**
     * 标记当前所用的provider
     */
    private void setProviderEnabled(String provider, boolean enabled) {
        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            networkProviderEnable = enabled;
        } else {
            gpsProviderEnable = enabled;
        }
    }

    /**
     * 停止GPS定位服务
     */
    @SuppressWarnings("MissingPermission")
    protected void stopGPSLocation() {
        fetchingTimes = 0;
        if (hasGPSAccessPermission()) {
            if (null != locationManager) {
                if (isGpsLocationStarted) {
                    locationManager.removeUpdates(locationListener);
                    isGpsLocationStarted = false;
                    log("GPS location stop.");
                }
            }
        }
    }

    /**
     * GPS位置活动监听
     */
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLastLocation(location);
            fetchingTimes++;
            if (fetchingTimes >= LOCATION_FETCHING_TIMES) {
                log("This wake up window has now stop.");
                stopGPSLocation();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            int extra = 0;
            if (null != extras) {
                extra = extras.getInt("satellites", 0);
            }
            log(format("Location provider %s status changed: %s, satellites: %d.", provider, getProviderStatus(status), extra));
        }

        @Override
        public void onProviderEnabled(String provider) {
            setProviderEnabled(provider, true);
            log(format("Location provider %s enabled.", provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
            setProviderEnabled(provider, false);
            log(format("Location provider %s disabled.", provider));
        }

        private String getProviderStatus(int status) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    return "Available";
                case LocationProvider.OUT_OF_SERVICE:
                    return "Out of Service";
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    return "Temporarily unavailable";
                default:
                    return "Unknown";
            }
        }
    };

}

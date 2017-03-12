package com.hlk.wbs.espider.services;

import android.location.Location;

import com.baidu.location.BDLocation;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.helpers.BaiduApiHelper;
import com.hlk.wbs.espider.helpers.BaiduApiHelper.OnBDLocatedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <b>功能</b>：百度api提供定位服务<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 19:51 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class BDService extends GPSService {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private boolean baiduInitialized = false;
    private boolean isBaiduLocationStarted = false;

    /**
     * 初始化百度地图api
     */
    protected void initializeBDLocationService() {
//        if (App.getInstance().client != AppType.CHINA) {
//            return;
//        }
        // 百度地图SDK初始化
        if (!baiduInitialized) {
            if (hasGPSAccessPermission()) {
                App.getInstance().initializeBaiduApi();
                baiduInitialized = true;
                currentUsedProvider = BaiduApiHelper.NAME;
                log("baidu map api has initialized.");
            }
        }

        if (hasGPSAccessPermission()) {
            if (!isBaiduLocationStarted) {
                BaiduApiHelper.Instance().stopWhenLocated(true).addOnLocatedListener(mOnLocatedListener).start();
                isBaiduLocationStarted = true;
                log("baidu map api has locating...");
            }
        }
    }

    /**
     * 停止百度定位
     */
    protected void stopBDLocation() {
        if (isBaiduLocationStarted) {
            BaiduApiHelper.Instance().stop();
            isBaiduLocationStarted = false;
            log("baidu location stop.");
        }
    }

    private OnBDLocatedListener mOnLocatedListener = new OnBDLocatedListener() {

        @Override
        public void onLocated(boolean success, BDLocation location) {
            log(format("baidu map api has located %s", success));
            if (success) {
                Location located = new Location(BaiduApiHelper.NAME);
                located.setAltitude(location.getAltitude());
                located.setLatitude(location.getLatitude());
                located.setLongitude(location.getLongitude());
                located.setSpeed(location.getSpeed());
                located.setTime(getTime(location.getTime()));
                // 更新最后的定位信息
                updateLastLocation(located);
            }
            // 标记百度地图定位已经停止
            isBaiduLocationStarted = false;
        }

        /**将百度定位时间变换成GMT时间*/
        private long getTime(String baiduTime) {
            Date date;
            try {
                date = sdf.parse(baiduTime);
            } catch (ParseException e) {
                date = new Date();
            }
            return date.getTime();
        }
    };
}

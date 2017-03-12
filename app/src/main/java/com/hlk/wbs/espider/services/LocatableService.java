package com.hlk.wbs.espider.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnLiteOrmTaskExecuteListener;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.BaiduApiHelper;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.Position;
import com.hlk.wbs.espider.tasks.OrmTask;
import com.hlk.wbs.espider.tasks.PeriodReportTask;
import com.hlk.wbs.tx.tx10g.Alarm;
import com.hlk.wbs.tx.tx10g.CMD7030;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <b>功能</b>：提供Locate相关功能的服务基类<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 19:23 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class LocatableService extends BaseService {

    /**
     * 默认用户所处区域为蒙古
     */
    protected String userBelong = Account.Belongs.MNG;
    /**
     * 默认获取成功3次位置信息之后停止当前循环
     */
    protected static final int LOCATION_FETCHING_TIMES = 3;
    /**
     * 当前网络状态是否可用
     */
    protected boolean networkAvailable = App.getInstance().isNetworkAvailable();
    /**
     * 定位时间间隔
     */
    private static final long LOCATE_INTERVAL = 1000 * 60 * 10;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    /**
     * GPS位置更新最小时间间隔
     */
    public static final long MIN_TIME = 10000;
    /**
     * GPS位置更新最小移动距离
     */
    public static final float MID_DISTANCE = 20.0f;

    /**
     * 启动定期获取位置信息的Action
     */
    public static final String ACTION_FETCHING_LOCATION = Action.BASE_ACTION + ".service.FETCHING_LOCATION";
    /**
     * 汇报定期报告的Action
     */
    public static final String ACTION_REPORT_LOCATION = Action.BASE_ACTION + ".service.REPORT_LOCATION";
    /**
     * 定位时检测是否为GPS定位，并在开始location之后2分钟内判断是否关闭，如果没有关闭则将gps请求关闭
     */
    public static final String ACTION_CHECK_GPS_PROVIDER = Action.BASE_ACTION + ".service.CHECK_GPS_PROVIDER";
    /**
     * 检测GPS provider是否启动的时间间隔
     */
    public static final long GPS_PROVIDER_CHECK_INTERVAL = 1000 * 60 * 2;

    /**
     * 标记gps和network定位功能是否可用
     */
    protected boolean gpsProviderEnable = false, networkProviderEnable = false;
    /**
     * 当前所用的provider：gps/network/gms/baidu
     */
    protected String currentUsedProvider;

    /**
     * 设置app中管理的网络状态变化
     */
    protected void setNetworkAvailable(boolean available) {
        if (networkAvailable != available) {
            networkAvailable = available;
            // 保存网络连接状态变化记录
            saveNewestPosition(true, networkAvailable ? Alarm.NetworkOn : Alarm.NetworkOff);
        }
    }

    /**
     * 检测是否有访问GPS的权限
     */
    protected boolean hasGPSAccessPermission() {
        boolean has = true;
        if (Build.VERSION.SDK_INT >= 23) {
            // 检测是否有相应的权限
            boolean fine = Permission.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            boolean coarse = Permission.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (!fine && !coarse) {
                log("No GPS service access permission.");
                has = false;
            }
        }
        App.getInstance().setGpsPermissionChanged(has);
        return has;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取缓存中最后一次定位的记录
        if (null == lastSavedPosition) {
            getLastPosition();
        }
    }

    private void getLastPosition() {
        if (null == App.Orm)
            return;

        QueryBuilder<Position> builder = new QueryBuilder<>(Position.class)
                .appendOrderDescBy(Position.Columns.GpsTime).limit(0, 1);
        List<Position> list;
        try {
            if (Permission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                list = App.Orm.query(builder);
            } else {
                list = null;
            }
        } catch (Exception e) {
            log("Cannot fetch the last position, please check the permission");
            list = null;
        }
        if (null != list && list.size() > 0) {
            lastSavedPosition = list.get(0);
            log(format("Last position is at: %s", sdf.format(new Date(lastSavedPosition.gpsTime))));
        }
    }

    /**
     * 最后一个保存的定位
     */
    protected Position lastSavedPosition = null;
    /**
     * 最后一个获取到的定位
     */
    protected Location lastLocation = null;

    protected void updateLastLocation(Location location) {
        // 最后的定位位置为空或当前定位更好于最后定位时更新定位信息
        if ((null == lastLocation) || isBetterLocation(location, lastLocation)) {
            boolean isBaidu = currentUsedProvider.equals(BaiduApiHelper.NAME);
            if (null == lastLocation && isBaidu) {
                lastLocation = new Location("");
            }
            if (isBaidu) {
                // 如果是百度地图定位过来的
                lastLocation.setProvider(location.getProvider());
                lastLocation.setAltitude(location.getAltitude());
                lastLocation.setLatitude(location.getLatitude());
                lastLocation.setLongitude(location.getLongitude());
                lastLocation.setSpeed(location.getSpeed());
                lastLocation.setTime(location.getTime());
            } else {
                lastLocation = location;
            }
            // 尝试保存最后一条定位信息
            saveNewestPosition(false, Alarm.NoAlarm);
        }
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 返回当前定位信息与最后一条保存的定位信息之间的距离差
     */
    private float distanceToLastLocation() {
        if (null == lastSavedPosition) return 100f;
        Location location = new Location("last");
        location.setLatitude(lastSavedPosition.latitude);
        location.setLongitude(lastSavedPosition.longitude);
        return location.distanceTo(lastLocation);
    }

    /**
     * 保存一条最新的定位信息
     *
     * @param directSave 是否直接存储当前最新的定位信息
     * @param alarm      警报标志
     */
    @SuppressWarnings({"unchecked", "MissingPermission"})
    protected void saveNewestPosition(boolean directSave, byte alarm) {
        if (alarm != Alarm.NoAlarm) {
            log(format("save alarm: %s", Alarm.getAlarm(alarm)));
        }
        boolean save = false;
        if (null == lastSavedPosition) {
            log("Last position not exist, now have new one.");
            save = true;
        } else {
            if (directSave) {
                save = true;
                // 这里已经具有gps访问的权限了
                if (hasGPSAccessPermission()) {
                    // 是否保存系统中最后已知的定位点
                    boolean saveLast = true;
                    LocationManager manager = ((LocationManager) getSystemService(LOCATION_SERVICE));
                    if (null != manager && !Utils.isEmpty(currentUsedProvider) && manager.isProviderEnabled(currentUsedProvider)) {
                        Location lastKnownLocation = manager.getLastKnownLocation(currentUsedProvider);
                        if (null != lastKnownLocation) {
                            saveLast = false;
                            lastLocation = lastKnownLocation;
                        }
                    }
                    if (saveLast) {
                        // 这里最后已知的点在网络断的情况下使用gps的话，有可能是null，所以判断一下，如果为null的话
                        // 直接把最后有效的定位时间改一下就好了
                        if (null != lastLocation) {
                            lastLocation.setTime(System.currentTimeMillis());
                        } else {
                            log("No known location since last start app.");
                        }
                    }
                }
            } else {
                // 如果当前定位时间与上一次定位的时间超过了预定的时间间隔则需要保存一条定位记录
                long interval = Math.abs(lastLocation.getTime() - getLastLocated());
                if (interval >= LOCATE_INTERVAL) {
                    float distance = distanceToLastLocation();
                    if (distance >= 50.f) {
                        log("Need a new position with time establish.");
                        save = true;
                    } else {
                        saveLastLocated(0);
                        log("Distance less than 50m, point has been ignored.");
                    }
                }
            }
        }
        if (save && null != lastLocation) {
            lastSavedPosition = new Position();
            lastSavedPosition.gpsTime = lastLocation.getTime();
            lastSavedPosition.alarm = alarm;
            lastSavedPosition.report = false;
            lastSavedPosition.latitude = lastLocation.getLatitude();
            lastSavedPosition.longitude = lastLocation.getLongitude();
            lastSavedPosition.provider = currentUsedProvider;
            lastSavedPosition.reportTime = 0L;
            new OrmTask<>(Position.class).addOnLiteOrmTaskExecuteListener(mOnTaskExecuteListener).exec();
            if (!directSave) {
                // 非报警直接保存的时候才保存最后定位时间
                saveLastLocated(0);
            }
        }
    }

    private void saveLastLocated(long value) {
        if (value == 0) value = System.currentTimeMillis();
        // 保存最后定位的时间
        PreferenceHelper.save(R.string.preference_tag_app_last_located, String.valueOf(value));
    }

    private void saveLastReported(long value) {
        if (value == 0) value = System.currentTimeMillis();
        // 保存最后顶起报告时间
        PreferenceHelper.save(R.string.preference_tag_app_last_reported, String.valueOf(value));
    }

    private OnLiteOrmTaskExecuteListener<Position> mOnTaskExecuteListener = new OnLiteOrmTaskExecuteListener<Position>() {
        @Override
        public void onPrepared() {

        }

        @Override
        public boolean isExecutingWithModify() {
            return true;
        }

        @Override
        public List<Position> executing(Object object) {
            if (null != App.Orm) {
                try {
                    if (Permission.hasPermission(LocatableService.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        App.Orm.save(lastSavedPosition);
                        // 保存完了之后查询未发送的条数
                        QueryBuilder<Position> queryBuilder = new QueryBuilder<>(Position.class).whereEquals(Position.Columns.Report, false);
                        return App.Orm.query(queryBuilder);
                    }
                } catch (Exception ignore) {
                    log("Cannot save current position, please check the permission.");
                }
            }
            return null;
        }

        @Override
        public void progressing(int percentage) {

        }

        @Override
        public void onExecuted(List<Position> list) {
            if (null != list && list.size() >= CMD7030.MAX_LOCATION) {
                // 如果暂存的未发数据大于10条时，需要向服务器发送
                new PeriodReportTask().exec();
            }
        }
    };

    /**
     * 向服务器汇报定位信息
     */
    protected void reportPositions() {
        // 保存最后一次定期报告时间
        saveLastReported(0);
        new PeriodReportTask().exec();
        // 到时间定期报告，约定下次报告时间
        scheduleNextPeriodReport();
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * 开始定时获取位置信息
     */
    protected void startFetchingLocation() {
        // 取消之前所有alarm
        stopFetchingLocation();
        // 取消之前定期报告的alarm
        stopReportingLocation();

        scheduleNextLocation();
        scheduleNextPeriodReport();
    }

    /**
     * 默认定时每一小时汇报一次
     */
    private static final long STATIC_REPORT_INTERVAL = 60 * 60 * 1000;

    /**
     * 开启定时汇报定期报告
     */
    private void scheduleNextPeriodReport() {
        long last = getLastReported();
        long current = System.currentTimeMillis();
        long interval = current - last;

        if (interval < STATIC_REPORT_INTERVAL) {
            interval = STATIC_REPORT_INTERVAL - interval;
            if (interval < 20000) {
                interval = STATIC_REPORT_INTERVAL;
            }
        } else if (interval > STATIC_REPORT_INTERVAL * 1.5) {
            // 超过1.5倍汇报时间的话，则在10s之后马上汇报
            interval = 10000;
        } else {
            interval = STATIC_REPORT_INTERVAL;
        }

        // 下次启动的时间
        long next = current + interval;
        log(format("delay %d milliseconds next period report, about at: %s", interval, sdf.format(new Date(next))));

        setAlarm(next, getOperationIntent(ACTION_REPORT_LOCATION));
    }

    /**
     * 设置闹铃
     */
    private void setAlarm(long time, PendingIntent operation) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC_WAKEUP, time, operation);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, time, operation);
        }
    }

    /**
     * 取消闹铃
     */
    private void cancelAlarm(PendingIntent operation) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(operation);
    }

    private PendingIntent getOperationIntent(String action) {
        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 停止定时获取定期报告
     */
    private void stopReportingLocation() {
        cancelAlarm(getOperationIntent(ACTION_REPORT_LOCATION));
    }

    /**
     * 最后汇报的时间
     */
    private long getLastReported() {
        String tmp = PreferenceHelper.get(R.string.preference_tag_app_last_reported, "0");
        long last = Long.valueOf(tmp);
        log(format("last period report is at: %s", sdf.format(new Date(last))));
        if (last == 0) {
            last = System.currentTimeMillis() - STATIC_REPORT_INTERVAL - 1000;
            saveLastReported(last);
        }
        return last;
    }

    /**
     * 获取上一次成功保存定位的时间
     */
    private long getLastLocated() {
        String tmp = PreferenceHelper.get(R.string.preference_tag_app_last_located, "0");
        long last = Long.valueOf(tmp);
        if (0 == last) {
            last = System.currentTimeMillis() - LOCATE_INTERVAL - 1000;
            saveLastLocated(last);
        }
        if (null == lastSavedPosition) {
            return last;
        } else if (last > lastSavedPosition.gpsTime) {
            return last;
        }
        return lastSavedPosition.gpsTime;
    }

    /**
     * 设定下次获取定位的时间
     */
    protected void scheduleNextLocation() {

        long last = getLastLocated();
        long current = System.currentTimeMillis();
        long interval = current - last;

        if (interval < LOCATE_INTERVAL) {
            interval = LOCATE_INTERVAL - interval;
            if (interval < 10000) {
                interval = LOCATE_INTERVAL;
            }
        } else {
            interval = LOCATE_INTERVAL;
        }

        // 下次启动的时间
        long next = current + interval;
        log(format("delay %d milliseconds next location, about at: %s", interval, sdf.format(new Date(next))));

        setAlarm(next, getOperationIntent(ACTION_FETCHING_LOCATION));
        if (!Utils.isEmpty(currentUsedProvider) && currentUsedProvider.equals(LocationManager.GPS_PROVIDER)) {
            next = current + GPS_PROVIDER_CHECK_INTERVAL;
            // 如果当前启动的是通过GPS方式获取定位则在2分钟后再次检查是否启动，如果启动则关闭，要不然会一直耗费电池
            setAlarm(next, getOperationIntent(ACTION_CHECK_GPS_PROVIDER));
            log(format("delay %d milliseconds to re-check gps provider and stop it, about at: %s", GPS_PROVIDER_CHECK_INTERVAL, sdf.format(new Date(next))));
        }
    }

    /**
     * 停止定期获取位置
     */
    protected void stopFetchingLocation() {
        cancelAlarm(getOperationIntent(ACTION_FETCHING_LOCATION));
    }
}

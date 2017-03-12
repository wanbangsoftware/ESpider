package com.hlk.wbs.espider.applications;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.google.gson.Gson;
import com.hlk.hlklib.etc.Cryptography;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.AppType;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.services.BaseService;

import java.util.List;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/01 11:21 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class BaseApplication extends Application {

    public static final String TAG = "everdigm";
    protected static BaseApplication instance;

    private static final String ipStart = "http://10.141.130.";

    public int client = AppType.EVERDIGM;

    private String apiUrl;

    public void resetApiUrl() {
        apiUrl = null;
        getApiUrl();
    }

    public String getApiUrl() {
        if (Utils.isEmpty(apiUrl)) {
//            boolean have = false;
//            if (BuildConfig.DEBUG) {
//                String gateway = getGateway();
//                if (!Utils.isEmpty(gateway)) {
//                    if (gateway.endsWith(".130.1")) {
//                        apiUrl = StringHelper.format("%s%s/", ipStart, StringHelper.getString(R.string.ui_url_default_local_h));
//                        have = true;
//                    } else if (gateway.startsWith("10.141.130.")) {
//                        apiUrl = StringHelper.format("%s%s/", ipStart, StringHelper.getString(R.string.ui_url_default_local_o));
//                        have = true;
//                    } else if (DeviceUtility.isEmulator() && gateway.equals("0.0.0.0")) {
//                        // 模拟器
//                        apiUrl = "http://10.0.2.2/";
//                        have = true;
//                    }
//                }
//            }
//            if (!have) {
                apiUrl = StringHelper.getString(R.string.app_url_default);
//            }
        }
        return apiUrl;
    }

    private String getGateway() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (null != wifiManager) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (null != dhcpInfo) {
                return intToIp(dhcpInfo.gateway);
            }
        }
        return null;
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        client = getResources().getInteger(R.integer.app_version_type);
    }

    /**
     * 静态缓存名称
     */
    public static String staticName() {
        return Cryptography.sha1(StringHelper.getString(R.string.app_url_default));
    }

    /**
     * 检测当前进程是否为主进程
     */
    public boolean shouldInitialize() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        boolean ret = false;
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    /**
     * 获取application中配置的meta-data值
     */
    public String getMetadata(String name) {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前apk的版本号
     */
    public String version() {
        String version = "";
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取当前手机的制造商
     */
    public String manufacturer() {
        return Build.MANUFACTURER;
    }

//    private Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
//        @Override
//        public boolean shouldSkipField(FieldAttributes f) {
//            return f.getDeclaringClass().equals(RealmObject.class);
//        }
//
//        @Override
//        public boolean shouldSkipClass(Class<?> clazz) {
//            return false;
//        }
//    }).create();

    private Gson gson = new Gson();

    public Gson Gson() {
        return gson;
    }

    public boolean bindAccountWarning = false;
    /**
     * 设备ID，由 Manufacturer:Model:UniqueId 组成
     */
    private String deviceId = "";

    private void setDeviceId() {
        boolean bo = Utils.isEmpty(deviceId);
        buildDeviceId();
        if (bo) {
            // 如果当前的device id为空则发送广播
            sendBroadcast(new Intent(Action.CONNECT_ACTION));
        }
    }

    private void buildDeviceId() {
        String device = PreferenceHelper.get(R.string.preference_tag_app_unique_code);
        deviceId = StringHelper.format("%s:%s:%s", Build.MANUFACTURER, Build.MODEL, device);
    }

    public void setUniqueCode(String uniqueCode) {
        // 保存缓存中的设备唯一代码
        String value = PreferenceHelper.get(R.string.preference_tag_app_unique_code, "");
        if (Utils.isEmpty(value) || !value.equals(uniqueCode)) {
            PreferenceHelper.save(R.string.preference_tag_app_unique_code, uniqueCode);
        }
        setDeviceId();
    }

    public String getDeviceId() {
        if (Utils.isEmpty(deviceId)) {
            buildDeviceId();
        }
        return deviceId;
    }

    /**
     * 查看网络连接是否可用
     */
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            NetworkInfo networkinfo = manager.getActiveNetworkInfo();
            return networkinfo != null && networkinfo.isConnected();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向本地缓存中放入GPS权限改动标记
     */
    public void setGpsPermissionChanged(boolean enable) {
        String value = PreferenceHelper.get(R.string.preference_tag_app_gps_permission, "false");
        boolean old = Boolean.valueOf(value);
        if (old != enable) {
            PreferenceHelper.save(R.string.preference_tag_app_gps_permission, String.valueOf(enable));
            Intent intent = new Intent(Action.GPS_PERMISSION_CHANGED_ACTION);
            intent.putExtra(BaseService.TOPIC, enable);
            sendBroadcast(intent);
        }
    }

    private int networkNotAvailableWarning = 0;

    public int getNetworkNotAvailableWarning() {
        return networkNotAvailableWarning;
    }

    public void setNetworkNotAvailableWarning() {
        networkNotAvailableWarning++;
    }

    private int serviceNotReachableWarning = 0;

    public int getServiceNotReachableWarning() {
        return serviceNotReachableWarning;
    }

    public void setServiceNotReachableWarning() {
        serviceNotReachableWarning++;
    }
}

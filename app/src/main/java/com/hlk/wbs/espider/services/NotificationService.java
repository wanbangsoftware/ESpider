package com.hlk.wbs.espider.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.api.Api;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.JsonResult;
import com.hlk.wbs.espider.tasks.SimpleHttpTask;
import com.hlk.wbs.tx.tx10g.Alarm;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 作者：Hsiang Leekwok on 2015/09/15 15:13<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class NotificationService extends UpdateService {

    // this is the log tag
    public static final String TAG = "Notify";

    // the IP address, where your MQTT broker is running.
    // private static final String MQTT_HOST = "10.0.2.2";
    // the port at which the broker is running.
    private static int MQTT_BROKER_PORT_NUM = 1883;
    // Let's not use the MQTT persistence.
    // private static MqttPersistence MQTT_PERSISTENCE = null;
    // We don't need to remember any state between the connections, so we use a
    // clean start.
    private static boolean MQTT_CLEAN_SESSION = true;
    // Let's set the internal keep alive for MQTT to 10 mins. I haven't tested
    // this value much. It could probably be increased.
    private static short MQTT_KEEP_ALIVE = 60 * 5;
    // Set quality of services to 0 (at most once delivery), since we don't want
    // push notifications
    // arrive more than once. However, this means that some messages might get
    // lost (delivery is not guaranteed)
    private static int[] MQTT_QUALITIES_OF_SERVICE = {0};
    private static int MQTT_QUALITY_OF_SERVICE = 0;
    // Set timeout of client
    private static final int MQTT_CONNECT_TIMEOUT = 5;
    // The broker should not retain any messages.
    private static boolean MQTT_RETAINED_PUBLISH = false;

    // These are the actions for the service (name are descriptive enough)
    public static final String ACTION_START = Action.BASE_ACTION + ".service.START";
    public static final String ACTION_STOP = Action.BASE_ACTION + ".service.STOP";
    private static final String ACTION_KEEPALIVE = Action.BASE_ACTION + ".service.KEEP_ALIVE";
    private static final String ACTION_RECONNECT = Action.BASE_ACTION + ".service.RECONNECT";
    private static final String ACTION_SETTING = Action.BASE_ACTION + ".service.SETTING";

    // notification settings
    /**
     * 本地设置：是否开启通知
     */
    public static final String LOCAL_SETTING_NOTIFICATION_ENABLE = "lsn_enable";
    /**
     * 本地设置：是否开启声音通知（在允许通知的情况下）
     */
    public static final String LOCAL_SETTING_NOTIFICATION_SOUND = "lsn_sound";
    /**
     * 本地设置：是否开启震动（在允许通知的情况下）
     */
    public static final String LOCAL_SETTING_NOTIFICATION_VIBRATE = "lsn_vibrate";
    /**
     * 本地设置：是否开启呼吸灯（在允许通知的情况下）
     */
    public static final String LOCAL_SETTING_NOTIFICATION_LIGHT = "lsn_light";

    /**
     * 全局处理mqtt消息的线程
     */
    private HandlerThread mHandlerThread = null;
    /**
     * 通知handlerthread的handler
     */
    private Handler mHandler = null;
    public static final String NOTIFICATION_TAG = "_lxbg_notification_tag_";
    /**
     * 全局系统通知的ID
     */
    private final static int NOTIFICATION_ID = 0x1FFFFFFF;
    private int mNotificationRequestCode = 0;

    private ConnectivityChangedBroadcastReceiver mConnectivityChanged;
    // Preferences instance
    //private SharedPreferences mPrefs;

    // Whether or not the service has been started.
    private boolean mStarted;

    // This the application level keep-alive interval, that is used by the AlarmManager
    // to keep the connection active, even when the device goes to sleep.
    private static final long KEEP_ALIVE_INTERVAL = 1000 * 60 * 6;

    // Retry intervals, when the connection is lost.
    private static final long INITIAL_RETRY_INTERVAL = 1000 * 30;
    private static final long MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;

    // We store in the preferences, whether or not the service has been started
    private static final String PREF_STARTED = "serviceIsStarted";
    // We store the last retry interval
    private static final String PREF_RETRY = "serviceRetryInterval";

    // This is the instance of an MQTT connection.
    private MQTTConnectionPaho mConnection;
    private long mStartTime;

    // Connection log for the push service. Good for debugging.
    //private ChatLog mLog;

    // Static method to start the service
    public static void actionStart(Context context) {
        Intent i = new Intent(context, NotificationService.class);
        i.setAction(ACTION_START);
        context.startService(i);
    }

    // Static method to stop the service
    public static void actionStop(Context context) {
        Intent i = new Intent(context, NotificationService.class);
        i.setAction(ACTION_STOP);
        context.startService(i);
    }

    // Static method to send a keep alive message
    public static void actionPing(Context context) {
        Intent i = new Intent(context, NotificationService.class);
        i.setAction(ACTION_KEEPALIVE);
        context.startService(i);
    }

    // log helper function
    @Override
    protected void log(String message) {
        log(message, null);
    }

    @Override
    protected void log(String message, Throwable e) {
        super.log(message, e);
    }

    private void prepareThread() {
        mHandlerThread = new HandlerThread("mqtt_handler_thread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        log("Creating service");
        mStartTime = System.currentTimeMillis();
        // 开始重新计算alarm
        startFetchingLocation();

        prepareThread();

        refreshLocalSetting();
        // Register a connectivity listener
        try {
            registerBroadcastReceiver();
        } catch (Exception ignore) {
        }
        /*
         * If our process was reaped by the system for any reason we need to
		 * restore our state with merely a call to onCreate. We record the last
		 * "started" value and restore it here if necessary.
		 */
        handleCrashedService();
    }

    // This method does any necessary clean-up need in case the server has been
    // destroyed by the system
    // and then restarted
    private void handleCrashedService() {
        if (wasStarted()) {
            log("Handling crashed service...");
            // stop the keep alives
            stopKeepAlives();

            // Do a clean start
            start();
        }
    }

    private void handleServiceDown() {
        Intent intent = new Intent();
        intent.setAction(Action.SERVICE_DOWN_ACTION);
        sendBroadcast(intent, PERMISSION_MQTT_MSG);
    }

    @Override
    public void onDestroy() {
        log("Service destroyed (started=" + mStarted + ")");
        stopFetchingLocation();
        // Stop the services, if it has been started
        if (mStarted) {
            stop();
        }

        // Remove the connectivity receiver
        try {
            if (null != mConnectivityChanged) {
                unregisterReceiver(mConnectivityChanged);
            }
        } catch (Exception ignore) {
        }

        handleServiceDown();

        // 停止handlerThread
        mHandler.removeCallbacks(mHandleThreadRunnable);
        mHandlerThread.quit();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // log("Service started with intent = " + intent + ", startId: " + startId);
        // Do an appropriate action based on the intent.
        if (null != intent) {
            String action = intent.getAction();
            log(String.format("Start command by action: %s", action));

            // action为null时默认是启动服务
            if (Utils.isEmpty(action)) action = ACTION_START;

            switch (action) {
                case ACTION_STOP:
                    clearClientInfo();
                    stop();
                    stopSelf();
                    break;
                case ACTION_START:
                    start();
                    break;
                case ACTION_KEEPALIVE:
                    keepAlive();
                    break;
                case ACTION_RECONNECT:
                    if (isNetworkAvailable()) {
                        reconnectIfNecessary();
                    }
                    break;
                case ACTION_SETTING:
                    // 更改了链接关系，需要重新连接
                    stop();
                    start();
                    break;
                case ACTION_FETCHING_LOCATION:
                    // 尝试启动本服务
                    //start();
                    // 停止已经存在了的位置服务
                    stopGPSLocation();
                    stopBDLocation();
                    log("Alarm up, stop old instance and start fetching location");
                    sendBroadcast(new Intent(Action.GPS_READY_ACTION));
                    scheduleNextLocation();
                    break;
                case ACTION_CHECK_GPS_PROVIDER:
                    log("Re-check gps provider and try to stop it to saving the power.");
                    if (!Utils.isEmpty(currentUsedProvider) && currentUsedProvider.equals(LocationManager.GPS_PROVIDER)) {
                        stopGPSLocation();
                        stopBDLocation();
                    }
                    break;
                case ACTION_REPORT_LOCATION:
                    // 汇报定期报告
                    reportPositions();
                    break;
                default:
                    start();
                    break;
            }
        } else {
            log("onStartCommand by null intent.");
            start();
        }
        return START_STICKY;
    }

    /**
     * 清除客户端连接信息
     */
    private void clearClientInfo() {
//        PreferenceHelper.save(DEVICE_ID, "");
//        PreferenceHelper.save(HOST_NAME, "");
//        PreferenceHelper.save(HOST_PORT, "0");
//        PreferenceHelper.save(TOPIC, "");
    }

    // Reads whether or not the service has been started from the preferences
    private boolean wasStarted() {
        String ret = PreferenceHelper.get(PREF_STARTED, "");
        return Utils.isEmpty(ret) ? false : Boolean.valueOf(ret);
    }

    // Sets whether or not the services has been started in the preferences.
    private void setStarted(boolean started) {
        PreferenceHelper.save(PREF_STARTED, String.valueOf(started));
        mStarted = started;
    }

    private void start() {
        log("Starting service...");
        // Do nothing, if the service is already running.
        if (mStarted) {
            log("Attempt to start service that is already active");
            return;
        }

        getRemoteParameters();
        // Establish an MQTT connection
        connect();
    }

    private void registerBroadcastReceiver() {
        if (null == mConnectivityChanged) {
            mConnectivityChanged = new ConnectivityChangedBroadcastReceiver();
            IntentFilter intent = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            intent.addAction(Intent.ACTION_USER_PRESENT);
            // 屏幕关闭时，暂停GPS信息获取
            intent.addAction(Intent.ACTION_SCREEN_OFF);
            intent.addAction(Intent.ACTION_SHUTDOWN);
            intent.addAction(Intent.ACTION_BATTERY_LOW);
            intent.addAction(Intent.ACTION_POWER_DISCONNECTED);
            intent.addAction(Intent.ACTION_POWER_CONNECTED);
            // GPS 控制相关
            intent.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            if (Build.VERSION.SDK_INT >= 19) {
                intent.addAction(LocationManager.MODE_CHANGED_ACTION);
            }

            intent.addAction(Action.FETCHING_MSG_ACTION);
            intent.addAction(Action.LOCAL_SETTING_REFRESH_ACTION);
            intent.addAction(Action.CONNECT_DISCONNECTED_ACTION);
            intent.addAction(Action.GMS_READY_ACTION);
            intent.addAction(Action.GPS_READY_ACTION);
            // update service actions
            intent.addAction(Action.UPDATE_CHECK_ACTION);
            intent.addAction(Action.UPDATE_ACTION);
            intent.addAction(Action.UPDATING_PROGRESS_ACTION);
            intent.addAction(Action.GPS_PERMISSION_CHANGED_ACTION);
            intent.addAction(Action.CONNECT_ACTION);

            intent.setPriority(PROIORITY_NONE);
            registerReceiver(mConnectivityChanged, intent, Permission.FETCH_MESSAGE, null);
        }
    }

    private void stop() {
        log("Stoping service...");
        // Do nothing, if the service is not running.
        if (!mStarted) {
            log("Attempt to stop service not active.");
            return;
        }

        // Save stopped state in the preferences
        setStarted(false);

        // Any existing reconnect timers should be removed, since we explicitly stopping the service.
        try {
            cancelReconnect();
        } catch (Exception ignored) {
        }

        // Destroy the MQTT connection if there is one
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

    private boolean isConnecting = false;

    //
    private void connect() {
        if (isConnecting) {
            log("Attempt to connect a connection operation is still in progress.");
            return;
        }
        if (null != mConnection && mConnection.isConnected()) {
            log("Attempt to connect a connection is already connected.");
            return;
        }
        isConnecting = true;
        log("Connecting...");
        // fetch the device ID from the preferences.
        String deviceID = PreferenceHelper.get(DEVICE_ID, "");
        String hostName = PreferenceHelper.get(HOST_NAME, "");
        String port = PreferenceHelper.get(HOST_PORT, "");
        int hostPort = Utils.isEmpty(port) ? MQTT_BROKER_PORT_NUM : Integer.valueOf(port);
        String topic = PreferenceHelper.get(TOPIC, "");
        // Create a new connection only if the device id is not NULL
        if (Utils.isEmpty(deviceID)) {
            log("Device id not found.");
            isConnecting = false;
        } else if (Utils.isEmpty(hostName)) {
            log("Host name not found.");
            isConnecting = false;
        } else if (Utils.isEmpty(topic)) {
            log("Topic not found.");
            isConnecting = false;
        } else {
            try {
                mConnection = new MQTTConnectionPaho(hostName, hostPort, topic, deviceID);
            } catch (MqttException e) {
                isConnecting = false;
                // Schedule a reconnect, if we failed to connect
                log("MqttException: " + e);
                if (isNetworkAvailable()) {
                    scheduleReconnect();
                }
            }
            setStarted(true);
        }
    }

    private void keepAlive() {
        try {
            // Send a keep alive, if there is a connection.
            if (mStarted && mConnection != null) {
                mConnection.sendKeepAlive();
            }
        } catch (MqttException e) {
            log("MqttException: " + (e.getMessage() != null ? e.getMessage() : "NULL"), e);
            if (null != mConnection) {
                mConnection.disconnect();
                mConnection = null;
            }
            cancelReconnect();
        }
    }

    // Schedule application level keep-alives using the AlarmManager
    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, NotificationService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pi);
    }

    // Remove all scheduled keep alives
    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, NotificationService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    // We schedule a reconnect based on the starttime of the service
    public void scheduleReconnect() {
        // the last keep-alive interval
        String inte = PreferenceHelper.get(PREF_RETRY, "");
        long interval = Utils.isEmpty(inte) ? INITIAL_RETRY_INTERVAL : Long.valueOf(inte);
        //mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);

        // Calculate the elapsed time since the start
        long now = System.currentTimeMillis();
        long elapsed = now - mStartTime;

        // Set an appropriate interval based on the elapsed time since start
        if (elapsed < interval) {
            interval = Math.min(interval, MAXIMUM_RETRY_INTERVAL);
        } else {
            interval += INITIAL_RETRY_INTERVAL;
            if (interval > MAXIMUM_RETRY_INTERVAL) {
                // 大于最大时间长时不再增长
                interval = MAXIMUM_RETRY_INTERVAL;
            }
        }

        //log(String.format("interval: %d, elapsed %d", interval, elapsed));

        log("Rescheduling connection in " + interval + "ms.");

        // Save the new internval
        PreferenceHelper.save(PREF_RETRY, String.valueOf(interval));

        // Schedule a reconnect using the alarm manager.
        Intent i = new Intent();
        i.setClass(this, NotificationService.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
    }

    // Remove the scheduled reconnect
    public void cancelReconnect() {
        Intent i = new Intent();
        i.setClass(this, NotificationService.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    private void reconnectIfNecessary() {
        if (mStarted && mConnection == null) {
            log("Reconnecting...");
            connect();
        }
    }

    private boolean mNotificationEnable = true, mNotificationSound = true, mNotificationVibrate = true, mNotificationLights = true;

    private synchronized void refreshLocalSetting() {
//        Realm realm = null;
//        try {
//            realm = RealmConfig.getInstance().getRealm();
//            SettingImpl impl = new SettingImpl(realm);
//            String value = impl.select(R.integer.setting_notification_enable, "true");
//            mNotificationEnable = Boolean.valueOf(value);
//
//            value = impl.select(R.integer.setting_notification_sound, "true");
//            mNotificationSound = Boolean.valueOf(value);
//
//            value = impl.select(R.integer.setting_notification_vibrate, "true");
//            mNotificationVibrate = Boolean.valueOf(value);
//
//            value = impl.select(R.integer.setting_notification_light, "true");
//            mNotificationLights = Boolean.valueOf(value);
//
//            //log(format("enable: %s, sound: %s, vibrate: %s, lights: %s", mNotificationEnable, mNotificationSound, mNotificationVibrate, mNotificationLights));
//        } finally {
//            if (null != realm)
//                realm.close();
//        }
    }

    /**
     * 有消息到达时发送提醒
     */
    private void showNotification(String title, String text) {
//        if (mNotificationEnable) {
//            Intent intent = new Intent(this, LxbgMainActivity.class);
//            Notification notification = getNotification(title, text, intent);
//            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
//        }
    }

    /**
     * 设置当通知点击后打开的activity的intent
     */
//    private Intent getActionedIntent(String classFullName, String params) {
//        // 设置点击之后启动的 activity
//        Intent intent = new Intent(this, LxbgMainActivity.class);
//        //intent.putExtra(LxbgContainerActivity.REQUEST_CLASS, classFullName);
//        //intent.putExtra(LxbgContainerActivity.REQUEST_PARAMS, params);
//        return intent;
//    }
//    private PendingIntent getMyIntent(Intent intent) {
//        PendingIntent pIntent = PendingIntent.getActivity(this, mNotificationRequestCode, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
//        mNotificationRequestCode++;
//        return pIntent;
//    }
//
//    private static final long[] _vibrate = {0, 100, 200, 100};
//
//    private Notification getNotification(String title, String text, Intent intent) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this);
//        int client = LxbgApp.getInstance().getClientType();
//        builder.setSmallIcon(client == ClientType.A_CUSTOMER ? R.mipmap.app_icon_customer : R.mipmap.app_icon_mechanic).setAutoCancel(true);
//        int defaults = 0;
//        if (mNotificationSound) {
//            defaults |= Notification.DEFAULT_SOUND;
//        }
//        if (mNotificationVibrate) {
//            builder.setVibrate(_vibrate);
//        }
//        // 显示灯光
//        if (mNotificationLights) {
//            builder.setLights(Color.BLUE, 1000, 1000);
//        }
//        builder.setDefaults(defaults).setContentTitle(title).setContentText(text).setContentIntent(getMyIntent(intent));
//
//        return builder.build();
//    }

    // This receiver listeners for network changes and updates the MQTT
    // connection accordingly
    private class ConnectivityChangedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    // Is there connectivity?
                    boolean hasConnectivity = isNetworkAvailable();

                    setNetworkAvailable(hasConnectivity);

                    log("Connectivity changed: connected = " + hasConnectivity);
                    // 发送通知到前台以便提示用户网络不可用
                    sendBroadcast(new Intent(Action.NETWORK_CHANGED_ACTION));

                    if (hasConnectivity) {
                        reconnectIfNecessary();
                    } else if (mConnection != null) {
                        // if there no connectivity, make sure MQTT connection is destroyed
                        mConnection.disconnect();
                        cancelReconnect();
                        mConnection = null;
                    }
                    // 网络环境改变时，检测卫星服务状况
                    checkLocationProviderStatus();
                    if (hasConnectivity) {
                        // 每次网络连接成功之后都尝试变更一下api url地址
                        App.getInstance().resetApiUrl();
                        // 网络掉线重连之后需要访问一下服务器上的信息
                        getRemoteParameters();
                    }
                    break;

                case Intent.ACTION_SHUTDOWN:
                    saveNewestPosition(true, Alarm.Shutdown);
                    log("Device shutdown: " + action);
                    // 设备正在关闭，此时也需要关闭服务
                    actionStop(context);
                    break;

                case Intent.ACTION_USER_PRESENT:
                    log("Broadcast action: " + action);
                    // 用户唤醒了设备，技工端上报新的x，y
                    startLocationService();
                    break;

                case Intent.ACTION_BATTERY_LOW:
                    saveNewestPosition(true, Alarm.BatteryLow);
                    log("Battery low warning.");
                    break;

                case Intent.ACTION_POWER_CONNECTED:
                    saveNewestPosition(true, Alarm.ChargingOn);
                    log("Device power connected");
                    break;

                case Intent.ACTION_POWER_DISCONNECTED:
                    saveNewestPosition(true, Alarm.ChargingOff);
                    log("Device power disconnected, alarm to charging off");
                    break;

                case Intent.ACTION_SCREEN_OFF:
                    log("Screen off, going to sleep");
                    stopLocationService();
                    break;

                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    //log("GPS providers changed " + intent);
                    checkLocationProviderStatus();
                    break;

                case LocationManager.MODE_CHANGED_ACTION:
                    //log("mode changed action " + intent.toString());
                    checkLocationProviderStatus();
                    break;

                case Action.CONNECT_ACTION:
                    getRemoteParameters();
                    break;

                case Action.CONNECT_DISCONNECTED_ACTION:
                    handleConnectDisconnected();
                    break;

                case Action.FETCHING_MSG_ACTION:
                    // 尝试从服务器上获取消息
                    // 通知线程要从服务器上获取新的chat消息
                    mHandler.post(mHandleThreadRunnable);
                    break;

                case Action.LOCAL_SETTING_REFRESH_ACTION:
                    // 重新读取本地配置信息
                    refreshLocalSetting();
                    break;

                case Action.UPDATE_CHECK_ACTION:
                    fetchingUpdate();
                    break;

                case Action.UPDATE_ACTION:
                    update(intent.getStringExtra(EXTRA_URL));
                    break;

                case Action.GMS_READY_ACTION:
                    // Google Play Service可以使用时，启动google play位置服务
                    startGooglePlayLocationService();
                    break;

                case Action.GPS_READY_ACTION:
                    // GPS 服务可以使用了，启动位置服务
                    startLocationService();
                    break;
                case Action.GPS_PERMISSION_CHANGED_ACTION:
                    // GPS 权限更改了
                    boolean flag = intent.getBooleanExtra(TOPIC, false);
                    reportGpsPermissionAlarm(flag);
                    log(format("GPS permission has changed: ", flag));
                    break;
                default:
                    log("Broadcast action: " + action);
                    break;

            }
        }

        private void handleConnectDisconnected() {
            // 收到掉线处理完毕的广播
            int ret = this.getResultCode();

            switch (ret) {
                case Activity.RESULT_CANCELED:
                    // 账号在异地设备上登录了，确保MQTT链接销毁
                    log("Other device login, you will be offline.");
                    if (null != mConnection) {
                        mConnection.disconnect();
                        mConnection = null;
                    }
                    cancelReconnect();
                    // 查看用户是否已经退出
                    //String value = PreferenceHelper.get(R.string.app_login_state, "0");
                    //if ("1".equals(value)) {
                    //    showRemoteDeviceLoginWarning();
                    //}
                    break;
                case Activity.RESULT_OK:            // 正常网络掉线导致连接丢失
                case Activity.RESULT_FIRST_USER:    // MQTT 服务端连接处理失败
                    String text = ret == Activity.RESULT_OK ? "Network connection loss, try to re-connect..." : "Publish server not reachable, schedule to retry...";
                    log(text);
                    if (isNetworkAvailable()) {
                        scheduleReconnect();
                        //reconnectIfNecessary();
                    }
                    break;
            }
        }
    }

    private void logoutAndStop() {
        //PreferenceHelper.save(R.string.app_login_id, "");
        //App.getInstance().setLoginID = "";
        // 清空已登录信息
        //PreferenceHelper.save(R.string.app_login_state, "0");
        // 停止服务
        stop();
    }

    // Check if we are online
    private boolean isNetworkAvailable() {
        return App.getInstance().isNetworkAvailable();
    }

    private void getRemoteParameters() {
        // 设备唯一的号码是否存在
        String device = PreferenceHelper.get(R.string.preference_tag_app_unique_code);
        if (Utils.isEmpty(device))
            return;

        // get parameter只需要提供device id即可
        Account account = new Account();
        account.device = App.getInstance().getDeviceId();

        new SimpleHttpTask().addOnTaskExecuteListener(new OnTaskExecuteListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void doneInBackground(JsonResult result) {
            }

            @Override
            public void onComplete(JsonResult result) {
                // get parameter 只会返回mqtt地址(md5)，sim(data)，session

                if (null != result && result.State == 0 && !Utils.isEmpty(result.Data)) {
                    Account resp = App.getInstance().Gson().fromJson(result.Data, Account.class);
                    // get parameter会返回本device id绑定的sim号码
                    PreferenceHelper.save(DEVICE_ID, resp.data);
                    PreferenceHelper.save(HOST_NAME, resp.md5);
                    PreferenceHelper.save(TOPIC, resp.data);
                    // 账户名
                    PreferenceHelper.save(R.string.preference_tag_app_account_name, resp.name);
                    // 账户所属区域
                    PreferenceHelper.save(R.string.preference_tag_app_account_belong, resp.device);
                    // 保存当前用户的所处区域
                    userBelong = resp.device;
                    // 保存更新信息
                    App.getInstance().setLastAccount(resp.data, resp.session);

                }
                // 联网成功之后发送未发送的数据
                //reportPositions();
                connect();
            }
        }).exec(Api.ApiUrl(), Api.GetParameter(account));
    }

    public static String getThrowableString(Throwable ex) {
        if (null == ex) return "null throwable object.";

        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();
        return result;
    }

    private Runnable mHandleThreadRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    /**
     * 用Paho实现MQTT通讯
     */
    private class MQTTConnectionPaho implements MqttCallback, IMqttActionListener {

        private MqttAsyncClient _client;
        private String topic;

        private boolean connected = false;

        /**
         * 是否已连接成功
         */
        public boolean isConnected() {
            return connected;
        }

        public MQTTConnectionPaho(String brokerHostName, int brokerHostPort, String initTopic, String deviceId) throws MqttException {
            this.topic = initTopic;
            // Create connection spec
            // ex. tcp://localhost:1883 or ssl://localhost:8883
            String mqttConnSpec = "tcp://" + brokerHostName + ":" + brokerHostPort;
            //log("try to create client " + mqttConnSpec);
            // Create the client and connect
            _client = new MqttAsyncClient(mqttConnSpec, deviceId, new MemoryPersistence());

            // connect with options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(MQTT_CONNECT_TIMEOUT);
            // mqtt receive offline message
            options.setCleanSession(MQTT_CLEAN_SESSION);
            options.setKeepAliveInterval(MQTT_KEEP_ALIVE);
            _client.setCallback(MQTTConnectionPaho.this);
            log("Connect to " + mqttConnSpec + ", deviceId: " + deviceId);
            _client.connect(options, null, MQTTConnectionPaho.this);
        }

        // Disconnect
        public void disconnect() {
            try {
                stopKeepAlives();
                _client.disconnect();
                connected = false;
            } catch (MqttException e) {
                log("MqttException " + (e.getMessage() != null ? e.getMessage() : " NULL"), e);
            }
        }

        private void tryFetchingMsgs(boolean direct) {
            if (direct) {
                Intent intent = new Intent(Action.FETCHING_MSG_ACTION);
                sendBroadcast(intent);
            } else {
                // 10秒后
                mHandler.postDelayed(mHandleThreadRunnable, 10000);
            }
        }

        /*
         * Send a request to the message broker to be sent messages published
         * with the specified topic name. Wildcards are allowed.
         */
        private void subscribeToTopic(String topicName) throws MqttException {

            if ((_client == null) || (!_client.isConnected())) {
                // quick sanity check - don't try and subscribe if we don't have a connection
                log("No connection to subscribe to \"" + topicName + "\"");
                connected = false;
            } else {
                String[] topics = {topicName};
                _client.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
            }
        }

        @SuppressWarnings("unused")
        private void unsubscribeTopic(String topicName) throws MqttException {
            if ((_client == null) || (!_client.isConnected())) {
                // quick sanity check - don't try and subscribe if we don't have a connection
                log("No connection to unsubscribe to \"" + topicName + "\"");
                connected = false;
            } else {
                String[] topics = {topicName};
                _client.unsubscribe(topics);
            }
        }

        /*
         * Sends a message to the message broker, requesting that it be published to the specified topic.
         */
        private void publishToTopic(String topicName, String message) throws MqttException {
            if ((_client == null) || (!_client.isConnected())) {
                // quick sanity check - don't try and publish if we don't have a connection
                log("No connection to public to \"" + topicName + "\" with message \"" + message + "\", try to reconnecting...");
                connected = false;
                // 尝试重连
                handleFail(Activity.RESULT_OK);
            } else {
                _client.publish(topicName, message.getBytes(), MQTT_QUALITY_OF_SERVICE, MQTT_RETAINED_PUBLISH);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            connected = false;
            log("Connection lost: " + cause.getLocalizedMessage());
            // 连接断开的时候，如果当前网络没有断开则不需要重新发起连接
            if (!isNetworkAvailable()) {
                handleFail(Activity.RESULT_OK);
            }
        }

        private void handleFail(int failReason) {
            connected = false;
            stopKeepAlives();
            // null itself
            mConnection = null;
            Intent intent = new Intent(Action.CONNECT_DISCONNECTED_ACTION);
            sendOrderedBroadcast(intent, Permission.FETCH_MESSAGE, null, null, failReason, null, null);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // 收到新的推送信息，从服务器上下载新收到的消息并发送本地广播通知
            //new RetrieveMessage().exec();
            // Show a notification
            String s = new String(message.getPayload());
            log("Got message: " + s);
            tryFetchingMsgs(true);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            log("deliveryComplete ");
        }

        public void sendKeepAlive() throws MqttException {
            log("Sending keep alive");
            // publish to a keep-alive topic
            publishToTopic("keepalive", PreferenceHelper.get(TOPIC, ""));
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            isConnecting = false;
            try {
                // Subscribe to an initial topic, which is combination of client ID and device ID.
                subscribeToTopic(topic);
                log("Connection established on topic " + topic);
                tryFetchingMsgs(false);

                // Save start time
                mStartTime = System.currentTimeMillis();

                // Star the keep-alives
                startKeepAlives();
                connected = true;

                // Clear the retry interval
                //PreferenceHelper.save(PREF_RETRY, String.valueOf(0));

            } catch (MqttException e) {
                e.printStackTrace();
                log("subscribe topic failed " + e.getMessage() + ", schedule reconnect...");
                // 链接失败时需要发起重新连接的消息
                handleFail(Activity.RESULT_FIRST_USER);
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            log("Connecting failed, schedule reconnect...");
            isConnecting = false;
            // 重新连接
            handleFail(Activity.RESULT_FIRST_USER);
        }
    }
}

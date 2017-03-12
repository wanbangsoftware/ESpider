package com.hlk.wbs.espider.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.helpers.StringHelper;

/**
 * 作者：Hsiang Leekwok on 2015/09/15 15:35<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public abstract class BaseService extends Service {
    /**
     * 允许控制MQTT服务的permission
     */
    public static final String PERMISSION_SERVICE_CONTROL = Action.BASE_ACTION + ".permission.MQTT_CONTROL";
    /**
     * 运行接收MQTT消息的permission
     */
    public static final String PERMISSION_MQTT_MSG = Action.BASE_ACTION + ".permission.MQTT_MESSAGE";
    /**
     * Mqtt服务包名
     */
    public static final String MQTT_SERVICE = "com.xlg.android.mqttservice";
    /**
     * 客户端标记
     */
    public static final String DEVICE_ID = "__device_id_";
    /**
     * Mqtt服务地址
     */
    public static final String HOST_NAME = "__host_name_";
    /**
     * Mqtt服务端口
     */
    public static final String HOST_PORT = "__host_port_";
    /**
     * Mqtt订阅主题
     */
    public static final String TOPIC = "__topic_";
    /**
     * 标识收到的推送内容，byte[]数组
     */
    public static final String PUBLISHED_DATA = "__publish_data_";
    /**
     * 标识收到的推送是否为保留的消息
     */
    public static final String PUBLISHED_RETAINED = "__publish_retained";
    /**
     * 启动服务时广播中的控制字段名
     */
    public static final String EXTRA_DATA = "__extra_data_";


    /**
     * 聊天信息广播中标记聊天内容的Extra
     */
    public static final String CHAT_BODY = "body";
    /**
     * 聊天信息广播中标记聊天信息是否已处理过的Extra
     */
    public static final String CHAT_HANDLED = "handled";
    /**
     * 消息未经处理
     */
    public static final int HANDLED_NONE = 0;
    /**
     * 消息已经过未读标记处理
     */
    public static final int HANDLED_FLAGED = 1;
    /**
     * 消息已读
     */
    public static final int HANDLED_HAS_READ = 2;
    /**
     * 后台Service处理收到消息的优先级：最低
     */
    public static final int PROIORITY_NONE = 2;
    /**
     * 前台处理收到消息的优先级：只做未读标记，消息不会被设为已读
     */
    public static final int PROIORITY_FLAG = PROIORITY_NONE + 10;
    /**
     * 前台处理收到消息的优先级：消息可以被设为已读
     */
    public static final int PROIORITY_READ = PROIORITY_FLAG + 10;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 打开debug记录
        try {
            mDebugLog = new DebugLog();
            LogHelper.log("BaseService", "Opened log at: " + mDebugLog.getPath());
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mDebugLog != null)
                mDebugLog.close();
        } catch (Exception ignore) {
        }
        super.onDestroy();
    }

    private DebugLog mDebugLog;

    // log helper function
    protected void log(String message) {
        log(message, null);
    }

    protected void log(String message, Throwable e) {
        if (e != null) {
            LogHelper.log(this.getClass().getSimpleName(), message, e);
        } else {
            LogHelper.log(this.getClass().getSimpleName(), message);
        }
        if (null != mDebugLog) {
            try {
                mDebugLog.println(message + (null == e ? "" : ("\r\n" + NotificationService.getThrowableString(e))));
            } catch (Exception ignore) {
            }
        }
    }

    protected String format(String fmt, Object... args) {
        return StringHelper.format(fmt, args);
    }
}

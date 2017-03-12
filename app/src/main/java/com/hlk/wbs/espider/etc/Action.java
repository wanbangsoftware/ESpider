package com.hlk.wbs.espider.etc;

import com.hlk.wbs.espider.BuildConfig;

/**
 * 一些定义的Action<br />
 * 作者：Hsiang Leekwok on 2015/09/15 21:25<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class Action {
    /**
     * 包名作前缀
     */
    public static final String BASE_ACTION = BuildConfig.APPLICATION_ID;
    /**
     * 启动在线更新服务
     */
    public static final String UPDATE_START_ACTION = BASE_ACTION + ".intent.UPDATE_START";
    /**
     * 停止在线更新服务
     */
    public static final String UPDATE_STOP_ACTION = BASE_ACTION + ".intent.UPDATE_STOP";
    /**
     * 通知后台服务尝试更新app
     */
    public static final String UPDATE_CHECK_ACTION = BASE_ACTION + ".intent.UPDATE_CHECK";
    /**
     * 更新失败
     */
    public static final String UPDATE_FAIL_ACTION = BASE_ACTION + ".intent.UPDATE_FAIL";
    /**
     * 通知前台app有可用的更新
     */
    public static final String UPDATABLE_ACTION = BASE_ACTION + ".intent.UPDATABLE";
    /**
     * 通知后台服务开始更新app
     */
    public static final String UPDATE_ACTION = BASE_ACTION + ".intent.UPDATE";
    /**
     * 后台更新app的进度通知
     */
    public static final String UPDATING_PROGRESS_ACTION = BASE_ACTION + ".intent.UPDATING";
    /**
     * 后台服务下载完毕app，可以马上安装
     */
    public static final String UPDATED_ACTION = BASE_ACTION + ".intent.UPDATED";
    /**
     * 网络状态改变事件通知
     */
    public static final String NETWORK_CHANGED_ACTION = BASE_ACTION + ".intent.NETWORK_CHANGED";
    /**
     * 接收到推送消息
     */
    public static final String PUBLISH_ARRIVED_ACTION = BASE_ACTION + ".intent.PUBLISH_ARRIVED";
    /**
     * 从服务器上获取新的消息记录
     */
    public static final String FETCHING_MSG_ACTION = BASE_ACTION + ".intent.FETCHING_MSG";
    /**
     * 远程服务已关闭的广播
     */
    public static final String SERVICE_DOWN_ACTION = BASE_ACTION + ".intent.SERVICE_DOWN";
    /**
     * 发起Mqtt连接的广播
     */
    public static final String CONNECT_ACTION = BASE_ACTION + ".intent.CONNECT";
    /**
     * Mqtt链接断开的广播
     */
    public static final String CONNECT_DISCONNECTED_ACTION = BASE_ACTION + ".intent.CONNECT_DISCONNECTED";
    /**
     * 本地配置文件更改
     */
    public static final String LOCAL_SETTING_REFRESH_ACTION = BASE_ACTION + ".intent.LOCAL_SETTING";
    /**
     * Google Play 服务可以正常使用了
     */
    public static final String GMS_READY_ACTION = BASE_ACTION + ".intent.GOOGLE_PLAY_READY";
    /**
     * GPS 服务可以正常使用了
     */
    public static final String GPS_READY_ACTION = BASE_ACTION + ".intent.GPS_READY";
    /**
     * 本地缓存数据刷新的action
     */
    public static final String TEMPORARY_DATA_REFRESHED_ACTION = BASE_ACTION + ".intent.TEMPORARY_DATA_REFRESHED";
    /**
     * GPS访问权限更改了
     */
    public static final String GPS_PERMISSION_CHANGED_ACTION = BASE_ACTION + ".intent.GPS_PERMISSION_CHANGED";
    /**
     * 账户需要绑定
     */
    public static final String ACCOUNT_NEED_BIND_ACTION = BASE_ACTION + ".intent.ACCOUNT_NEED_BIND";
    /**
     * 本地缓存用户信息已更改
     */
    public static final String ACCOUNT_CHANGED_ACTION = BASE_ACTION + ".intent.ACCOUNT_CHANGED";
}

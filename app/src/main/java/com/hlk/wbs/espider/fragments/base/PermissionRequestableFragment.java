package com.hlk.wbs.espider.fragments.base;

import android.support.v4.app.Fragment;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/03 23:50 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class PermissionRequestableFragment extends Fragment {

    // 获取Google Play Service的请求代码
    public static final int REQUEST_GOOGLE_PLAY_SERVICE = 20000;
    /**
     * 请求打开GPS服务
     */
    public static final int REQUEST_GPS_PROVIDER = 20001;
    // Android 6.0 + 的 permission grant 操作的需求值
    /**
     * app运行时检测基本的权限列表
     */
    public static final int GRANT_BASE = 0x50;
    /**
     * app运行时请求camera的权限
     */
    public static final int GRANT_CAMERA = GRANT_BASE + 1;
    /**
     * app运行时向用户请求位置权限
     */
    public static final int GRANT_LOCATION = GRANT_BASE + 2;
    /**
     * 请求录音设备权限
     */
    public static final int GRANT_RECORD_AUDIO = GRANT_BASE + 3;
    /**
     * 请求拨打电话的权限
     */
    public static final int GRANT_PHONE_CALL = GRANT_BASE + 4;
    /**
     * 请求接收SMS的权限
     */
    public static final int GRANT_SMS = GRANT_BASE + 5;
    /**
     * 请求读取存储设备的权限
     */
    public static final int GRANT_STORAGE = GRANT_BASE + 6;
}

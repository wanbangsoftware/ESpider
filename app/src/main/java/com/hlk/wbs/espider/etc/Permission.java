package com.hlk.wbs.espider.etc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hlk.wbs.espider.applications.App;
import com.litesuits.orm.db.annotation.NotNull;

/**
 * <b>功能</b>：自定义权限列表<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 18:41 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Permission {

    /**
     * 接收后台推送消息
     */
    public static final String FETCH_MESSAGE = Action.BASE_ACTION + ".permission.FETCH_MESSAGE";

    /**
     * 检测是否有相应的权限
     *
     * @param context    当前app上下文
     * @param permission 权限
     */
    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int grant = ContextCompat.checkSelfPermission(context, permission);
            boolean granted = grant == PackageManager.PERMISSION_GRANTED;
            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                App.getInstance().setGpsPermissionChanged(granted);
            }
            return granted;
        }
        return true;
    }

    /**
     * 请求权限
     *
     * @param permission  权限
     * @param requestCode 请求代码
     */
    public static void grantPermission(@NonNull Activity activity, String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermission(activity, permission)) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        }
    }

}

package com.hlk.wbs.espider.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.services.NotificationService;

/**
 * <b>功能</b>：一些必要的控制保证后台服务正常运行不被kill<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 18:44 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SHUTDOWN)) {
                // 设备关机时，停止后台服务
                NotificationService.actionStop(context);
            } else {
                // 无论如何启动后台服务
                NotificationService.actionStart(context);
            }
        }
    }
}

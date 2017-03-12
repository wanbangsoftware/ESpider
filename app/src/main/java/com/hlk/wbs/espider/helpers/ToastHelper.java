package com.hlk.wbs.espider.helpers;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hlk.wbs.espider.lib.IconTextView;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;

import java.util.Date;

/**
 * 提供静态显示Toast消息的Helper
 * Created by Hsiang Leekwok on 2015/07/17.
 */
public class ToastHelper {

    private static long lastToasted = 0, toastInterval = 1000;

    public static void showMsg(int text) {
        showMsg(App.getInstance().getString(text));
    }

    public static void showMsg(int text, int icon) {
        showMsg(App.getInstance().getString(text), App.getInstance().getString(icon));
    }

    public static void showMsg(String msg) {
        showMsg(msg, null);
    }

    public static void showMsg(String msg, String icon) {
        showMsg(msg, icon, null);
    }

    public static void showMsg(String msg, String icon, Handler handler) {

        Date d = new Date();
        if ((d.getTime() - lastToasted) >= toastInterval) {
            lastToasted = d.getTime();
            if (null == handler) {
                toastInThread(msg, icon);
            } else {
                toastInHandle(handler, msg, icon);
            }
        }
    }

    /**
     * 在线程中显示toast消息
     */
    private static void toastInThread(final String msg, final String icon) {

        new Thread() {

            @Override
            public void run() {
                // Toast 显示需要出现在一个线程的消息队列中
                Looper.prepare();
                toast(msg, icon);
                Looper.loop();
            }
        }.start();
    }

    /**
     * 在Handler中显示toast消息
     */
    private static void toastInHandle(Handler handler, final String msg, final String icon) {

        handler.post(new Runnable() {

            @Override
            public void run() {
                toast(msg, icon);
            }
        });
    }

    private static void toast(String msg, String icon) {
        View view = View.inflate(App.getInstance(), R.layout.layout_custom_toast, null);
        TextView text = (TextView) view.findViewById(R.id.ui_custom_toast_text);
        IconTextView awesome = (IconTextView) view.findViewById(R.id.ui_custom_toast_icon);
        text.setText(msg);
        awesome.setText(icon);
        awesome.setVisibility(null == icon ? View.GONE : View.VISIBLE);
        Toast toast = new Toast(App.getInstance());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }
}

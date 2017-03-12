package com.hlk.wbs.espider.activities.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.ContainerActivity;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.fragments.BindAccountFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.services.UpdateService;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能</b>：Activity的基类<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 18:58 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 当前app类型
     */
    protected int client = App.getInstance().client;

    protected Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().addActivity(this);
    }

    @Override
    public void onDestroy() {
        App.getInstance().removeActivity(this);
        super.onDestroy();
    }

    /**
     * 设置状态栏透明并且界面延伸到状态栏后面
     */
    public void transparentStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
    }

    private boolean mIsFullScreen = false;

    /**
     * 在全屏和非全屏窗口之间切换
     */
    public void toggleFullScreen() {
        Window w = getWindow();
        if (mIsFullScreen) {
            w.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mIsFullScreen = false;
        } else {
            w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            w.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            mIsFullScreen = true;
        }
    }

    /**
     * 设置全屏显示窗口
     */
    public void setFullScreen(boolean toFullScreen) {
        mIsFullScreen = !toFullScreen;
        toggleFullScreen();
    }

    /**
     * 查看是否为全屏状态
     */
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    /**
     * 格式化字符串
     */
    public String format(String format, Object... args) {
        return StringHelper.format(format, args);
    }

    public void log(String string) {
        LogHelper.log(BaseActivity.this.getClass().getSimpleName(), string);
    }

    /**
     * 启动容器Activity
     *
     * @param fragmentFullClassName 需要在容器中显示的Fragment的class全名
     * @param params                附加参数
     */
    public void openActivity(String fragmentFullClassName, String params) {
        openActivity(fragmentFullClassName, params, BaseFragment.ACTIVITY_RESULT_REQUEST);
    }

    /**
     * 启动容器Activity
     *
     * @param fragmentFullClassName 需要在容器中显示的Fragment的 class全名
     * @param params                附加参数
     * @param requestCode           区别码
     */
    public void openActivity(String fragmentFullClassName, String params, int requestCode) {
        Intent intent = new Intent(this, ContainerActivity.class);
        Bundle b = new Bundle();
        b.putInt(ContainerActivity.REQUEST_CODE, requestCode);
        b.putString(ContainerActivity.REQUEST_CLASS, fragmentFullClassName);
        b.putString(ContainerActivity.REQUEST_PARAMS, params);
        intent.putExtra(ContainerActivity.EXTRA_BUNDLE, b);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 检测是否有相应的权限
     *
     * @param permission 权限
     */
    protected boolean hasPermission(String permission) {
        return Permission.hasPermission(this, permission);
    }

    /**
     * 请求权限
     *
     * @param permission  权限
     * @param requestCode 请求代码
     */
    protected void grantPermission(String permission, int requestCode) {
        Permission.grantPermission(this, permission, requestCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcast();
    }

    @Override
    public void onPause() {
        // pause 的时候取消广播监听
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void registerBroadcast() {
        IntentFilter intent = new IntentFilter(Action.ACCOUNT_NEED_BIND_ACTION);
        // resume 的时候注册广播监听
        registerReceiver(mBroadcastReceiver, intent);
    }

    /**
     * 接受后台发过来的错误通知
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                if (!Utils.isEmpty(action)) {
                    switch (action) {
                        case Action.ACCOUNT_NEED_BIND_ACTION:
                            int state = intent.getIntExtra(UpdateService.EXTRA_TITLE, -1);
                            handleAccountBindRequest(state);
                            break;
                    }
                }
            }
        }
    };

    private void handleAccountBindRequest(int state) {
        if (!App.getInstance().bindAccountWarning) {
            App.getInstance().bindAccountWarning = true;
            String text = StringHelper.getString(state == Account.BIND ? R.string.ui_warning_account_need_bind : R.string.ui_warning_account_need_re_bind);
            warningDialog(text, false, new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    openActivity(BindAccountFragment.class.getName(), "");
                }
            });
        }
    }

    private void warningDialog(String text, boolean cancelable, SweetAlertDialog.OnSweetClickListener confirm) {
        SweetAlertDialog dialog = new SweetAlertDialog(this)
                .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                .setContentText(text)
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know));
        if (null != confirm) {
            dialog.setConfirmClickListener(confirm);
        }
        dialog.setCancelable(cancelable);
        dialog.show();
    }
}

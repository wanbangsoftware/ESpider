package com.hlk.wbs.espider.applications;

import android.app.Activity;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.baidu.mapapi.SDKInitializer;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.CrashHandler;
import com.hlk.wbs.espider.etc.PressAgainToExit;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.helpers.ToastHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.services.NotificationService;
import com.hlk.wbs.espider.services.UpdateService;
import com.hlk.wbs.espider.tasks.PeriodReportTask;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 00:55 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class App extends OrmApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        getLastAccount();
        startService();
        initializeCrashHandler();
    }

    private void initializeCrashHandler() {
        //if (!BuildConfig.ISDEBUG) {
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(this);
        // 发送以前没发送的报告(可选)
        crashHandler.sendPreviousReportsToServer();
        //}
    }

    /**
     * 获取全局Application单例实例
     */
    public static App getInstance() {
        return (App) instance;
    }

    /**
     * 初始化百度地图api
     */
    public void initializeBaiduApi() {
        if (shouldInitialize()) {
            SDKInitializer.initialize(App.getInstance());
        }
    }

    /**
     * 启动后台服务
     */
    public void startService() {
        NotificationService.actionStart(this);
    }

    /**
     * 停止后台服务
     */
    public void stopService() {
        NotificationService.actionStop(this);
    }

    // 再按一次退出
    private PressAgainToExit mPressAgainToExit = new PressAgainToExit();

    /**
     * 再按一次退出程序。
     */
    public void pressAgainExit() {
        if (mPressAgainToExit.isExit()) {
            clearActivity();
        } else {
            LogHelper.log(TAG, "press again to exit.");
            ToastHelper.showMsg(R.string.ui_warning_press_again_to_exit);
            mPressAgainToExit.doExitInOneSecond();
        }
    }

    private Map<String, Activity> activities = new HashMap<>();

    /**
     * 设置当前Activity
     */
    public void addActivity(Activity activity) {
        String hashCode = Integer.toHexString(activity.hashCode());
        if (!activities.containsKey(hashCode)) {
            activities.put(hashCode, activity);
        }
    }

    /**
     * 检测是否有相同的activity实例存在
     */
    public boolean activityExists(String name) {
        String[] keys = getActivityKeys();
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                String n = activities.get(key).getClass().getSimpleName();
                if (n.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getActivityKeys() {
        int size = activities.size();
        if (size > 0) {
            return activities.keySet().toArray(new String[size]);
        }
        return null;
    }

    // 获取最后添加进来的Activity
    public Activity getActivity() {
        String[] keys = getActivityKeys();

        if (null != keys && keys.length > 0) {
            return activities.get(keys[keys.length - 1]);
        }
        return null;
    }

    private void removeActivity(String hashCode) {
        if (null != hashCode) {
            if (activities.containsKey(hashCode)) {
                Activity a = activities.remove(hashCode);
                if (null != a && !a.isFinishing()) {
                    a.finish();
                }
            }
        }
    }

    public void removeActivity(Activity activity) {
        if (null != activity) {
            String hashCode = Integer.toHexString(activity.hashCode());
            removeActivity(hashCode);
        }
    }

    private void clearActivity() {
        int size = activities.size();
        String[] keys = getActivityKeys();
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                if (null != key) {
                    removeActivity(keys[size - 1]);
                }
                size--;
            }
        }
    }

    protected boolean updatable = true;

    public void checkUpdate() {
        if (updatable) {
            Intent i = new Intent(Action.UPDATE_CHECK_ACTION);
            sendBroadcast(i);
        }
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    private String mqttServer = "";

    public String getMqttServer() {
        if (Utils.isEmpty(mqttServer)) {
            // 获取mqtt服务器地址
            mqttServer = PreferenceHelper.get(R.string.preference_tag_app_service_id, "");
        }
        return mqttServer;
    }

    public void setMqttServer(String address) {
        mqttServer = address;
    }

    private String lastAccount = "", lastSession = "";

    private void fetchLastAccount() {
        if (Utils.isEmpty(lastAccount)) {
            lastAccount = PreferenceHelper.get(R.string.preference_tag_app_last_account, "");
        }
        if (Utils.isEmpty(lastSession)) {
            lastSession = PreferenceHelper.get(R.string.preference_tag_app_service_id, "");
        }
    }

    /**
     * 获取当前登录的账号名
     */
    public String getLastAccount() {
        fetchLastAccount();
        if (!Utils.isEmpty(lastAccount)) {
            initializeLiteOrm(lastAccount);
        }
        return lastAccount;
    }

    /**
     * 获取用户的登录状态
     */
    public String getLastSession() {
        fetchLastAccount();
        return lastSession;
    }

    public void saveAccount(String account, String session) {
        lastAccount = account;
        lastSession = session;
        PreferenceHelper.save(R.string.preference_tag_app_last_account, account);
        PreferenceHelper.save(R.string.preference_tag_app_service_id, session);
        sendBroadcast(new Intent(Action.ACCOUNT_CHANGED_ACTION));
    }

    public void setLastAccount(String account, String session) {
        int bind = Account.NONE;
        fetchLastAccount();
        if (Utils.isEmpty(lastAccount)) {
            // 如果返回的session不为空则直接保存，说明以前绑定过，且卸载过app，现在重新安装了
            if (!Utils.isEmpty(session)) {
                saveAccount(account, session);
            } else {
                // 没有账号信息说明需要绑定账号
                bind = Account.BIND;
            }
        } else if (!lastAccount.equals(account)) {
            // 账号不同则需要重新绑定
            bind = Account.REBIND;
        } else {
            if (Utils.isEmpty(session)) {
                // 服务器返回的session为空
                if (Utils.isEmpty(lastSession)) {
                    bind = Account.BIND;
                } else {
                    // 本地session不为空则需要重新绑定账号
                    bind = Account.REBIND;
                    lastSession = "";
                }
            } else {
                if (Utils.isEmpty(lastSession)) {
                    // 本地session为空则直接保存session
                    lastSession = session;
                } else if (!session.equals(lastSession)) {
                    // 服务器返回的session跟本地的session不一样，需要重新绑定账号
                    bind = Account.REBIND;
                }
            }
        }
        if (bind != Account.NONE) {
            Intent intent = new Intent(Action.ACCOUNT_NEED_BIND_ACTION);
            intent.putExtra(UpdateService.EXTRA_TITLE, bind);
            sendBroadcast(intent);
        }
        initializeLiteOrm(lastAccount);
    }

    private int reportPeriod = 0;

    /**
     * 获取当前汇报时间间隔
     */
    public int getReportPeriod() {
        if (0 == reportPeriod) {
            reportPeriod = Integer.valueOf(PreferenceHelper.get(R.string.preference_tag_app_report_period, String.valueOf(PeriodReportTask.DEFAULT_PERIOD)));
        }
        return reportPeriod;
    }

    /**
     * 更改汇报时间间隔
     */
    public void setReportPeriod(int period) {
        if (period >= 10 && reportPeriod != period) {
            reportPeriod = period;
            PreferenceHelper.save(R.string.preference_tag_app_report_period, String.valueOf(reportPeriod));
        }
    }
    // 一些加载的动画

    /**
     * 加载UI中的动画
     */
    public Animation loadingAnimation() {
        return AnimationUtils.loadAnimation(this, R.anim.ui_animation_loading);
    }

}

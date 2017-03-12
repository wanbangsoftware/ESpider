package com.hlk.wbs.espider.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hlk.hlklib.etc.DeviceUtility;
import com.hlk.wbs.espider.BuildConfig;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.base.GooglePlayReachableActivity;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.CrashHandler;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.fragments.DataPreviewFragment;
import com.hlk.wbs.espider.fragments.MainFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.helpers.SnackbarHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.services.UpdateService;
import com.hlk.wbs.espider.tasks.AsyncExecutableTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends GooglePlayReachableActivity {

    private MainFragment mainFragment;

    private void register() {
        IntentFilter intent = new IntentFilter(Action.UPDATABLE_ACTION);
        intent.addAction(Action.UPDATE_FAIL_ACTION);
        registerReceiver(mUpdateReceiver, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setIcon(R.mipmap.img_app_toolbar_icon);
        }
        register();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SnackbarHelper.show(view, "Replace with your own action", SnackbarHelper.ShowType.Warning);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        mainFragment = new MainFragment();
        setMainFrameLayout(mainFragment);
        // 尝试检测Google服务套件
        checkGooglePlayServices();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkEmulator();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            App.getInstance().pressAgainExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    private void checkEmulator() {
        if (DeviceUtility.isEmulator() && !BuildConfig.DEBUG) {
            warningEmulator();
        } else {
            checkClientId();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkBasePermissions();
                }
            }, 3000);
        }
    }

    /**
     * 检测本地缓存中是否有clientid
     */
    private void checkClientId() {
    }

    /**
     * 警告用户不能在模拟器里运行应用
     */
    private void warningEmulator() {
        CrashHandler.getInstance().report(new IllegalArgumentException("Cannot running this app in any Emulator"));
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                .setContentText(StringHelper.getString(R.string.ui_warning_cannot_run_in_emulator))
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        finish();
                    }
                })
                .show();
    }

    private static final String COLOR_TEXT = "<font color=\"#00a8a8\">%s</font>";

    private void checkBasePermissions() {
        List<String> tmp = new ArrayList<>();
        String txt = "", explain, dot = StringHelper.getString(R.string.ui_base_text_dot);
        // 读取存储设备
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            explain = StringHelper.getString(R.string.ui_permission_access_external_storage);
            txt = format(COLOR_TEXT, explain);
            tmp.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // 位置信息
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            explain = StringHelper.getString(R.string.ui_permission_access_location);
            txt += (Utils.isEmpty(txt) ? "" : dot) + format(COLOR_TEXT, explain);
            tmp.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (tmp.size() > 0) {
            String text = StringHelper.getString(R.string.ui_permission_base, txt);
            final String[] strings = tmp.toArray(new String[tmp.size()]);
            warningBasePermission(text, strings);
        } else {
            checkLocation();
        }
    }

    private void warningBasePermission(String text, final String[] permissions) {
        new SweetAlertDialog(this)
                .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                .setContentText(text)
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, BaseFragment.GRANT_BASE);
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        int granted = PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case BaseFragment.GRANT_BASE:
                if (grantResults.length > 0 && grantResults[0] == granted) {
                    checkLocation();
                }
                break;
            case BaseFragment.GRANT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == granted) {
                    // 读取已保存的设备id
                    getStoredDeviceId();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean locationPermissionCheckable = true, updateCheckable = true;

    /**
     * 如果拥有GPS访问权限则启动GPS服务
     */
    private void checkLocation() {
        if (locationPermissionCheckable) {
            locationPermissionCheckable = false;
            checkLocationPermission();
        }
        checkStoragePermission();
        // 每次启动应用都检测一下是否有更新
        if (updateCheckable) {
            updateCheckable = false;
            App.getInstance().checkUpdate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationPermissionCheckable = true;
    }

    private void warningPermissionDenied() {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                .setContentText(StringHelper.getString(R.string.ui_warning_cannot_run_in_emulator))
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        //finish();
                    }
                })
                .show();
    }

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                switch (action) {
                    case Action.UPDATABLE_ACTION:
                        String url = intent.getStringExtra(UpdateService.EXTRA_URL);
                        String title = intent.getStringExtra(UpdateService.EXTRA_TITLE);
                        String text = intent.getStringExtra(UpdateService.EXTRA_INFO);
                        warningUpdatable(title, text, url);
                        break;
                    case Action.UPDATE_FAIL_ACTION:
                        int reason = intent.getIntExtra(UpdateService.EXTRA_TITLE, -1);
                        if (null != mainFragment) {
                            mainFragment.changeUpdateStatus(getReason(reason));
                        }
                        break;
                }
            }
        }

        private String getReason(int reason) {
            switch (reason) {
                case UpdateService.NO_UPDATE:
                    // 已经是最新版本了
                    return StringHelper.getString(R.string.ui_warning_updating_no_update);
                case UpdateService.ERR_UPDATE:
                case UpdateService.ERR_DATA:
                    return StringHelper.getString(R.string.ui_warning_updating_server_fail);
                default:
                    // 处理失败
                    return StringHelper.getString(R.string.ui_warning_updating_fail);
            }
        }
    };

    private void warningUpdatable(String title, String text, final String url) {
        new SweetAlertDialog(MainActivity.this).setTitleText(title).setContentText(text)
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_update_confirm))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        Intent i = new Intent(Action.UPDATE_ACTION);
                        i.putExtra(UpdateService.EXTRA_URL, url);
                        sendBroadcast(i);
                    }
                })
                .setCancelText(StringHelper.getString(R.string.ui_dialog_button_update_cancel))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        mainFragment.changeUpdateStatus(StringHelper.getString(R.string.ui_warning_updating_user_canceled));
                    }
                })
                .show();
    }

    /**
     * 检测是否有读取存储设备的权限
     */
    private void checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (!hasPermission(permission)) {
            grantPermission(permission, BaseFragment.GRANT_STORAGE);
        } else {
            // 读取设备中保存的设备代码
            getStoredDeviceId();
        }
    }

    private void getStoredDeviceId() {
        new ReadStoriedDeviceId().exec();
    }

    /**
     * 读取本机已经保存了的设备ID
     */
    private class ReadStoriedDeviceId extends AsyncExecutableTask<Void, Void, Void> {

        @Override
        protected Void doInTask(Void... params) {
            String name = App.staticName();
            String path = Utils.getCachePath(Utils.CACHE_DIR);
            path = path + name;
            String uuid = getUuid(path);
            // 如果设备号码不存在或错误则重新生成一个新的设备号码
            if (Utils.isEmpty(uuid) || uuid.length() < 5) {
                uuid = UUID.randomUUID().toString().replaceAll("-", "");
                saveUuid(path, uuid);
            }
            App.getInstance().setUniqueCode(uuid);
            return null;
        }

        /**
         * 读取已经保存了的设备号码
         */
        private String getUuid(String path) {
            File file = new File(path);
            String uuid = null;
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    int c;
                    StringBuilder sb = new StringBuilder();
                    while ((c = fis.read()) != -1) {
                        sb.append((char) c);
                    }
                    fis.close();
                    uuid = sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 读取失败时从本地缓存中获取uuid
            if (Utils.isEmpty(uuid)) {
                uuid = PreferenceHelper.get(R.string.preference_tag_app_unique_code);
            }
            return uuid;
        }

        /**
         * 保存设备号到本地存储目录
         */
        private void saveUuid(String path, String uuid) {
            try {
                FileWriter fw = new FileWriter(path);
                fw.write(uuid);
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void doAfterExecute() {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.ui_menu_action_realm:
                openActivity(DataPreviewFragment.class.getName(), "");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

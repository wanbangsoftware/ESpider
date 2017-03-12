package com.hlk.wbs.espider.activities.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.AppType;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.tx.Location;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 20:33 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class GPSServiceReachableActivity extends ToolbarActivity {

    private int providerCheckTimes = 0;

    /**
     * 检测是否有GPS权限并启动GPS服务
     */
    protected void checkLocationPermission() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            grantPermission(Manifest.permission.ACCESS_FINE_LOCATION, BaseFragment.GRANT_LOCATION);
        } else {
            checkGpsProvider();
        }
    }

    /**
     * 通知后台服务可以启动GPS监听了
     */
    private void startGpsLocation() {
        sendBroadcast(new Intent(Action.GPS_READY_ACTION));
    }

    private void checkGpsProvider() {
        if (providerCheckTimes > 3) return;
        providerCheckTimes++;

        // everdigm 版本才检测GPS服务是否开启
        if (App.getInstance().client == AppType.EVERDIGM) {
            String provider = App.getInstance().isNetworkAvailable() ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (null != locationManager) {
                if (locationManager.isProviderEnabled(provider)) {
                    startGpsLocation();
                } else {
                    if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                        // 网络定位不支持时，启动GPS定位
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            startGpsLocation();
                        } else {
                            warningGpsProvider();
                        }
                    } else {
                        warningGpsProvider();
                    }
                }
            } else {
                warningNoGpsService();
            }
        }
    }

    private void warningGpsProvider() {
        try {
            new SweetAlertDialog(this)
                    .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                    .setContentText(StringHelper.getString(R.string.ui_warning_location_provider_not_enabled))
                    .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), BaseFragment.REQUEST_GPS_PROVIDER);
                        }
                    }).setCancelText(StringHelper.getString(R.string.ui_dialog_button_update_cancel))
                    .show();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 通知用户没有位置服务存在
     */
    private void warningNoGpsService() {
        try {
            new SweetAlertDialog(this)
                    .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                    .setContentText(StringHelper.getString(R.string.ui_warning_location_service_not_exist))
                    .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                    .show();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        int granted = PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case BaseFragment.GRANT_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == granted) {
                    checkGpsProvider();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BaseFragment.REQUEST_GPS_PROVIDER) {
            if (resultCode == RESULT_OK) {
                checkGpsProvider();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

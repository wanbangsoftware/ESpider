package com.hlk.wbs.espider.activities.base;

import android.content.Intent;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.StringHelper;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能</b>：获取Google Play<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 16:50 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class GooglePlayReachableActivity extends GPSServiceReachableActivity {

    /**
     * 检测google play service是否已经安装
     */
    protected void checkGooglePlayServices() {
//        if (client != AppType.EVERDIGM)
//            return;

//        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
//
//        int available = googleApi.isGooglePlayServicesAvailable(this);
//        log(format("Google Play Service available: %d", available));
//        if (available != ConnectionResult.SUCCESS) {
//            /*
//            * Google Play Services is missing or update is required, return code could be
//		    * SUCCESS,
//		    * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
//		    * SERVICE_DISABLED, SERVICE_INVALID.
//		    */
//            if (googleApi.isUserResolvableError(available)) {
//                googleApi.getErrorDialog(this, available, BaseFragment.REQUEST_GOOGLE_PLAY_SERVICE).show();
//            } else {
//                warningAndClose(googleApi.getErrorString(available), true);
//            }
//        } else {
//            onActivityResult(BaseFragment.REQUEST_GOOGLE_PLAY_SERVICE, RESULT_OK, null);
//        }
    }

    private void warningAndClose(String text, final boolean closeable) {
        new SweetAlertDialog(this)
                .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                .setContentText(text)
                .setConfirmText(StringHelper.getString(R.string.ui_dialog_button_yes_i_know))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if (closeable) {
                            finish();
                        }
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BaseFragment.REQUEST_GOOGLE_PLAY_SERVICE) {
            if (resultCode == RESULT_OK) {
                // 通知后台服务马上启动Google服务
                sendBroadcast(new Intent(Action.GMS_READY_ACTION));
            } else {
                warningAndClose(StringHelper.getString(R.string.ui_warning_google_play_service_not_available), false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

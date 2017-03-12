package com.hlk.wbs.espider.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.base.ToolbarActivity;
import com.hlk.wbs.espider.fragments.AboutFragment;
import com.hlk.wbs.espider.fragments.BindAccountFragment;
import com.hlk.wbs.espider.fragments.DataPreviewFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.StringHelper;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 10:43 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class ContainerActivity extends ToolbarActivity {

    public static final String EXTRA_BUNDLE = "_bundle_";
    /**
     * 请求的CODE码
     */
    public static final String REQUEST_CODE = "_request_code_";
    /**
     * 需要打开的fragment的class名称
     */
    public static final String REQUEST_CLASS = "_request_class_";
    /**
     * 请求的参数列表
     */
    public static final String REQUEST_PARAMS = "_request_params_";

    private BaseFragment mFragment = null;

    private String mClass = "", mParams = "";
    // 默认不需要Activity返回
    private int mCode = BaseFragment.ACTIVITY_RESULT_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            initParams(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (null != intent) {
                Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE);
                if (null != bundle) {
                    initParams(bundle);
                }
            }
        }

        // 创建Fragment
        if (null != mClass) {
            if (null == mFragment) {
                mFragment = getFragment();
                if (null != mFragment) {
                    setMainFrameLayout(mFragment);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null == outState)
            outState = new Bundle();
        outState.putString(REQUEST_CLASS, mClass);
        outState.putString(REQUEST_PARAMS, mParams);
        outState.putInt(REQUEST_CODE, mCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null == mFragment && null != mClass)
            mFragment = getFragment();
        if (null != mFragment) {
            setMainFrameLayout(mFragment);
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(StringHelper.getString(R.string.ui_warning_default_title))
                    .setContentText(StringHelper.getString(R.string.ui_warning_no_content_display))
                    .show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//        }
        return super.onKeyDown(keyCode, event);
    }

    private void initParams(Bundle bundle) {
        mClass = bundle.getString(REQUEST_CLASS);
        mParams = bundle.getString(REQUEST_PARAMS);
        mCode = bundle.getInt(REQUEST_CODE);
        // 是否需要Activity返回
        if (mCode != BaseFragment.ACTIVITY_RESULT_NONE) {
            setResult(Activity.RESULT_OK);
        }
    }

    private BaseFragment getFragment() {
        BaseFragment base = null;
        String[] params = null;
        if (null != mParams) {
            params = mParams.split(",", -1);
        }
        if (mClass.equals(AboutFragment.class.getName())) {
            base = new AboutFragment();
        } else if (mClass.equals(BindAccountFragment.class.getName())) {
            base = new BindAccountFragment();
        } else if (mClass.equals(DataPreviewFragment.class.getName())) {
            base = new DataPreviewFragment();
        }
        return base;
    }
}

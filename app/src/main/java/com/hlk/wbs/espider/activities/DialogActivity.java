package com.hlk.wbs.espider.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/04 00:07 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class DialogActivity extends AppCompatActivity {

    public static final String BUNDLE_EXTRA = "_dialog_";
    public static final String DIALOG_TYPE = "_dialog_type_";
    public static final String DIALOG_TITLE = "_dialog_title_";
    public static final String DIALOG_TEXT = "_dialog_text_";

    private int dialogType = SweetAlertDialog.NORMAL_TYPE;
    private String dialogTitle, dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            initializeParameters(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (null != intent) {
                Bundle bundle = intent.getBundleExtra(BUNDLE_EXTRA);
                if (null != bundle) {
                    initializeParameters(bundle);
                }
            }
        }
    }

    private void initializeParameters(Bundle bundle) {
    }
}

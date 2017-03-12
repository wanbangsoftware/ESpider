package com.hlk.wbs.espider.activities.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hlk.wbs.espider.R;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 10:44 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class ToolbarActivity extends BaseActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    /**
     * 显示导航按钮
     */
    public void showNavigationIcon() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    public void showToolbar(boolean shown) {
        mToolbar.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置主 frame
     */
    public void setMainFrameLayout(Fragment fragment) {
        if (!isFinishing()) {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.ui_activity_frame, fragment, fragment.getClass().getName())
                        .commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

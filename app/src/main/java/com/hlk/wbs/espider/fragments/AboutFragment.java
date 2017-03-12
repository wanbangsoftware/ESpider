package com.hlk.wbs.espider.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hlk.wbs.espider.lib.RippleView;
import com.hlk.wbs.espider.lib.RippleView.OnRippleCompleteListener;
import com.hlk.hlklib.inject.Click;
import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.BuildConfig;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.base.ToolbarActivity;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.StringHelper;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 12:32 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class AboutFragment extends BaseFragment {

    @ViewId(R.id.ui_fragment_about_version)
    private TextView version;
    @ViewId(R.id.ui_fragment_about_name)
    private TextView name;
    @ViewId(R.id.ui_fragment_about_container)
    private RippleView container;

    @Override
    protected void destroyView() {

    }

    @Override
    protected void findViews() {
        ViewUtility.bind(this, mView);
        container.setOnRippleCompleteListener(mOnRippleCompleteListener);
    }

    @Override
    protected void getParamsFromBundle(Bundle bundle) {

    }

    @Override
    protected void saveParamsToBundle(Bundle bundle) {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_about;
    }

    @Override
    public void doingInResume() {
        ((ToolbarActivity) Activity()).showToolbar(false);
        Activity().toggleFullScreen();
        String title = format("%s for Everdigm", StringHelper.getString(R.string.app_name));
        name.setText(title);
        String ver = App.getInstance().version();
        String iver = StringHelper.getString(R.string.app_internal_version);
        version.setText(format("%s %s build %s", (BuildConfig.DEBUG ? "debug" : "release"), ver, iver));
    }

    @Click({R.id.ui_fragment_about_container})
    private void viewClick(View view) {

    }


    private OnRippleCompleteListener mOnRippleCompleteListener = new OnRippleCompleteListener() {
        @Override
        public void onComplete(RippleView rippleView) {
            finish();
        }
    };
}

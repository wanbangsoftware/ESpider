package com.hlk.wbs.espider.fragments.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hlk.wbs.espider.lib.IconTextView;
import com.hlk.wbs.espider.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * <b>功能：</b>提供nothing和loading显示的fragment<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 08:59 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public abstract class NothingLoadingFragment extends NetworkStatusCatchableFragment {

    private View mNothingView, mLoadingView;
    private TextView mNothingText, mLoadingText;
    private IconTextView mNothingIcon;
    private MaterialProgressBar mLoadingProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        findView();
        return view;
    }

    private void findView() {
        mNothingView = mView.findViewById(R.id.ui_refreshable_nothing);
        mLoadingView = mView.findViewById(R.id.ui_refreshable_loading);
        if (null != mNothingView) {
            mNothingText = (TextView) mView.findViewById(R.id.ui_base_nothing_text);
            mNothingIcon = (IconTextView) mView.findViewById(R.id.ui_base_nothing_icon);
        }
        if (null != mLoadingView) {
            mLoadingText = (TextView) mView.findViewById(R.id.ui_base_loading_text);
            mLoadingProgress = (MaterialProgressBar) mView.findViewById(R.id.ui_base_loading_progress);
        }
    }

    public void setNothingText(int text) {
        if (null != mNothingText)
            mNothingText.setText(text);
    }

    public void setNothingText(String text) {
        if (null != mNothingText)
            mNothingText.setText(text);
    }

    public void setNothingIcon(int icon) {
        if (null != mNothingIcon)
            mNothingIcon.setText(icon);
    }

    public void setNothingIcon(String icon) {
        if (null != mNothingIcon)
            mNothingIcon.setText(icon);
    }

    public void setLoadingText(int text) {
        if (null != mLoadingText)
            mLoadingText.setText(text);
    }

    public void setLoadingText(String text) {
        if (null != mLoadingText)
            mLoadingText.setText(text);
    }

    public void displayNothing(boolean shown) {
        if (null != mNothingView) {
            mNothingView.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 显示loading进度
     */
    public void displayLoading(int percentage) {
        if (null != mLoadingView) {
            mLoadingProgress.setIndeterminate(false);
            if (mLoadingView.getVisibility() != View.VISIBLE) {
                mLoadingView.setVisibility(View.VISIBLE);
            }
            mLoadingProgress.setProgress(percentage);
        }
    }

    public void displayLoading(boolean shown) {
        if (null != mLoadingView) {
            mLoadingView.setVisibility(shown ? View.VISIBLE : View.GONE);
            mLoadingProgress.setIndeterminate(shown);
        }
    }
}

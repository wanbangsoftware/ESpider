package com.hlk.wbs.espider.holders;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hlk.wbs.espider.activities.base.BaseActivity;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.LogHelper;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 12:06 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private BaseFragment fragment;

    /**
     * 创建ViewHolder
     *
     * @param itemView 该Item的根view
     * @param fragment 主显示fragment
     */
    public BaseViewHolder(View itemView, BaseFragment fragment) {
        super(itemView);
        this.fragment = fragment;
    }

    protected void log(String text) {
        LogHelper.log(this.getClass().getSimpleName(), text);
    }

    protected String format(String format, Object... args) {
        return fragment.format(format, args);
    }

    protected int getColor(int res) {
        return fragment.getColor(res);
    }

    protected BaseFragment Fragment() {
        return fragment;
    }

    protected Handler Handler() {
        return fragment.Handler();
    }

    protected BaseActivity Activity() {
        return fragment.Activity();
    }

    protected void openActivity(String fragmentFullClassName, String params) {
        fragment.openActivity(fragmentFullClassName, params);
    }

    protected void openActivity(String fragmentFullClassName, String params, int requestCode) {
        fragment.openActivity(fragmentFullClassName, params, requestCode);
    }

}

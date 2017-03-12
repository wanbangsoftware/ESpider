package com.hlk.wbs.espider.holders;

import android.view.View;
import android.widget.TextView;

import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.fragments.base.BaseFragment;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 16:29 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class WarningViewHolder extends BaseViewHolder {

    @ViewId(R.id.ui_view_item_warning_text)
    private TextView text;

    /**
     * 创建ViewHolder
     *
     * @param itemView 该Item的根view
     * @param fragment 主显示fragment
     */
    public WarningViewHolder(View itemView, BaseFragment fragment) {
        super(itemView, fragment);
        ViewUtility.bind(this, itemView);
    }

    public void showContent(String string) {
        text.setText(string);
    }
}

package com.hlk.wbs.espider.holders;

import android.view.View;
import android.widget.TextView;

import com.hlk.wbs.espider.lib.RippleView;
import com.hlk.wbs.espider.lib.RippleView.OnRippleCompleteListener;
import com.hlk.hlklib.inject.Click;
import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.fragments.BindAccountFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.models.InputItem;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 12:48 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class ButtonViewHolder extends BaseViewHolder {

    @ViewId(R.id.ui_view_item_button_button)
    private TextView button;
    @ViewId(R.id.ui_view_item_button_container)
    private RippleView container;

    private InputItem item;

    /**
     * 创建ViewHolder
     *
     * @param itemView 该Item的根view
     * @param fragment 主显示fragment
     */
    public ButtonViewHolder(View itemView, BaseFragment fragment) {
        super(itemView, fragment);
        ViewUtility.bind(this, itemView);
    }

    public void showContent(InputItem item) {
        this.item = item;
        showContent();
    }

    private void showContent() {
        button.setText(item.title);
        container.setOnRippleCompleteListener(mOnRippleCompleteListener);
    }

    @Click(R.id.ui_view_item_button_container)
    private void viewClick(View view) {
    }

    private OnRippleCompleteListener mOnRippleCompleteListener = new OnRippleCompleteListener() {
        @Override
        public void onComplete(RippleView rippleView) {
            if (Fragment() instanceof BindAccountFragment) {
                ((BindAccountFragment) Fragment()).verifyInputtedValues();
            }
        }
    };
}

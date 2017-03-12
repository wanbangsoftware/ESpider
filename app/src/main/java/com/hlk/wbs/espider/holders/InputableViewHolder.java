package com.hlk.wbs.espider.holders;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.hlk.wbs.espider.lib.CountdownEditText;
import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.fragments.BindAccountFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.models.InputItem;

/**
 * <b>功能：</b>可提供输入输出的ViewHolder<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 11:27 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class InputableViewHolder extends BaseViewHolder {

    @ViewId(R.id.ui_view_item_inputable_title)
    private TextView title;
    @ViewId(R.id.ui_view_item_inputable_text)
    private CountdownEditText text;

    private InputItem item;

    /**
     * 创建ViewHolder
     *
     * @param itemView 该Item的根view
     * @param fragment 主显示fragment
     */
    public InputableViewHolder(View itemView, BaseFragment fragment) {
        super(itemView, fragment);
        ViewUtility.bind(this, itemView);
    }

    public void showContent(InputItem item) {
        this.item = item;
        showContent();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (Fragment() instanceof BindAccountFragment) {
                switch (item.index) {
                    case 0:
                        ((BindAccountFragment) Fragment()).setAccount(s.toString());
                        break;
                    case 1:
                        ((BindAccountFragment) Fragment()).setPassword(s.toString());
                        break;
                }
            }
        }
    };

    private void showContent() {
        title.setText(item.title);
        text.setHint(item.hint);
        text.setInputType(item.inputType == 1 ?
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PASSWORD) :
                (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        text.setMaximumTextLength(item.max);

        text.addTextChangedListener(mTextWatcher);
    }
}

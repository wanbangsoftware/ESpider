package com.hlk.wbs.espider.holders;

import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlk.wbs.espider.lib.IconTextView;
import com.hlk.wbs.espider.lib.RippleView;
import com.hlk.wbs.espider.lib.RippleView.OnRippleCompleteListener;
import com.hlk.hlklib.inject.Click;
import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.fragments.AboutFragment;
import com.hlk.wbs.espider.fragments.BindAccountFragment;
import com.hlk.wbs.espider.fragments.MainFragment;
import com.hlk.wbs.espider.fragments.base.BaseFragment;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.models.SelectItem;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 11:05 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class OpenableViewHolder extends BaseViewHolder {

    @ViewId(R.id.ui_view_item_openable_icon)
    private IconTextView icon;
    @ViewId(R.id.ui_view_item_openable_title)
    private TextView title;
    @ViewId(R.id.ui_view_item_openable_text)
    private TextView text;
    @ViewId(R.id.ui_view_item_openable_container)
    private RippleView container;
    @ViewId(R.id.ui_view_item_openable_suffix)
    private IconTextView suffix;

    private SelectItem item;

    /**
     * 创建ViewHolder
     *
     * @param itemView 该Item的根view
     * @param fragment 主显示fragment
     */
    public OpenableViewHolder(View itemView, BaseFragment fragment) {
        super(itemView, fragment);
        ViewUtility.bind(this, itemView);
    }

    public void showContent(SelectItem item) {
        this.item = item;
        showContent();
    }

    private void showContent() {
        icon.setFontSource(item.font);
        icon.setText(item.icon);
        icon.setBackgroundShapeColor(item.color);
        title.setText(item.title);
        text.setText(Html.fromHtml(item.description));
        text.setVisibility(Utils.isEmpty(item.description) ? View.GONE : View.VISIBLE);
        suffix.setVisibility(item.suffix ? View.VISIBLE : View.GONE);
        Handler().post(new Runnable() {
            @Override
            public void run() {
                int size = icon.getMeasuredHeight();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) icon.getLayoutParams();
                params.width = size;
                icon.setLayoutParams(params);
            }
        });
        container.setOnRippleCompleteListener(mOnRippleCompleteListener);
    }

    private OnRippleCompleteListener mOnRippleCompleteListener = new OnRippleCompleteListener() {
        @Override
        public void onComplete(RippleView rippleView) {
            if (item.suffix) {
                if (Fragment() instanceof MainFragment) {
                    clickEventInMainFragment();
                }
            }
        }
    };

    @Click(R.id.ui_view_item_openable_container)
    private void viewClick(View view) {
    }

    private void clickEventInMainFragment() {
        switch (item.index) {
            case 1:
                openActivity(BindAccountFragment.class.getName(), "");
                break;
            case 2:
//                HashMap<String, Object> map = new HashMap<>();
//                map.put(Position.Columns.Report, false);
//                map.put(Position.Columns.ReportTime, 0);
//                WhereBuilder builder = new WhereBuilder(Position.class)
//                        .equals(Position.Columns.Report, true)
//                        .and()
//                        .noEquals(Position.Columns.Alarm, Alarm.NoAlarm);
//                App.Orm.update(builder, new ColumnsValue(map), ConflictAlgorithm.Rollback);
                //new PeriodReportTask().exec();
                break;
            case 4:
                // check update
                checkUpdate();
                break;
            case 5:
                openActivity(AboutFragment.class.getName(), "");
                break;
        }
    }

    private void checkUpdate() {
        ((MainFragment) Fragment()).changeUpdateStatus(StringHelper.getString(R.string.ui_view_holder_text_check_update));
        App.getInstance().checkUpdate();
    }
}

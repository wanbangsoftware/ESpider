package com.hlk.wbs.espider.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.fragments.base.SwipeRecyclerViewFragment;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.holders.BlankViewHolder;
import com.hlk.wbs.espider.holders.OpenableViewHolder;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.SelectItem;

import net.servicestack.func.Predicate;

import java.util.ArrayList;
import java.util.List;

import static net.servicestack.func.Func.filter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends SwipeRecyclerViewFragment {

    private List<SelectItem> items = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void destroyView() {

    }

    @Override
    protected void findViews() {
        ViewUtility.bind(this, mView);
    }

    @Override
    protected void getParamsFromBundle(Bundle bundle) {

    }

    @Override
    protected void saveParamsToBundle(Bundle bundle) {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_main;
    }

    @Override
    public void doingInResume() {
        String appName = StringHelper.getString(R.string.app_name);
        Activity().setTitle(" " + StringHelper.getString(R.string.app_title, appName));
        disableSwipeRefresh();
        initializeItems();
    }

    private void initializeItems() {
        if (items.size() < 1) {
            String[] temp = App.getInstance().getResources().getStringArray(R.array.ui_list_view_items);
            for (String string : temp) {
                SelectItem item = new SelectItem(string);
                items.add(item);
            }
        }
        checkBindStatus();
        checkPeriodStatus();
        initializeAdapter();
    }

    private void checkBindStatus() {
        List<SelectItem> temp = filter(items, new Predicate<SelectItem>() {
            @Override
            public boolean apply(SelectItem selectItem) {
                return selectItem.index == 1;
            }
        });
        if (null != temp && temp.size() > 0) {
            SelectItem item = temp.get(0);
            // 绑定状态
            String account = App.getInstance().getLastAccount();
            String session = App.getInstance().getLastSession();
            item.suffix = true;
            if (!Utils.isEmpty(account)) {
                if (Utils.isEmpty(session)) {
                    // 需要重新绑定账号
                    item.description = StringHelper.getString(R.string.ui_view_holder_text_account_rebind);
                    item.icon = StringHelper.getString(R.string.ui_base_text_account_bind_icon_rebind);
                } else {
                    // 已绑定账号
                    String name = PreferenceHelper.get(R.string.preference_tag_app_account_name, "-");
                    item.description = StringHelper.getString(R.string.ui_view_holder_text_account_bound, name);
                    item.icon = StringHelper.getString(R.string.ui_base_text_account_bind_icon_bound);
                    item.suffix = false;
                }
            }
        }
    }

    private void checkPeriodStatus() {
        List<SelectItem> temp = filter(items, new Predicate<SelectItem>() {
            @Override
            public boolean apply(SelectItem selectItem) {
                return selectItem.index == 2;
            }
        });
        if (null != temp && temp.size() > 0) {
            SelectItem item = temp.get(0);
            String desc = App.getInstance().getResources()
                    .getStringArray(R.array.ui_list_view_items)[2].split("\\|", -1)[4];
            item.description = format(desc, App.getInstance().getReportPeriod());
        }
    }

    public void changeUpdateStatus(String status) {
        items.get(4).description = status;
        initializeAdapter();
    }

    private void initializeAdapter() {
        if (null == mAdapter) {
            mAdapter = new RecyclerViewAdapter();
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSwipeRefreshing() {
        Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

    @Override
    protected void onDataRefreshed(String className) {
        if (className.equals(Account.class.getName())) {
            Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeItems();
                }
            }, 200);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater inflater;

        public RecyclerViewAdapter() {
            super();
            inflater = LayoutInflater.from(Activity());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case Type.HEADER:
                    return new HeadViewHolder(inflater.inflate(R.layout.layout_view_item_everdigm, parent, false));
                case Type.Blank:
                    return new BlankViewHolder(inflater.inflate(R.layout.layout_view_item_blank, parent, false), MainFragment.this);
                default:
                    return new OpenableViewHolder(inflater.inflate(R.layout.layout_view_item_openable, parent, false), MainFragment.this);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof OpenableViewHolder) {
                ((OpenableViewHolder) holder).showContent(items.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return Type.HEADER;

            if (Utils.isEmpty(items.get(position).icon))
                return Type.Blank;

            return Type.Normal;
        }
    }

    private class HeadViewHolder extends RecyclerView.ViewHolder {

        public HeadViewHolder(View itemView) {
            super(itemView);
        }

    }

    private static class Type {
        public static final int HEADER = 0;
        public static final int Normal = 1;
        public static final int Blank = 2;
    }
}

package com.hlk.wbs.espider.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hlk.hlklib.etc.Cryptography;
import com.hlk.hlklib.etc.Utility;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.base.ToolbarActivity;
import com.hlk.wbs.espider.api.Api;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.fragments.base.SwipeRecyclerViewFragment;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.helpers.SnackbarHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.holders.BaseViewHolder;
import com.hlk.wbs.espider.holders.BlankViewHolder;
import com.hlk.wbs.espider.holders.ButtonViewHolder;
import com.hlk.wbs.espider.holders.InputableViewHolder;
import com.hlk.wbs.espider.holders.WarningViewHolder;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.InputItem;
import com.hlk.wbs.espider.models.JsonResult;
import com.hlk.wbs.espider.tasks.SimpleHttpTask;
import com.hlk.wbs.tx.custom.CustomConvert;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 11:04 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class BindAccountFragment extends SwipeRecyclerViewFragment {

    private List<InputItem> inputs = new ArrayList<>();
    private InputAdapter mAdapter;

    @Override
    protected void onSwipeRefreshing() {

    }

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
        return R.layout.layout_refreshable_recycler_view;
    }

    @Override
    public void doingInResume() {
        Activity().setTitle(StringHelper.getString(R.string.ui_fragment_title_bind_account));
        ((ToolbarActivity) Activity()).showNavigationIcon();
        disableSwipeRefresh();
        initItems();
    }

    private void initItems() {
        if (inputs.size() < 1) {
            String[] temp = App.getInstance().getResources().getStringArray(R.array.ui_list_bind_account);
            for (String string : temp) {
                InputItem item = new InputItem(string);
                inputs.add(item);
            }
        }
        initAdapter();
    }

    private void initAdapter() {
        if (null == mAdapter) {
            mAdapter = new InputAdapter();
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDataRefreshed(String className) {

    }

    private class InputAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private final LayoutInflater inflater;

        public InputAdapter() {
            super();
            inflater = LayoutInflater.from(Activity());
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case Type.Blank:
                    return new BlankViewHolder(inflater.inflate(R.layout.layout_view_item_blank, parent, false), BindAccountFragment.this);
                case Type.Button:
                    return new ButtonViewHolder(inflater.inflate(R.layout.layout_view_item_button, parent, false), BindAccountFragment.this);
                case Type.Text:
                    return new WarningViewHolder(inflater.inflate(R.layout.layout_view_item_warning, parent, false), BindAccountFragment.this);
                default:
                    return new InputableViewHolder(inflater.inflate(R.layout.layout_view_item_inputable, parent, false), BindAccountFragment.this);
            }
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            if (holder instanceof InputableViewHolder) {
                ((InputableViewHolder) holder).showContent(inputs.get(position));
            } else if (holder instanceof ButtonViewHolder) {
                ((ButtonViewHolder) holder).showContent(inputs.get(position));
            } else if (holder instanceof WarningViewHolder) {
                ((WarningViewHolder) holder).showContent(inputs.get(position).title);
            }
        }

        @Override
        public int getItemCount() {
            return inputs.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == inputs.size() - 1)
                return Type.Button;

            if (inputs.get(position).title.charAt(0) == '-')
                return Type.Blank;
            if (inputs.get(position).title.length() > 20)
                return Type.Text;
            return Type.Input;
        }
    }

    private String name, pwd;

    public void setAccount(String account) {
        this.name = account;
    }

    public void setPassword(String pwd) {
        this.pwd = pwd;
    }

    /**
     * 获取用户的输入并进行操作
     */
    public void verifyInputtedValues() {
        Utility.hidingInputBoard(mSwipeRefreshLayout);
        if (!CustomConvert.verify(name, StringHelper.getString(R.string.ui_regex_account))) {
            SnackbarHelper.show(mSwipeRefreshLayout, StringHelper.getString(R.string.ui_warning_account_fail), SnackbarHelper.ShowType.Warning);
            return;
        }
        if (!CustomConvert.verify(pwd, StringHelper.getString(R.string.ui_regex_password))) {
            SnackbarHelper.show(mSwipeRefreshLayout, StringHelper.getString(R.string.ui_warning_password_fail), SnackbarHelper.ShowType.Warning);
            return;
        }

        Account account = new Account();
        account.name = name;
        account.md5 = Cryptography.md5(pwd);
        account.device = App.getInstance().getDeviceId();

        new SimpleHttpTask().addOnTaskExecuteListener(listener).exec(Api.ApiUrl(), Api.BindAccount(account));
    }

    private SweetAlertDialog progress;

    private OnTaskExecuteListener listener = new OnTaskExecuteListener() {
        @Override
        public void onPrepared() {
            progress = new SweetAlertDialog(Activity(), SweetAlertDialog.PROGRESS_TYPE).setTitleText(StringHelper.getString(R.string.ui_warning_progressing));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        public void doneInBackground(JsonResult result) {

        }

        @Override
        public void onComplete(JsonResult result) {
            if (null != result && result.State == 0) {
                Account account = Gson().fromJson(result.Data, Account.class);
                // 账户所属区域
                PreferenceHelper.save(R.string.preference_tag_app_account_belong, account.device);
                // 绑定账号时直接保存账号和session，不用再通知绑定账号
                App.getInstance().saveAccount(account.data, account.session);
                finish();
            } else {
                SnackbarHelper.show(mRecyclerView, null == result ? StringHelper.getString(R.string.ui_warning_server_handle_fail) : result.Data, SnackbarHelper.ShowType.Warning);
            }
            if (null != progress) {
                progress.dismissWithAnimation();
            }
        }
    };

    static class Type {
        static final int Input = 0;
        static final int Blank = 1;
        static final int Text = 2;
        static final int Button = 3;
    }
}

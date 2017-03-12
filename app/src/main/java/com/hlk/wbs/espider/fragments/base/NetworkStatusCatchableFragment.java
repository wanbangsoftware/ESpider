package com.hlk.wbs.espider.fragments.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Action;

/**
 * <b>功能：</b>网络不可用时，显示提示<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/02 13:59 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public abstract class NetworkStatusCatchableFragment extends DataRefreshableFragment {

    private View networkCaution;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        findView();
        return view;
    }

    private void findView() {
        networkCaution = mView.findViewById(R.id.ui_network_caution);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayNetworkCaution(!App.getInstance().isNetworkAvailable());
    }

    @Override
    public void onDestroy() {
        Activity().unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    private void registerReceiver() {
        IntentFilter i = new IntentFilter(Action.NETWORK_CHANGED_ACTION);
        Activity().registerReceiver(networkChangeReceiver, i);
    }

    private void displayNetworkCaution(boolean shown) {
        if (null != networkCaution) {
            networkCaution.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (action.equals(Action.NETWORK_CHANGED_ACTION)) {
                displayNetworkCaution(!App.getInstance().isNetworkAvailable());
            }
        }
    };
}

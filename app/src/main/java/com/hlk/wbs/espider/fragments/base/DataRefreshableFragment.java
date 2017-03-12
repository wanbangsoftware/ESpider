package com.hlk.wbs.espider.fragments.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.tasks.OrmTask;

/**
 * <b>功能</b>：能提供后台数据刷新通知的fragment<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 23:53 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class DataRefreshableFragment extends BaseFragment {

    /**
     * 后台数据刷新的通知
     */
    protected abstract void onDataRefreshed(String className);

    @Override
    public void onStart() {
        super.onStart();
        register();
    }

    private void register() {
        IntentFilter intent = new IntentFilter(OrmTask.ORM_DATA_CHANGED_ACTION);
        intent.addAction(Action.ACCOUNT_CHANGED_ACTION);
        Activity().registerReceiver(mDataRefreshedReceiver, intent);
    }

    @Override
    public void onStop() {
        Activity().unregisterReceiver(mDataRefreshedReceiver);
        super.onStop();
    }

    private BroadcastReceiver mDataRefreshedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                switch (action) {
                    case OrmTask.ORM_DATA_CHANGED_ACTION:
                        String clazz = intent.getStringExtra(OrmTask.EFFECTED);
                        //log(format("Data refreshed with: %s", clazz));
                        if (!Utils.isEmpty(clazz)) {
                            onDataRefreshed(clazz);
                        }
                        break;
                    case Action.ACCOUNT_CHANGED_ACTION:
                        onDataRefreshed(Account.class.getName());
                        break;
                }
            }
        }
    };
}

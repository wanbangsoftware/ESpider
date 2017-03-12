package com.hlk.wbs.espider.fragments.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import com.hlk.hlklib.inject.ViewId;
import com.hlk.wbs.espider.R;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/03 09:25 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public abstract class SwipeRecyclerViewFragment extends NothingLoadingFragment {

    // UI
    @ViewId(R.id.ui_refreshable_swipe_refresh_layout)
    public SwipeRefreshLayout mSwipeRefreshLayout;
    @ViewId(R.id.ui_refreshable_recycler_view)
    public RecyclerView mRecyclerView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != mRecyclerView) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initRefreshableItems();
    }

    protected void initRefreshableItems() {
        if (null == mSwipeRefreshLayout) return;
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                getResources().getDisplayMetrics()));
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        registerForContextMenu(mRecyclerView);
    }

    protected void disableSwipeRefresh() {
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            onSwipeRefreshing();
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mSwipeRefreshLayout.isEnabled()) {
                LinearLayoutManager llm = ((LinearLayoutManager) recyclerView.getLayoutManager());
                mSwipeRefreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
    };

    /**
     * 下拉刷新
     */
    protected abstract void onSwipeRefreshing();
}

package com.hlk.wbs.espider.fragments.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.activities.ContainerActivity;
import com.hlk.wbs.espider.activities.base.BaseActivity;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.helpers.StringHelper;

import java.lang.reflect.Field;

/**
 * <b>功能：</b>所有Fragment的基类<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/02 13:42 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public abstract class BaseFragment extends PermissionRequestableFragment {

    /**
     * 需求新打开的Activity返回结果
     */
    public static final int ACTIVITY_RESULT_REQUEST = 10000;
    /**
     * 不需要新打开的Activity返回结果
     */
    public static final int ACTIVITY_RESULT_NONE = -1;

    protected Gson gson = App.getInstance().Gson();

    /**
     * 获取一个Gson实例
     */
    public Gson Gson() {
        return gson;
    }

    private Handler mHandler = new Handler();

    /**
     * 获取一个Handler实例
     */
    public Handler Handler() {
        return mHandler;
    }

    /**
     * 当前 fragment 的 layout 资源 id
     */
    private int mLayout;

    protected View mView;

    /**
     * 格式化字符串
     */
    public String format(String format, Object... args) {
        return StringHelper.format(format, args);
    }

    public int getColor(int res) {
        return ContextCompat.getColor(App.getInstance(), res);
    }

    public void log(String string) {
        LogHelper.log(BaseFragment.this.getClass().getSimpleName(), string);
    }

    /**
     * 当前设备屏幕宽度像素
     */
    protected int mScreenWidth;
    /**
     * 当前设备屏幕高度像素
     */
    protected int mScreenHeight;

    /**
     * 获取当前设备的屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    /**
     * 销毁View时
     */
    protected abstract void destroyView();

    /**
     * 获取UI控件<br />
     * 由于此方法是在onCreateView的return之前调用的，所以使用注解时需要传入_rootView进行注解，
     * 否则所有的注解属性都会是null(因为此时rootView还未加到fragment里去)
     */
    protected abstract void findViews();

    /**
     * 从Bundle中获取初始化fragment的参数列表
     */
    protected abstract void getParamsFromBundle(Bundle bundle);

    /**
     * 暂存fragment的初始化参数列表
     */
    protected abstract void saveParamsToBundle(Bundle bundle);

    protected boolean DEBUG = false;

    private Context context;
    private BaseActivity activity;

    /**
     * 获取当前attach的activity实例
     */
    public BaseActivity Activity() {
        return activity;
    }

    @Override
    public Context getContext() {
        Context ctx = super.getContext();
        if (null == ctx && null != context) {
            return context;
        }
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (null == this.context) {
            this.context = context;
        }
        activity = (BaseActivity) getActivity();
        if (DEBUG) {
            log("onAttach");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            log("onCreate");
        }
        mLayout = getLayout();
        getScreenSize();

        if (null == savedInstanceState) {
            Bundle b = getArguments();
            if (null != b) {
                getParamsFromBundle(b);
            }
        } else {
            getParamsFromBundle(savedInstanceState);
        }
    }

    public abstract int getLayout();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mView) {
            mView = inflater.inflate(mLayout, container, false);
        }
        findViews();
        if (DEBUG) {
            log("onCreateView");
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) {
            log("onActivityCreated");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            log("onStart");
        }
    }

    /**
     * Fragment onResume过程中需要执行的方法
     */
    public abstract void doingInResume();

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            log("onResume");
        }
        doingInResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            log("onPause");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) {
            log("onStop");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null == outState) outState = new Bundle();
        saveParamsToBundle(outState);
        if (DEBUG) {
            log("onSaveInstanceState");
        }
        super.onSaveInstanceState(outState);
    }

    public void removeParent(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (null != parent) {
            parent.removeView(view);
        }
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) {
            log("onDestroyView");
        }
        destroyView();
        super.onDestroyView();
        if (null != mView) {
            removeParent(mView);
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            log("onDestroy");
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (DEBUG) {
            log("onDetach");
        }
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动容器Activity
     *
     * @param fragmentFullClassName 需要在容器中显示的Fragment的class全名
     * @param params                附加参数
     */
    public void openActivity(String fragmentFullClassName, String params) {
        openActivity(fragmentFullClassName, params, ACTIVITY_RESULT_REQUEST);
    }

    /**
     * 启动容器Activity
     *
     * @param fragmentFullClassName 需要在容器中显示的Fragment的 class全名
     * @param params                附加参数
     * @param requestCode           区别码
     */
    public void openActivity(String fragmentFullClassName, String params, int requestCode) {
        Intent intent = new Intent(Activity(), ContainerActivity.class);
        Bundle b = new Bundle();
        b.putInt(ContainerActivity.REQUEST_CODE, requestCode);
        b.putString(ContainerActivity.REQUEST_CLASS, fragmentFullClassName);
        b.putString(ContainerActivity.REQUEST_PARAMS, params);
        intent.putExtra(ContainerActivity.EXTRA_BUNDLE, b);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 关闭Activity
     */
    public void finish() {
        Utility.hidingInputBoard(mView);
        Activity().finish();
    }
}

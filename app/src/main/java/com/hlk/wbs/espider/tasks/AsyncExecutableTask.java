package com.hlk.wbs.espider.tasks;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.ToastHelper;
import com.hlk.wbs.espider.models.JsonResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 作者：Hsiang Leekwok on 2015/08/31 09:52<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public abstract class AsyncExecutableTask<Params, Progress, Result> extends HttpPostTask<Params, Progress, Result> {

    protected JsonResult result;

    @SuppressWarnings({"unchecked", "varargs"})
    @Override
    protected Result doInBackground(Params... params) {
        Result ret = doInTask(params);
        if (null != mOnTaskExecuteListener) {
            mOnTaskExecuteListener.doneInBackground(result);
        }
        return ret;
    }

    @Override
    protected void onPreExecute() {
        if (null != mOnTaskExecuteListener) {
            mOnTaskExecuteListener.onPrepared();
        }
        doBeforeExecute();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Result result) {
        doAfterExecute();
        super.onPostExecute(result);
    }

    @SuppressWarnings({"unchecked", "varargs"})
    @Override
    protected void onProgressUpdate(Progress... values) {
        doProgress(values);
        super.onProgressUpdate(values);
    }

    /**
     * 获取网络返回的json
     */
    protected void fetchingJson(String... params) {
        if (!App.getInstance().isNetworkAvailable()) {
            if (App.getInstance().getNetworkNotAvailableWarning() < 1) {
                ToastHelper.showMsg(R.string.ui_warning_network_not_available);
                App.getInstance().setNetworkNotAvailableWarning();
            }
        } else {
            String json = postHttp(params);
            // 网络调用失败提示服务有问题
            if (Utils.isEmpty(json)) {
                if (App.getInstance().getServiceNotReachableWarning() < 2) {
                    ToastHelper.showMsg(R.string.ui_warning_network_service_not_available);
                    App.getInstance().setServiceNotReachableWarning();
                }
            } else {
                // 去掉json头尾的空白字符
                json = json.trim();
                parseResult(json);
            }
        }
    }

    private void parseResult(String json) {
        try {
            JSONObject obj = (JSONObject) new JSONTokener(json).nextValue();
            result = new JsonResult();
            if (obj.has("State")) {
                result.State = obj.getInt("State");
            }
            if (obj.has("Data")) {
                result.Data = obj.getString("Data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Task执行前需要做的工作，如更新UI等
     */
    protected void doBeforeExecute() {
    }

    /**
     * Task执行过程中执行进度更新时要做的工作，如更新UI等
     */
    @SuppressWarnings({"unchecked", "varargs"})
    protected void doProgress(Progress... values) {
    }

    /**
     * Task的执行的方法，更改UI的方法不要放在这里
     */
    @SuppressWarnings({"unchecked", "varargs"})
    protected abstract Result doInTask(Params... params);

    /**
     * Task执行完毕之后需要做的工作，如更新UI等
     */
    protected abstract void doAfterExecute();

    protected OnTaskExecuteListener mOnTaskExecuteListener;

    public AsyncExecutableTask addOnTaskExecuteListener(OnTaskExecuteListener l) {
        mOnTaskExecuteListener = l;
        return this;
    }
}

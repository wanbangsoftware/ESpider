package com.hlk.wbs.espider.tasks;


import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.models.JsonResult;

/**
 * <b>功能：</b>简单的HttpPostTask<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/17 01:50 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class SimpleHttpTask extends AsyncExecutableTask<String, Void, JsonResult> {

    @Override
    protected JsonResult doInTask(String... params) {
        fetchingJson(params);
        return result;
    }

    @Override
    protected void doAfterExecute() {
        if (null != mOnTaskExecuteListener) {
            mOnTaskExecuteListener.onComplete(result);
        }
    }

    @Override
    public SimpleHttpTask addOnTaskExecuteListener(OnTaskExecuteListener l) {
        super.addOnTaskExecuteListener(l);
        return this;
    }
}

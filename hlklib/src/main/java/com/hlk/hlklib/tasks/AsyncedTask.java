package com.hlk.hlklib.tasks;

import android.os.AsyncTask;
import android.os.Build;

import com.hlk.hlklib.etc.Log;

/**
 * 自定义的AsyncTask
 */
public abstract class AsyncedTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    @SuppressWarnings({"unchecked", "varargs"})
    public AsyncTask<Params, Progress, Result> exec(Params... param) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return executeOnExecutor(THREAD_POOL_EXECUTOR, param);
        } else {
            return execute(param);
        }
    }

    protected void log(String string) {
        Log.log(AsyncedTask.this.getClass().getSimpleName(), string);
    }
}

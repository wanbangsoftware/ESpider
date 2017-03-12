package com.hlk.wbs.espider.tasks;

import android.content.Intent;

import com.hlk.hlklib.tasks.AsyncedTask;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnLiteOrmTaskExecuteListener;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.helpers.LogHelper;

import java.util.List;

/**
 * <b>功能</b>：提供LiteOrm数据存取的线程<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 14:15 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class OrmTask<E> extends AsyncedTask<Object, Integer, Void> {

    /**
     * 更新数据库时受影响的类
     */
    public static final String EFFECTED = "_effected_class_";
    /**
     * 通知广播LiteOrm数据已更改
     */
    public static final String ORM_DATA_CHANGED_ACTION = Action.BASE_ACTION + ".intent.ORM_DATA_CHANGED";

    private String effectClass;

    private List<E> list;

    private boolean modify = false;

    public OrmTask(Class<E> clazz) {
        effectClass = clazz.getName();
    }

    @Override
    protected void log(String string) {
        LogHelper.log(this.getClass().getSimpleName(), string, true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (null != listener) {
            listener.onPrepared();
        }
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (null != listener) {
            modify = listener.isExecutingWithModify();
            list = listener.executing(this);
        }
        return null;
    }

    /**
     * 更改task的执行进度
     */
    public void progressing(int percentage) {
        publishProgress(percentage);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (null != listener) {
            listener.progressing(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (modify) {
            // 如果是插入、删除、更新操作，则需要发送数据更改广播
            notifyDataChanged();
        }
        if (null != listener) {
            listener.onExecuted(list);
        }
        super.onPostExecute(result);
    }

    /**
     * 向app内部广播数据已更改
     */
    private void notifyDataChanged() {
        Intent intent = new Intent(ORM_DATA_CHANGED_ACTION);
        intent.putExtra(EFFECTED, effectClass);
        App.getInstance().sendBroadcast(intent);
    }

    private OnLiteOrmTaskExecuteListener<E> listener;

    public OrmTask addOnLiteOrmTaskExecuteListener(OnLiteOrmTaskExecuteListener<E> l) {
        listener = l;
        return this;
    }
}

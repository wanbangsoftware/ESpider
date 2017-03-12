package com.hlk.wbs.espider.callbacks;

import java.util.List;

/**
 * <b>功能</b>：ListOrm数据库操作回调<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 14:42 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public interface OnLiteOrmTaskExecuteListener<E> {
    /**
     * Task准备执行，主线程中回调
     */
    void onPrepared();

    /**
     * 标记执行过程是否修改数据
     */
    boolean isExecutingWithModify();

    /**
     * Task执行过程
     *
     * @param object 执行任务的task对象
     * @return modify为true时，返回可以为null，否则返回查询结果
     */
    List<E> executing(Object object);

    /**
     * 执行进度，主线程中回调
     */
    void progressing(int percentage);

    /**
     * Task执行完毕，主线程中调用
     */
    void onExecuted(List<E> result);
}

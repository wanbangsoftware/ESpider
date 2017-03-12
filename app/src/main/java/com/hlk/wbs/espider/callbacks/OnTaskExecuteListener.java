package com.hlk.wbs.espider.callbacks;

import com.hlk.wbs.espider.models.JsonResult;

/**
 * <b>功能</b>：Task执行完毕的回调<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/03/20 23:27 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public interface OnTaskExecuteListener {
    /**
     * 准备执行时，主UI更新
     */
    void onPrepared();

    /**
     * Task执行完毕，此时还处于非主UI
     *
     * @param result 服务器返回的结果
     */
    void doneInBackground(JsonResult result);

    /**
     * 执行完毕的回调，由主线程调用
     *
     * @param result 服务器返回的结果
     */
    void onComplete(JsonResult result);
}

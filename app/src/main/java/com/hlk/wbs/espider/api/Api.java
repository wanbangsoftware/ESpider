package com.hlk.wbs.espider.api;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.Post;
import com.hlk.wbs.espider.models.Updater;

/**
 * <b>功能</b>：api接口方法集合<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 19:53 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Api {

    public static String format(String fmt, Object... args) {
        return StringHelper.format(fmt, args);
    }

    /**
     * 组成post的json字符串
     */
    private static String parsePost(String cmd, String content) {
        return postJson(new Post(cmd, content));
    }

    /**
     * 将post类转换成json
     */
    private static String postJson(Post post) {
        return parseJson(post);
    }

    /**
     * 将object转成json对象
     */
    private static String parseJson(Object object) {
        return App.getInstance().Gson().toJson(object);
    }

    /**
     * 将json进行转义
     */
    public static String escapeJson(String json) {
        return json.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }

    /**
     * api地址
     */
    public static String ApiUrl() {
        return format("%s%s", App.getInstance().getApiUrl(), "ajax/api.ashx");
    }

    /**
     * 获取服务器参数
     */
    public static String GetParameter(Account account) {
        return parsePost("GetParameter", parseJson(account));
    }

    /**
     * 检测新版本
     */
    public static String CheckUpdate(Updater updater) {
        return parsePost("CheckUpdate", parseJson(updater));
    }

    /**
     * 绑定账号和设备
     */
    public static String BindAccount(Account account) {
        return parsePost("BindAccount", parseJson(account));
    }

    /**
     * 汇报数据到服务器
     */
    public static String Report(Account account) {
        return parsePost("Report", parseJson(account));
    }
}

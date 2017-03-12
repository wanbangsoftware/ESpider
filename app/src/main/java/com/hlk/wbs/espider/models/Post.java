package com.hlk.wbs.espider.models;

/**
 * <b>功能</b>：post请求的基本结构<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/07 21:52 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Post {
    /**
     * 命令字
     */
    public String cmd;
    /**
     * 内容
     */
    public String content;

    public Post() {
    }

    public Post(String cmd, String content) {
        this.cmd = cmd;
        this.content = content;
    }
}

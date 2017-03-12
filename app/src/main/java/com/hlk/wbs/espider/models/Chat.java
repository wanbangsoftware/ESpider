package com.hlk.wbs.espider.models;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Table;

/**
 * <b>功能：</b>服务器发来的设定或通知信息<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/09/04 19:11 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
@Table("chat")
public class Chat extends Model {

    /**
     * 列名
     */
    public static class Columns {
        public static final String ID="id";
        public static final String CreateTime = "createTime";
        public static final String PublishTime = "publishTime";
        public static final String Deliver = "deliver";
        public static final String Type = "type";
        public static final String Content = "content";
    }

    @Column(Columns.ID)
    public long id;

    @Column(Columns.CreateTime)
    public long createTime;

    @Column(Columns.PublishTime)
    public long publishTime;

    @Column(Columns.Deliver)
    public String deliver;

    @Column(Columns.Type)
    public int type;

    @Column(Columns.Content)
    public String content;

    /**
     * 消息类型
     */
    public static class Type {
        /**
         * 系统通知
         */
        public static final int Notification = 0;
        /**
         * 修改设定
         */
        public static final int Setting = 1;
    }
}

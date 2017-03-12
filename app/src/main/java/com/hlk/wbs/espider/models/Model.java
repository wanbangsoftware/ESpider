package com.hlk.wbs.espider.models;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 07:54 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class Model {

    private static class Columns {
        public static final String Id = "_id";
    }

    // 指定自增，每个对象需要有一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    @Column(Columns.Id)
    public long id;
}

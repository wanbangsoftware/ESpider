package com.hlk.wbs.espider.models;

import android.graphics.Color;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 23:51 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class SelectItem {
    public int index;
    public String icon;
    public int color;
    public String title;
    public String description;
    public int type;
    public int font;
    public boolean suffix;

    public SelectItem(String string) {
        String[] temp = string.split("\\|", -1);
        if (temp[1].charAt(0) != '-') {
            index = Integer.valueOf(temp[0]);
            icon = temp[1];
            color = Color.parseColor(temp[2]);
            title = temp[3];
            description = temp[4];
            type = Integer.valueOf(temp[5]);
            font = Integer.valueOf(temp[6]);
            suffix = Integer.valueOf(temp[7]) > 0;
        }
    }
}

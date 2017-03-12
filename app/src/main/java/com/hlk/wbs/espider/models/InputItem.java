package com.hlk.wbs.espider.models;

import com.hlk.wbs.espider.etc.Utils;

/**
 * <b>功能：</b>输入框内容<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 11:25 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class InputItem {
    public int index;
    public String title;
    public String hint;
    public int max;
    public int inputType;

    public InputItem(String string) {
        String[] temp = string.split("\\|", -1);
        if (!Utils.isEmpty(temp[0])) {
            index = Integer.valueOf(temp[0]);
            title = temp[1];
            hint = temp[2];
            max = Integer.valueOf(temp[3]);
            inputType = Integer.valueOf(temp[4]);
        }
    }
}

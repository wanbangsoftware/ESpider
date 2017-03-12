package com.hlk.hlklib.etc;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

/**
 * <b>功能：</b>中国大陆手机号码输入格式<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/31 21:33 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class ChinaHandPhoneTextWatcher implements TextWatcher {

    private static final int MAX = 13;
    private static final int D1 = 4;
    private static final int D2 = 9;
    private char divider;

    public ChinaHandPhoneTextWatcher() {
        divider = ' ';
    }

    public ChinaHandPhoneTextWatcher(char divider) {
        this.divider = divider;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private void deleteChar(Editable s) {
        if (divider == s.charAt(s.length() - 1)) {
            s.delete(s.length() - 1, s.length());
        }
    }

    private void insertChar(Editable s) {
        char c = s.charAt(s.length() - 1);
        // Only if its a digit where there should be a space we insert a space
        if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(divider)).length <= 3) {
            s.insert(s.length() - 1, String.valueOf(divider));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // 139 9999 9999
        // 删除输入文本最后的空格
        if (s.length() > 0) {
            if (s.length() == D1 && s.charAt(s.length() - 1) == divider) {
                deleteChar(s);
            } else if (s.length() == D2 && s.charAt(s.length() - 1) == divider) {
                deleteChar(s);
            }
        }
        // Insert char where needed.
        if (s.length() > 0) {
            if (s.length() == D1) {
                insertChar(s);
            } else if (s.length() == D2) {
                insertChar(s);
            }
        }
    }
}

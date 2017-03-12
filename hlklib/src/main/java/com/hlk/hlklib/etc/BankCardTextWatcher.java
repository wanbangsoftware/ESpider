package com.hlk.hlklib.etc;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

/**
 * <b>功能：</b>提供银行卡输入格式化显示的TextWatcher<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/31 21:07 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class BankCardTextWatcher implements TextWatcher {

    private char divider;

    public BankCardTextWatcher() {
        divider = ' ';
    }

    public BankCardTextWatcher(char divider) {
        this.divider = divider;
    }

    public void setDivider(char divider) {
        this.divider = divider;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        // Remove last spacing char
//        if (s.length() > 0 && (s.length() % 5) == 0) {
//            final char c = s.charAt(s.length() - 1);
//            if (divider == c) {
//                s.delete(s.length() - 1, s.length());
//            }
//        }
//        // Insert char where needed.
//        if (s.length() > 0 && (s.length() % 5) == 0) {
//            int i = 4;
//            while (i < s.length()) {
//                char c = s.charAt(i);
//                // Only if its a digit where there should be a space we insert a space
//                if (Character.isDigit(c)) {
//                    s.insert(i, String.valueOf(divider));
//                }
//                i += 5;
//            }
//        }
        String initial = s.toString();
        // remove all non-digits characters
        String processed = initial.replaceAll("\\D", "");
        // insert a space after all groups of 4 digits that are followed by another digit
        processed = processed.replaceAll("(\\d{4})(?=\\d)", "$1" + divider);
        // to avoid stackoverflow errors, check that the processed is different from what's already
        //  there before setting
        if (!initial.equals(processed)) {
            // set the value
            s.replace(0, initial.length(), processed);
        }
    }
}

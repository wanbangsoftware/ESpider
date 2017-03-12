package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

/**
 * 提供边框背景的EditText<br />
 * Created by Hsiang Leekwok on 2015/12/24.
 */
public class CorneredEditText extends AppCompatEditText {

    // setting
    private int act_color, dft_color, dsb_color, bg_color, corner, lftt, lftb, ritt, ritb, border;

    public CorneredEditText(Context context) {
        this(context, null);
    }

    public CorneredEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public CorneredEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CorneredEditText, defStyle, 0);
        getAttributes(a);
        a.recycle();

        init();
    }

    private void getAttributes(TypedArray array) {
        act_color = array.getColor(R.styleable.CorneredEditText_cet_active_border, Default.ACT_COLOR);
        dft_color = array.getColor(R.styleable.CorneredEditText_cet_normal_border, Default.DFT_COLOR);
        dsb_color = array.getColor(R.styleable.CorneredEditText_cet_disabled_border, Default.DSB_COLOR);
        bg_color = array.getColor(R.styleable.CorneredEditText_cet_background, Color.WHITE);
        border = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_border_size, Default.BORDER);
        corner = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_corner_size, 0);
        if (corner > 0) {
            lftt = corner;
            lftb = corner;
            ritb = corner;
            ritt = corner;
        } else {
            lftb = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_left_bottom_corner, 0);
            lftt = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_left_top_corner, 0);
            ritb = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_right_bottom_corner, 0);
            ritt = array.getDimensionPixelOffset(R.styleable.CorneredEditText_cet_right_top_corner, 0);
        }
    }

    private Drawable getDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(border, color);
        gd.setColor(bg_color);
        gd.setCornerRadius(corner);
        if (lftt != corner || ritt != corner || lftb != corner || ritb != corner) {
            gd.setCornerRadii(new float[]{lftt, lftt, ritt, ritt, ritb, ritb, lftb, lftb});
        }
        return gd;
    }

    private void init() {
        Drawable active = getDrawable(act_color), normal = getDrawable(dft_color), disabled = getDrawable(dsb_color);
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{-android.R.attr.state_enabled}, disabled);
        sld.addState(new int[]{android.R.attr.state_pressed}, active);
        sld.addState(new int[]{android.R.attr.state_focused}, active);
        sld.addState(new int[]{}, normal);

        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(sld);
        } else {
            setBackgroundDrawable(sld);
        }
    }

    public void setActiveColor(int color) {
        act_color = color;
        init();
    }

    public void setNormalColor(int color) {
        dft_color = color;
        init();
    }

    public void setDisabledColor(int color) {
        dsb_color = color;
        init();
    }

    private static class Default {
        public static final int ACT_COLOR = Color.parseColor("#FF8909");
        public static final int DFT_COLOR = Color.parseColor("#2cc3bb");
        public static final int DSB_COLOR = Color.parseColor("#e7e7e7");
        public static final int CORNER = Utility.ConvertDp(5);
        public static final int BORDER = Utility.ConvertDp(1);
    }
}

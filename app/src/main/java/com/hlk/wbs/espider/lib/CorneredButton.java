package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

/**
 * 圆角边框的按钮
 * Created by Hsiang Leekwok on 2015/07/13.
 */
public class CorneredButton extends AppCompatButton {

    private int active, normal, disabled, corner, leftt, leftb, rightt, rightb, bgtype;

    public CorneredButton(Context context) {
        this(context, null);
    }

    public CorneredButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public CorneredButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化背景属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CorneredButton, defStyleAttr, 0);
        getAttributes(a);
        a.recycle();

        init();
    }

    private void getAttributes(TypedArray array) {
        active = array.getColor(R.styleable.CorneredButton_active_color, Default.ACT_COLOR);
        normal = array.getColor(R.styleable.CorneredButton_normal_color, Default.DFT_COLOR);
        disabled = array.getColor(R.styleable.CorneredButton_disabled_color, Default.DSB_COLOR);
        corner = array.getDimensionPixelOffset(R.styleable.CorneredButton_corner_size, 0);
        if (corner > 0) {
            leftb = corner;
            leftt = corner;
            rightb = corner;
            rightt = corner;
        } else {
            leftt = array.getDimensionPixelOffset(R.styleable.CorneredButton_left_top_corner_size, 0);
            leftb = array.getDimensionPixelOffset(R.styleable.CorneredButton_left_bottom_corner_size, 0);
            rightt = array.getDimensionPixelOffset(R.styleable.CorneredButton_right_top_corner_size, 0);
            rightb = array.getDimensionPixelOffset(R.styleable.CorneredButton_right_bottom_corner_size, 0);
        }
        bgtype = array.getInteger(R.styleable.CorneredButton_background_type, Default.BG_TYPE);
    }

    private Drawable getDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(Default.BORDER, color);
        gd.setColor(bgtype == 0 ? color : Color.WHITE);
        gd.setCornerRadius(corner);
        if (leftt != corner || rightt != corner || leftb != corner || rightb != corner) {
            gd.setCornerRadii(new float[]{leftt, leftt, rightt, rightt, rightb, rightb, leftb, leftb});
        }
        return gd;
    }

    private void init() {
        Drawable active = getDrawable(this.active), normal = getDrawable(this.normal), disabled = getDrawable(this.disabled);
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

        //initColors();
    }

    private void initColors() {
        int dft = getTextColors().getDefaultColor();
        int[] colors = new int[]{Default.DSB_TEXT, dft};
        int[][] state = new int[2][];
        state[0] = new int[]{-android.R.attr.state_enabled};
        state[1] = new int[]{};
        ColorStateList csl = new ColorStateList(state, colors);
        setTextColor(csl);
    }

    public void setActiveColor(int color) {
        active = color;
        init();
    }

    public void setNormalColor(int color) {
        normal = color;
        init();
    }

    private static class Default {
        /**
         * 默认背景类型为填充
         */
        public static final int BG_TYPE = 0;
        public static final int CORNER = Utility.ConvertDp(5);
        public static final int BORDER = Utility.ConvertDp(1);
        public static final int ACT_COLOR = Color.parseColor("#2cc3bb");
        public static final int DFT_COLOR = Color.parseColor("#2fd0c8");
        public static final int DSB_COLOR = Color.parseColor("#e7e7e7");
        public static final int DSB_TEXT = Color.parseColor("#d0d0d0");
    }
}

package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义打分UI<br />
 * 作者：Hsiang Leekwok on 2015/11/16 09:32<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class CustomRatingBar extends LinearLayout {

    private int star_num, font_size, orientation, spacing;
    private ColorStateList font_color;
    private float step_size, rate;
    private float density;
    // 星星列表
    private List<IconTextView> stars = new ArrayList<>();

    public CustomRatingBar(Context context) {
        this(context, null);
    }

    public CustomRatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttributes(context, attrs, defStyle);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        density = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar, defStyleAttr, 0);
        star_num = a.getInteger(R.styleable.CustomRatingBar_star_num, Setting.STAR_NUM);
        int font = a.getDimensionPixelOffset(R.styleable.CustomRatingBar_star_size, Setting.FONT_SIZE);
        font_size = (int) (font / density);
        orientation = a.getInteger(R.styleable.CustomRatingBar_star_orientation, Setting.ORIENTATION);
        step_size = a.getFloat(R.styleable.CustomRatingBar_step_size, Setting.STEP_SIZE);
        rate = a.getFloat(R.styleable.CustomRatingBar_default_rate, Setting.RATE);
        font_color = a.getColorStateList(R.styleable.CustomRatingBar_star_color);
        spacing = a.getDimensionPixelOffset(R.styleable.CustomRatingBar_star_spacing, Setting.SPACING);
        a.recycle();
        if (null == font_color) {
            font_color = ColorStateList.valueOf(Setting.STAR_COLOR);
        }
        initView(context);
    }

    private void initView(Context context) {
        LinearLayout view = (LinearLayout) View.inflate(context, R.layout.hlklib_custom_ratingbar, this);
        view.setOrientation(orientation == 0 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        stars.clear();
        for (int i = 0; i < star_num; i++) {
            IconTextView atv = getStar(i, context);
            stars.add(atv);
            view.addView(atv);
        }
        initializeRate();
    }

    private IconTextView getStar(int index, Context context) {
        IconTextView atv = new IconTextView(context);
        atv.setTextColor(font_color);
        atv.setTextSize(font_size);
        atv.setFontSource(IconTextView.FONT_AWESOME);
        if (Build.VERSION.SDK_INT >= 16) {
            atv.setBackground(null);
        } else {
            atv.setBackgroundDrawable(null);
        }
        // 初始化评分类型
        atv.setTag(Setting.TAG_INDEX, index);
        // 默认评分为未评分
        atv.setTag(Setting.TAG_RATE_TYPE, RateType.EMPTY);
        atv.setOnClickListener(mOnClickListenter);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, index >= star_num - 1 ? 0 : spacing, 0);
        atv.setLayoutParams(params);
        return atv;
    }

    private OnClickListener mOnClickListenter = new OnClickListener() {

        @Override
        public void onClick(View v) {
            changeRating((IconTextView) v);
        }
    };

    /**
     * 通过设置评分数动态设置星星的字体
     */
    private void changeRating(IconTextView view) {
        // 获取当前的rate
        int index = (Integer) view.getTag(Setting.TAG_INDEX);
        int type = (Integer) view.getTag(Setting.TAG_RATE_TYPE);
        float rate = (float) (1.0 * index);
        switch (type) {
            case RateType.EMPTY:
                rate += 1;
                view.setText(Setting.STAR_FULL);
                view.setTag(Setting.TAG_RATE_TYPE, RateType.FULL);
                break;
            case RateType.HALF:
                //rate -= step_size;
                view.setText(Setting.STAR_EMPTY);
                view.setTag(Setting.TAG_RATE_TYPE, RateType.EMPTY);
                break;
            case RateType.FULL:
                rate += 0.5;
                view.setText(Setting.STAR_HALF);
                view.setTag(Setting.TAG_RATE_TYPE, RateType.HALF);
                break;
        }
        this.rate = rate;
        initializeRate();
    }

    /**
     * 初始化评分
     */
    private void initializeRate() {
        int floor = (int) rate;
        for (int i = 0; i < star_num; i++) {
            IconTextView atv = stars.get(i);
            if (i < floor) {
                atv.setText(Setting.STAR_FULL);
                atv.setTag(Setting.TAG_RATE_TYPE, RateType.FULL);
            } else if (i > floor) {
                atv.setText(Setting.STAR_EMPTY);
                atv.setTag(Setting.TAG_RATE_TYPE, RateType.EMPTY);
            } else {
                float dot = rate - floor;
                if (dot * 10 > 0) {
                    atv.setText(Setting.STAR_HALF);
                    atv.setTag(Setting.TAG_RATE_TYPE, RateType.HALF);
                } else {
                    int type = (Integer) atv.getTag(Setting.TAG_RATE_TYPE);
                    atv.setText(type == RateType.FULL ? Setting.STAR_FULL : Setting.STAR_EMPTY);
                    //atv.setTag(Setting.TAG_RATE_TYPE, RateType.FULL);
                }
            }
        }
        if (null != mOnRateChangedListener) {
            mOnRateChangedListener.onRateChanged(this.rate);
        }
    }

    public void setRate(float rate) {
        this.rate = rate;
        initializeRate();
    }

    public float getRate() {
        return this.rate;
    }

    private OnRateChangedListener mOnRateChangedListener;

    public void addOnRateChangedListener(OnRateChangedListener l) {
        this.mOnRateChangedListener = l;
    }

    /**
     * 评分值变化的监听接口
     */
    public interface OnRateChangedListener {
        /**
         * 评分值变化的回调
         */
        void onRateChanged(float newRate);
    }

    /**
     * 评分类型
     */
    private static class RateType {
        /**
         * 该UI中的评分为空
         */
        public static final int EMPTY = 0;
        /**
         * 该UI中的评分为0.5
         */
        public static final int HALF = 1;
        /**
         * 该UI中的评分为1分
         */
        public static final int FULL = 2;
    }

    /**
     * 基本设置
     */
    private static class Setting {
        public static final int TAG_INDEX = R.id.hlklib_ids_custom_ratingbar_tag_index;
        public static final int TAG_RATE_TYPE = R.id.hlklib_ids_custom_ratingbar_tag_rate_type;
        /**
         * 默认星的数量
         */
        public static final int STAR_NUM = 5;
        /**
         * 字符大小(sp/dp)
         */
        public static final int FONT_SIZE = Utility.ConvertDp(25);
        /**
         * 默认星星之间的间隔为2dp
         */
        public static final int SPACING = Utility.ConvertDp(2);
        /**
         * 步长
         */
        public static final float STEP_SIZE = 0.5f;
        /**
         * 默认星的颜色
         */
        public static final int STAR_COLOR = Color.parseColor("#fe8802");
        /**
         * 默认评分
         */
        public static final float RATE = 0.0f;
        /**
         * 布局方式 0=横向布局1=纵向布局
         */
        public static final int ORIENTATION = 0;
        /**
         * 空星
         */
        public static final String STAR_EMPTY = "\uf006";
        /**
         * 半星
         */
        public static final String STAR_HALF = "\uf123";
        /**
         * 全星
         */
        public static final String STAR_FULL = "\uf005";
    }
}

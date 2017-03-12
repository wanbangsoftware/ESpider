package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

/**
 * 自定义Switch
 * 作者：Hsiang Leekwok on 2015/09/06 21:03<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class CustomSwitch extends LinearLayout {

    /**
     * 选择方式
     */
    public enum SwitchMode {
        /**
         * 左边选中
         */
        LEFT,
        /**
         * 中间选中
         */
        CENTER,
        /**
         * 右边选中
         */
        RIGHT
    }

    /**
     * 2项选择
     */
    private static final int SWITCH_TYPE_TWO = 0;
    /**
     * 3项选择
     */
    private static final int SWITCH_TYPE_THREE = 1;

    /**
     * 默认选中的左边
     */
    private SwitchMode _Mode = SwitchMode.LEFT;

    private View _Root;
    private TextView _left, _center, _right;

    private float density;

    // 设置
    private int switch_mode = SWITCH_TYPE_TWO;// 默认只有2项选择
    private String _leftText, _rightText, _centerText;
    private int _fontSize;
    private Drawable _background, _onBackground, _offBackground, _onUnBackground, _offUnBackground,
            _centerBackground, _centerUnBackground;
    private int _selectedColor, _unselectedColor;

    public CustomSwitch(Context context) {
        super(context);
        initDefaults(context);
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults(context);
        initialize(context, attrs);
    }

    @Deprecated
    private void initDefaults(Context context) {
        Resources res = context.getResources();
        density = res.getDisplayMetrics().density;
        _offUnBackground = res.getDrawable(R.drawable.hlklib_custom_switch_right_unselected);
        _onUnBackground = res.getDrawable(R.drawable.hlklib_custom_switch_left_unselected);
        _leftText = res.getString(R.string.hlklib_custom_switch_left_text);
        _rightText = res.getString(R.string.hlklib_custom_switch_right_text);
        _centerText = res.getString(R.string.hlklib_custom_switch_left_text);
        _onBackground = res.getDrawable(R.drawable.hlklib_custom_switch_left_selected);
        _offBackground = res.getDrawable(R.drawable.hlklib_custom_switch_right_selected);
        _background = res.getDrawable(R.drawable.hlklib_custom_switch_bg);
        _centerBackground = res.getDrawable(R.drawable.hlklib_custom_switch_center_selected);
        _centerUnBackground = res.getDrawable(R.drawable.hlklib_custom_switch_center_unselected);
        _fontSize = Utility.ConvertDp(14);
        _unselectedColor = res.getColor(R.color.hlklib_custom_switch_selected_color);
        _selectedColor = Color.WHITE;
        _Root = View.inflate(context, R.layout.hlklib_custom_switch, this);
        findViews();
    }

    private void findViews() {
        _left = (TextView) _Root.findViewById(R.id.hlklib_custom_switch_left_text);
        _right = (TextView) _Root.findViewById(R.id.hlklib_custom_switch_right_text);
        _center = (TextView) _Root.findViewById(R.id.hlklib_custom_switch_center_text);
        _left.setOnClickListener(mOnClickListener);
        _right.setOnClickListener(mOnClickListener);
        _center.setOnClickListener(mOnClickListener);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSwitch);
        String temp = a.getString(R.styleable.CustomSwitch_switch_off_text);
        if (!TextUtils.isEmpty(temp)) {
            _rightText = temp;
        }
        temp = a.getString(R.styleable.CustomSwitch_switch_on_text);
        if (!TextUtils.isEmpty(temp)) {
            _leftText = temp;
        }
        temp = a.getString(R.styleable.CustomSwitch_switch_center_text);
        if (!TextUtils.isEmpty(temp)) {
            _centerText = temp;
        }
        Drawable d = a.getDrawable(R.styleable.CustomSwitch_switch_background);
        if (null != d) _background = d;
        d = a.getDrawable(R.styleable.CustomSwitch_switch_on_background);
        if (null != d) _onBackground = d;
        d = a.getDrawable(R.styleable.CustomSwitch_switch_off_background);
        if (null != d) _offBackground = d;
        int font = a.getDimensionPixelOffset(R.styleable.CustomSwitch_switch_font_size, 0);
        if (font > 0) {
            _fontSize = (int) (font / density);
        }
        switch_mode = a.getInteger(R.styleable.CustomSwitch_switch_mode, SWITCH_TYPE_TWO);
        a.recycle();
        initViews();
    }

    private void initViews() {
        _center.setVisibility(switch_mode == SWITCH_TYPE_THREE ? View.VISIBLE : View.GONE);
        setLeftText(_leftText);
        setRightText(_rightText);
        setCenterText(_centerText);
    }

    public void setLeftText(String text) {
        if (null != _left) _left.setText(text);
    }

    public void setRightText(String text) {
        if (null != _right) _right.setText(text);
    }

    public void setCenterText(String text) {
        if (null != _center) _center.setText(text);
    }

    public void setFontSize(float size) {
        if (null != _left) _left.setTextSize(size);
        if (null != _right) _right.setTextSize(size);
    }

    public void setLeftSelectedBackground(Drawable drawable) {
        if (null != drawable) _onBackground = drawable;
    }

    public void setRightSelectedBackground(Drawable drawable) {
        if (null != drawable) _offBackground = drawable;
    }

    public SwitchMode getSwitchMode() {
        return _Mode;
    }

    /**
     * 设置当前选中的选项
     */
    public void setSelectedSwitch(SwitchMode mode) {
        if (_Mode != mode) {
            _Mode = mode;
            changeSelectedBackground();
            if (null != mOnSwitchChangeListener) {
                mOnSwitchChangeListener.onChanged(_Mode);
            }
        }
    }

    @Deprecated
    private void changeSelectedBackground() {
        _left.setBackgroundDrawable(_Mode == SwitchMode.LEFT ? _onBackground : _onUnBackground);
        _left.setTextColor(_Mode == SwitchMode.LEFT ? _selectedColor : _unselectedColor);
        _right.setBackgroundDrawable(_Mode == SwitchMode.RIGHT ? _offBackground : _offUnBackground);
        _right.setTextColor(_Mode == SwitchMode.RIGHT ? _selectedColor : _unselectedColor);
        _center.setBackgroundDrawable(_Mode == SwitchMode.CENTER ? _centerBackground : _centerUnBackground);
        _center.setTextColor(_Mode == SwitchMode.CENTER ? _selectedColor : _unselectedColor);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == _left) {
                if (_Mode != SwitchMode.LEFT) {
                    _Mode = SwitchMode.LEFT;
                    changeSelectedBackground();
                    if (null != mOnSwitchChangeListener) {
                        mOnSwitchChangeListener.onChanged(_Mode);
                    }
                }
            } else if (v == _right) {
                if (_Mode != SwitchMode.RIGHT) {
                    _Mode = SwitchMode.RIGHT;
                    changeSelectedBackground();
                    if (null != mOnSwitchChangeListener) {
                        mOnSwitchChangeListener.onChanged(_Mode);
                    }
                }
            } else if (v == _center) {
                if (_Mode != SwitchMode.CENTER) {
                    _Mode = SwitchMode.CENTER;
                    changeSelectedBackground();
                    if (null != mOnSwitchChangeListener) {
                        mOnSwitchChangeListener.onChanged(_Mode);
                    }
                }
            }
        }
    };

    private OnSwitchChangeListener mOnSwitchChangeListener;

    /**
     * 为开关设置状态改变时的处理回调
     */
    public void addOnSwitchChangeListener(OnSwitchChangeListener l) {
        mOnSwitchChangeListener = l;
    }

    /**
     * 开关状态改变事件
     */
    public interface OnSwitchChangeListener {
        /**
         * 开关状态改变事件的处理回调
         *
         * @param mode 标记已经该表到的状态
         */
        void onChanged(SwitchMode mode);
    }
}

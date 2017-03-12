package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

import java.util.Locale;

/**
 * <b>功能：</b>提供输入统计的EditText<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/06 09:54 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class CountdownEditText extends AppCompatEditText {

    public CountdownEditText(Context context) {
        this(context, null);
    }

    public CountdownEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public CountdownEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        density = context.getResources().getDisplayMetrics().density;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CountdownEditText, defStyle, 0);
        getAttributes(array);
        array.recycle();
        init();
    }

    private void getAttributes(TypedArray array) {
        textSize = array.getDimensionPixelSize(R.styleable.CountdownEditText_cet_counter_text_size, 0);
        if (0 == textSize) {
            textSize = Default.TextSize;
        }
        textColor = array.getColor(R.styleable.CountdownEditText_cet_counter_text_color, Default.TextColor);
        textFormat = array.getString(R.styleable.CountdownEditText_cet_counter_text_format);
        if (TextUtils.isEmpty(textFormat)) {
            textFormat = Default.TextFormat;
        }
        textMax = array.getInteger(R.styleable.CountdownEditText_cet_counter_max, 0);
    }

    private void init() {
        addTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String initial = s.toString();
            int len = initial.length();
            if (textMax > 0 && len > textMax) {
                initial = initial.substring(0, textMax);
                s.replace(0, len, initial);
            }
        }
    };

    private void drawCounter(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(textSize);
        int positionX = getWidth() - Default.TextPadding;
        int positionY = getHeight() - Default.TextPadding;
        int len = getText().length();
        String text;
        try {
            text = String.format(Locale.getDefault(), textFormat, len, textMax);
        } catch (Exception e) {
            text = String.format(Locale.getDefault(), Default.TextFormat, len, textMax);
        }
        canvas.drawText(text, positionX, positionY, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        int len = text.length();
        if (len > textMax) {
            setText(text.substring(0, textMax));
        }
        super.onDraw(canvas);
        drawCounter(canvas);
    }

    /**
     * 设置最大输入长度
     */
    public void setMaximumTextLength(int length) {
        textMax = length;
    }

    public void setCountTextColor(int color) {
        textColor = color;
    }

    public void setCountTextFormat(String format) {
        textFormat = format;
    }

    private int textSize, textColor, textMax;
    private float density;
    private String textFormat;

    private static class Default {
        public static final int TextPadding = Utility.ConvertDp(5);
        public static final int TextSize = Utility.ConvertDp(10);
        public static final int TextColor = Color.parseColor("#ff4081");
        public static final String TextFormat = "%d/%d";
    }
}

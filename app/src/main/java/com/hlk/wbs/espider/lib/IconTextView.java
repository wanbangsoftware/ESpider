package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.hlk.wbs.espider.R;

import java.util.Locale;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/03 20:56 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class IconTextView extends AppCompatTextView {

    public IconTextView(Context context) {
        this(context, null);
    }

    public IconTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public IconTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.IconTextView, defStyle, 0);
        getAttributes(array);
        array.recycle();
        init();
    }

    private void getAttributes(TypedArray array) {
        shape = array.getInteger(R.styleable.IconTextView_hlktv_background_shape, SHAPE_NONE);
        color = array.getColor(R.styleable.IconTextView_hlktv_background_color, Color.TRANSPARENT);
        font = array.getInteger(R.styleable.IconTextView_hlktv_font, MATERIAL_ICON);
    }

    private void init() {
        setFontFamily();
        setBackgroundShape();
    }

    /**
     * 获取背景图像
     */
    private Drawable getShapeDrawable() {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setShape(shape == SHAPE_OVAL ? new OvalShape() : new RectShape());
        drawable.getPaint().setColor(color);
        return drawable;
    }

    private String fontSource() {
        switch (font) {
            case FONT_AWESOME:
                return "fonts/fontawesome.ttf";
            case MATERIAL_ICON:
                return "fonts/material-icons-regular.ttf";
            case ICOMOON:
                return "fonts/icomoon.ttf";
            default:
                return "fonts/material-icons-regular.ttf";
        }
    }

    private void setFontFamily() {
        Typeface fontFamily = Typeface.createFromAsset(getContext().getAssets(), fontSource());
        setTypeface(fontFamily);
        invalidate();
    }

    private void setBackgroundShape() {
        if (shape > SHAPE_NONE) {
            if (Build.VERSION.SDK_INT >= 16) {
                setBackground(getShapeDrawable());
            } else {
                setBackgroundDrawable(getShapeDrawable());
            }
        }
    }

    /**
     * 手动设置背景形状
     */
    public void setBackgroundShape(int shape) {
        if (shape >= SHAPE_RECTANGLE && shape <= SHAPE_OVAL) {
            if (this.shape != shape) {
                this.shape = shape;
                setBackgroundShape();
            }
        } else {
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "Not supported shape: %d", shape));
        }
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundShapeColor(int color) {
        if (this.color != color) {
            this.color = color;
            setBackgroundShape();
        }
    }

    public void setFontSource(int font) {
        if (font >= FONT_AWESOME && font <= ICOMOON) {
            if (this.font != font) {
                this.font = font;
                setFontFamily();
            }
        } else {
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "Not supported font source: %d", font));
        }
    }

    private int shape, color, font;
    /**
     * 无形状背景
     */
    public static final int SHAPE_NONE = 0;
    /**
     * 圆形背景
     */
    public static final int SHAPE_OVAL = 1;
    /**
     * 方形背景
     */
    public static final int SHAPE_RECTANGLE = 2;

    /**
     * FontAwesome字体
     */
    public static final int FONT_AWESOME = 0;
    /**
     * MaterialIcon字体
     */
    public static final int MATERIAL_ICON = 1;
    public static final int ICOMOON = 2;
}

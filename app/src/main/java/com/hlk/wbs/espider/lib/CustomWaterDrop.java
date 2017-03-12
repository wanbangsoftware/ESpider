package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/11 12:26 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class CustomWaterDrop extends View {

    private int radius, color;

    public CustomWaterDrop(Context context) {
        this(context, null);
    }

    public CustomWaterDrop(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomWaterDrop(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomWaterDrop, defStyle, 0);
        getAttributes(a);
        a.recycle();
        initialize();
    }

    private void getAttributes(TypedArray array) {
        radius = array.getDimensionPixelOffset(R.styleable.CustomWaterDrop_cwd_radius, Default.Radius);
        color = array.getColor(R.styleable.CustomWaterDrop_cwd_color, Default.Background);
    }

    private void initialize() {

    }

    private Point getPoint(double angle) {
        return new Point((int) (radius + radius * Math.cos(angle)),
                (int) (radius + radius * Math.sin(angle)));
    }

    private void customDraw(Canvas canvas) {
        int width = getMeasuredWidth(), height = getMeasuredHeight();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);

        // 切边长度
        double a = radius, b, c = (radius + radius * 4 / 5);
        b = Math.sqrt(Math.pow(c, 2) - Math.pow(a, 2));
        double val = Math.acos((Math.pow(c, 2) + Math.pow(a, 2) - Math.pow(b, 2)) / (2 * c * a));
        double angle = Math.toDegrees(val);
        Point left = getPoint(Math.PI / 2 + val);
        Point right = getPoint(Math.PI / 2 - val);
        Path path = new Path();
        path.moveTo(left.x, left.y);
        path.lineTo(radius, height);
        path.lineTo(right.x, right.y);
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        customDraw(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 宽度为直径的长度，高度为直径+半径的2/3
        setMeasuredDimension(radius * 2, radius * 2 + radius * 4 / 5);
    }

    private static class Default {
        static final int Radius = Utility.ConvertDp(20);
        static final int Background = Color.parseColor("#FF8909");
    }
}

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

import java.util.Locale;

/**
 * 可以自定义形状的多边形
 * 作者：Hsiang Leekwok on 2015/09/04 17:39<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class CustomShape extends View {

    private static final String TAG = "CustomShape";
    /**
     * 默认边角数量
     */
    private static final int DEFAULT_CORNERS = 5;
    // 默认背景颜色
    private static final int DEFAULT_BACKGROUND = Color.parseColor("#f0f5f5");
    // 默认边框颜色
    private static final int DEFAULT_OUTER_BORDER_COLOR = Color.parseColor("#dbe2e5");
    // 默认引导线颜色
    private static final int DEFAULT_GUIDELINE_COLOR = Color.parseColor("#e5ebed");
    // 默认是否显示引导线
    private static final boolean DEFAULT_SHOW_GUIDELINE = true;
    // 默认半径长度
    private static final int DEFAULT_OUT_RADIUS = Utility.ConvertDp(100);
    // 边框粗细
    private static final int DEFAULT_OUT_BORDER_WIDTH = Utility.ConvertDp(1);
    // 引导线粗细
    private static final int DEFAULT_GUIDELINE_WIDTH = Utility.ConvertDp(1);
    // 默认前景颜色列表
    private static final int[] DEFAULT_FOREGROUND_COLORS = new int[]{
            Color.parseColor("#7ce1c6"),
            Color.parseColor("#68dcbe"),
            Color.parseColor("#50d7b4"),
            Color.parseColor("#4cd0ad"),
            Color.parseColor("#50d7b4"),
    };
    // 默认前景值列表
    private static final int[] DEFAULT_FORGROUND_VALUES = new int[]{60, 60, 60, 60, 60};
    // 默认边距大小
    private static final int DEFAULT_PADDING_SIZE = Utility.ConvertDp(5);

    // 动态设定内容************************************************
    /**
     * 边角数量
     */
    private int _corners = DEFAULT_CORNERS;
    /**
     * 背景颜色
     */
    private int _background = DEFAULT_BACKGROUND;
    /**
     * 外边框颜色
     */
    private int _outer_border_color = DEFAULT_OUTER_BORDER_COLOR;
    /**
     * 外边框粗细
     */
    private int _outer_border_width = DEFAULT_OUT_BORDER_WIDTH;
    /**
     * 引导线颜色
     */
    private int _guideline_color = DEFAULT_GUIDELINE_COLOR;
    /**
     * 引导线粗细
     */
    private int _guideline_width = DEFAULT_GUIDELINE_WIDTH;
    /**
     * 是否显示引导线
     */
    private boolean _show_guideline = DEFAULT_SHOW_GUIDELINE;
    /**
     * 默认前景色
     */
    private int[] _foreground_colors = DEFAULT_FOREGROUND_COLORS;
    /**
     * 默认值列表
     */
    private int[] _foreground_values = DEFAULT_FORGROUND_VALUES;
    /**
     * 默认边距大小
     */
    private int _padding_size = DEFAULT_PADDING_SIZE;

    // 动态计算的内容
    private int _outer_radius = DEFAULT_OUT_RADIUS, inner_radius = 0;
    private Point center;

    public CustomShape(Context context) {
        this(context, null);
    }

    public CustomShape(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomShape(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeSetting(context, attrs, defStyleAttr);
    }

    /**
     * 初始化设定
     */
    private void initializeSetting(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomShape, defStyleAttr, 0);
        _corners = a.getInteger(R.styleable.CustomShape_corners, DEFAULT_CORNERS);
        _background = a.getColor(R.styleable.CustomShape_background_color, DEFAULT_BACKGROUND);
        _outer_border_color = a.getColor(R.styleable.CustomShape_outer_border_color, DEFAULT_OUTER_BORDER_COLOR);
        _outer_border_width = a.getDimensionPixelOffset(R.styleable.CustomShape_outer_border_width, DEFAULT_OUT_BORDER_WIDTH);
        _guideline_color = a.getColor(R.styleable.CustomShape_guideline_color, DEFAULT_GUIDELINE_COLOR);
        _guideline_width = a.getDimensionPixelOffset(R.styleable.CustomShape_guideline_width, DEFAULT_GUIDELINE_WIDTH);
        _show_guideline = a.getBoolean(R.styleable.CustomShape_show_guideline, DEFAULT_SHOW_GUIDELINE);
        // 获取前景色列表
        int id = a.getResourceId(R.styleable.CustomShape_foreground_colors, 0);
        if (id != 0) {
            String[] colors = context.getResources().getStringArray(id);
            int len = colors.length;
            _foreground_colors = new int[len];
            for (int i = 0; i < len; i++) {
                _foreground_colors[i] = Color.parseColor(colors[i]);
            }
        }
        // 获取值列表
        id = a.getResourceId(R.styleable.CustomShape_foreground_values, 0);
        if (id != 0) {
            _foreground_values = context.getResources().getIntArray(id);
        }
        _padding_size = a.getDimensionPixelSize(R.styleable.CustomShape_padding_size, DEFAULT_PADDING_SIZE);
        a.recycle();

        if (_corners < 3) {
            throw new IllegalArgumentException("The minimize corner number is 3.");
        }

        if (_corners != _foreground_colors.length) {
            throw new IllegalArgumentException("Not enough foreground colors.");
        }

        if (_corners != _foreground_values.length) {
            throw new IllegalArgumentException("Not enough foreground values.");
        }
    }

    private void log(String string) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        customDraw(canvas);
        super.onDraw(canvas);
    }

    private void getCenter() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 中心点在宽度的1/2，高度
        center = new Point((int) (width / 2.0), (int) (height / 2.0));
        int min = width > height ? height : width;
        // 外围半径
        _outer_radius = min / 2 - _padding_size;
    }

    /**
     * 通过设定的边角数量计算每个角度的增量
     */
    private double getAngleIncremental() {
        return 2 * Math.PI / _corners;
    }

    /**
     * 获取各个顶点的角度列表
     */
    private double[] getAngles(double startAngle) {
        double[] angles = new double[_corners];
        angles[0] = startAngle;
        // 角度的增量
        double incremental = getAngleIncremental();
        for (int i = 1; i < _corners; i++) {
            angles[i] = angles[i - 1] + incremental;
        }
        return angles;
    }

    /**
     * 计算指定角度指定位置的点
     *
     * @param angle  角度
     * @param radius 半径
     */
    private Point getPoint(double angle, int radius) {
        return new Point((int) (center.x + radius * Math.cos(angle)),
                (int) (center.y + radius * Math.sin(angle)));
    }

    /**
     * 通过指定角度获取点的位置
     */
    private Point getPoint(double angle) {
        return new Point((int) (center.x + _outer_radius * Math.cos(angle)),
                (int) (center.y + _outer_radius * Math.sin(angle)));
    }

    private Point[] getCornerPoint(double[] angles) {
        Point[] points = new Point[_corners];
        for (int i = 0; i < _corners; i++) {
            points[i] = getPoint(angles[i]);
        }
        return points;
    }

    /**
     * 获取外边框
     */
    private Path getOuterBorder(Point[] points) {
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < _corners; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        path.close();
        return path;
    }

    /**
     * 画背景色
     */
    private void drawBackground(Point[] points, Canvas canvas) {
        Path border = getOuterBorder(points);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(_background);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(border, paint);
    }

    /**
     * 画外边框
     */
    private void drawOuterBorder(Point[] points, Canvas canvas) {
        Path border = getOuterBorder(points);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // 再画边框
        paint.setColor(_outer_border_color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(_outer_border_width);
        canvas.drawPath(border, paint);
    }

    /**
     * 画引导线
     */
    private void drawGuideline(Point[] points, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(_guideline_color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(_guideline_width);
        for (Point point : points) {
            canvas.drawLine(point.x, point.y, center.x, center.y, paint);
        }
    }

    /**
     * 各个顶点的平均值
     */
    private int getCornersAverage() {
        int total = 0;
        for (int i : _foreground_values) {
            total += Utility.ConvertDp(i);
        }
        return total / _foreground_values.length;
    }

    /**
     * 通过角度获取前景顶点列表
     */
    private Point[] getForegroundPoints(double[] angles) {
        Point[] points = new Point[_corners];
        for (int i = 0; i < _corners; i++) {
            // 按照比例计算前景色各个顶点的半径
            int value = _foreground_values[i];
            // 50的倍数当作最大外径长度  90,80,90,200,179
            int times = value / 50;
            int total = times * 50 + (value % 50 > 0 ? 50 : 0);
            if (value % 50 > 0) {
                total = times * 50 + 50;
            }
            double scale = _outer_radius * 1.0 / total;
            int r = (int) (scale * value);

            points[i] = getPoint(angles[i], r);
        }
        return points;
    }

    /**
     * 根据前景色的顶点画前景色块
     */
    private void drawForegrounds(Point[] points, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < _corners; i++) {
            Path path = new Path();
            path.moveTo(points[i].x, points[i].y);
            path.lineTo(center.x, center.y);
            if (i + 1 >= _corners) {
                path.lineTo(points[0].x, points[0].y);
            } else {
                path.lineTo(points[i + 1].x, points[i + 1].y);
            }
            path.close();
            paint.setColor(_foreground_colors[i]);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 画自定义的图形
     */
    private void customDraw(Canvas canvas) {
        // 计算中心点和半径
        getCenter();
        // 从正北为0开始获取所有角度
        double[] angles = getAngles(-Math.PI / 2);
        // 通过角度列表获取所有顶点的坐标
        Point[] points = getCornerPoint(angles);
        // 画背景
        drawBackground(points, canvas);
        // 画引导线
        if (_show_guideline) {
            drawGuideline(points, canvas);
        }
        // 画边框
        drawOuterBorder(points, canvas);
        // 计算前景的各个顶点
        Point[] foregrounds = getForegroundPoints(angles);
        // 画前景色
        drawForegrounds(foregrounds, canvas);
    }

    /**
     * 动态设置前景颜色
     */
    public void setForegroundColors(int[] colors) {
        if (colors.length < _corners) {
            throw new IllegalArgumentException(String.format(Locale.getDefault(),
                    "Not enough foreground colors, your corners is %d.", _corners));
        }
        _foreground_colors = new int[colors.length];
        System.arraycopy(colors, 0, _foreground_colors, 0, colors.length);

        // 重新画
        invalidate();
    }

    /**
     * 动态设置前景值列表
     */
    public void setForegroundValues(int[] values) {
        if (values.length < _corners) {
            throw new IllegalArgumentException(String.format(Locale.getDefault(),
                    "Not enough foreground values, your corners is %d.", _corners));
        }
        _foreground_values = new int[values.length];
        System.arraycopy(values, 0, _foreground_values, 0, values.length);

        invalidate();
    }
}

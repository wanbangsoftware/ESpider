package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.hlk.hlklib.etc.Utility;
import com.hlk.wbs.espider.R;

/**
 * <b>功能：</b>圆环动画<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/01/09 21:31 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class CircularCountdown extends View {

    // parameters
    private int radius, handler, duration, background, foreground, border, textColor,
            textSize, type, handlerColor, maximumTimes, display;
    private boolean auto;

    // Data
    private Paint bPaint, fPaint, tPaint, hPaint;
    private boolean isStarting = false;

    private long startTime;
    private long currentTime;
    private long progressMillisecond;
    private double progress;
    private float textOffset;
    private RectF circleBounds;

    public CircularCountdown(Context context) {
        this(context, null);
    }

    public CircularCountdown(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularCountdown(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularCountdown, defStyle, 0);
        getAttributes(a);
        a.recycle();
        initialize();
    }

    private void getAttributes(TypedArray array) {
        radius = array.getDimensionPixelOffset(R.styleable.CircularCountdown_cc_radius, Default.Radius);
        handler = array.getDimensionPixelOffset(R.styleable.CircularCountdown_cc_handlerRadius, Default.HandlerSize);
        duration = array.getInteger(R.styleable.CircularCountdown_cc_duration, Default.Duration);
        background = array.getColor(R.styleable.CircularCountdown_cc_background, Default.Background);
        foreground = array.getColor(R.styleable.CircularCountdown_cc_foreground, Default.Foreground);
        border = array.getDimensionPixelOffset(R.styleable.CircularCountdown_cc_borderWidth, Default.Border);
        textColor = array.getColor(R.styleable.CircularCountdown_cc_textColor, Default.TextColor);
        textSize = array.getDimensionPixelSize(R.styleable.CircularCountdown_cc_textSize, Default.TextSize);
        type = array.getInteger(R.styleable.CircularCountdown_cc_countdown, 0);
        auto = array.getBoolean(R.styleable.CircularCountdown_cc_autoStart, true);
        handlerColor = array.getColor(R.styleable.CircularCountdown_cc_handlerColor, Default.HandlerColor);
        maximumTimes = array.getInteger(R.styleable.CircularCountdown_cc_maximumCycleTimes, Default.MaximumCycleTimes);
        display = array.getInteger(R.styleable.CircularCountdown_cc_display, 0);
    }

    private void initialize() {
        circleBounds = new RectF();
        // the style of the background
        bPaint = new Paint();
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setAntiAlias(true);
        bPaint.setStrokeWidth(border);
        bPaint.setStrokeCap(Paint.Cap.SQUARE);
        bPaint.setColor(background);

        // the style of the 'progress'
        fPaint = new Paint();
        fPaint.setStyle(Paint.Style.STROKE);
        fPaint.setAntiAlias(true);
        fPaint.setStrokeWidth(border);
        fPaint.setStrokeCap(Paint.Cap.SQUARE);
        fPaint.setColor(foreground);

        // the style of the 'handler'
        hPaint = new Paint();
        hPaint.setStyle(Paint.Style.FILL);
        hPaint.setAntiAlias(true);
        hPaint.setColor(handlerColor);

        // the style for the text in the middle
        tPaint = new TextPaint();
        tPaint.setTextSize(textSize);
        tPaint.setColor(textColor);
        tPaint.setTextAlign(Paint.Align.CENTER);

        textOffset = (textSize / 2) - tPaint.descent();

        if (auto) {
            start();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = radius * 2 + handler * 2;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get the center of the view
        float centerWidth = getMeasuredWidth() / 2;
        float centerHeight = getMeasuredHeight() / 2;


        // set bound of our circle in the middle of the view
        circleBounds.set(centerWidth - radius, centerHeight - radius, centerWidth + radius, centerHeight + radius);


        // draw background circle
        canvas.drawCircle(centerWidth, centerHeight, radius, bPaint);

        // we want to start at -90°, 0° is pointing to the right
        canvas.drawArc(circleBounds, -90, (float) (progress * 360), false, fPaint);

        // draw handle or the circle
        if (0 == display || 2 == display) {
            canvas.drawCircle((float) (centerWidth + (Math.sin(progress * 2 * Math.PI) * radius)),
                    (float) (centerHeight - (Math.cos(progress * 2 * Math.PI) * radius)), handler, hPaint);
        }

        // display text inside the circle
        if (0 == display || 1 == display) {
            //String text = (double) (progressMillisecond / 100) / 10 + "s";
            String text = (int) (progressMillisecond / 1000) + "s";
            float tw = tPaint.measureText(text) / 2;
            float tx = (float) (centerWidth + (Math.sin(progress * 2 * Math.PI) * radius));
            float ty = (float) (centerHeight - (Math.cos(progress * 2 * Math.PI) * radius)) + textOffset;
            //canvas.drawText((double) (progressMillisecond / 100) / 10 + "s", centerWidth, centerHeight + textOffset, tPaint);
            canvas.drawText(text, tx, ty, tPaint);
        }
    }

    private final Handler viewHandler = new Handler();
    private final Runnable updateView = new Runnable() {

        @Override
        public void run() {
            // update current time
            currentTime = System.currentTimeMillis();

            // get elapsed time in milliseconds and clamp between <0, maxTime>
            progressMillisecond = (currentTime - startTime);

            // check the max cycle times
            if (progressMillisecond / duration > maximumTimes) {
                stop();
            } else {
                if (type == 1) {
                    progressMillisecond = progressMillisecond % duration;
                }

                // get current progress on a range <0, 1>
                progress = (double) (progressMillisecond % duration) / duration;

                CircularCountdown.this.invalidate();
            }
            checkStopping();
        }
    };

    private void checkStopping() {
        if (isStarting) {
            viewHandler.postDelayed(updateView, Default.Delay);
        }
    }

    public void start() {
        if (!isStarting) {
            // start and current time
            startTime = System.currentTimeMillis();
            currentTime = startTime;

            isStarting = true;
            viewHandler.post(updateView);
        }
    }

    public void pause() {
        if (isStarting) {
            isStarting = false;
        }
    }

    public void resume() {
        if (!isStarting) {
            isStarting = true;
            viewHandler.post(updateView);
        }
    }

    public void stop() {
        if (isStarting) {
            isStarting = false;
        }
    }

    private static class Default {
        static final int Radius = Utility.ConvertDp(100);
        static final int HandlerSize = Utility.ConvertDp(4);
        static final int Duration = 30 * 1000;
        static final int Background = Color.parseColor("#e7e7e7");
        static final int Foreground = Color.parseColor("#00A9FF");
        static final int HandlerColor = Color.parseColor("#00A9FF");
        static final int Border = Utility.ConvertDp(3);
        static final int TextColor = Color.parseColor("#e78720");
        static final int TextSize = Utility.ConvertDp(8);
        static final int Delay = 1000 / 60;
        static final int MaximumCycleTimes = 2;
        static final int HandlerBorder = Utility.ConvertDp(1);
    }
}

package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.helpers.StringHelper;

/**
 * <b>功能</b>：自定义TextView<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/03 00:39 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class EverdigmIcon extends IconTextView {

    public EverdigmIcon(Context context) {
        this(context, null);
    }

    public EverdigmIcon(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public EverdigmIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontSource(IconTextView.ICOMOON);
        if (isInEditMode()) {
            setText("\ued0f");
        } else {
            setText(StringHelper.getString(R.string.ui_base_text_everdigm_icon_icon));
        }
        setCustomColor();
    }

    private void setCustomColor() {
        int[] color = {ContextCompat.getColor(getContext(), R.color.color_aed24c),
                ContextCompat.getColor(getContext(), R.color.color_00a8a8)};
        float[] position = {0, 1};
        /*  参数说明：
            参数x0：表示渐变的起始点x坐标；
            参数y0：表示渐变的起始点y坐标；
            参数x1：表示渐变的终点x坐标；
            参数y1：表示渐变的终点y坐标；
            参数colors：表示渐变的颜色数组；
            参数positions：用来指定颜色数组的相对位置；
            参数color0：表示渐变开始颜色；
            参数color1：表示渐变结束颜色；
            参数tile：表示平铺方式

            Shader.TileMode有3种参数可供选择，分别为CLAMP、REPEAT和MIRROR：
            CLAMP的作用：是如果渲染器超出原始边界范围，则会复制边缘颜色对超出范围的区域进行着色
            REPEAT的作用是：在横向和纵向上以平铺的形式重复渲染位图
            MIRROR的作用是：在横向和纵向上以镜像的方式重复渲染位图
        */
        // 创建LinearGradient并设置渐变颜色数组
        // 第一个,第二个参数表示渐变起点 可以设置起点终点在对角等任意位置
        // 第三个,第四个参数表示渐变终点
        // 第五个参数表示渐变颜色
        // 第六个参数可以为空,表示坐标,值为0-1 new float[] {0.25f, 0.5f, 0.75f, 1 }
        // 如果这是空的，颜色均匀分布，沿梯度线。
        // 第七个表示平铺方式
        // CLAMP重复最后一个颜色至最后
        // MIRROR重复着色的图像水平或垂直方向已镜像方式填充会有翻转效果
        // REPEAT重复着色的图像水平或垂直方向
        double angleInRadians = Math.toRadians(45);
        Paint paint = getPaint();
        float size = getTextSize();
        float endX = (float) (Math.cos(angleInRadians) * size);
        float endY = (float) (Math.sin(angleInRadians) * size);
        Shader textShader = new LinearGradient(0, 0, endX, size, color, position, Shader.TileMode.CLAMP);
        paint.setShader(textShader);
        //Matrix trans = new Matrix();
        //trans.setRotate(90);
        //textShader.setLocalMatrix(trans);
    }
}

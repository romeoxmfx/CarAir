
package com.android.carair.views;

import com.android.carair.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.print.PrintAttributes.Margins;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 * 
 * @author xiaanming
 */
public class RoundProgressBar extends View {
    /**
     * 画笔对象的引用
     */
    private Paint paint;

    private Paint paintProgress;

    /**
     * 圆环的颜色
     */
    private int roundColor;

    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    /**
     * 圆环的宽度
     */
    private float roundOuterWidth;

    private float roundInnerWidth;

    /**
     * 最大进度
     */
    private int max;

    /**
     * 当前进度
     */
    private int progress;
    /**
     * 是否显示中间的进度
     */
    private boolean textIsDisplayable;

    /**
     * 进度的风格，实心或者空心
     */
    private int style;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();

        paintProgress = new Paint();

        // paintProgress = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        // 获取自定义属性和默认值
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor,
                Color.GREEN);
        textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundTextColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundTextSize, 15);
        roundOuterWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundOuterWidth, 5);
        roundInnerWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundInnerWidth, 5);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable,
                true);
        style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画最外层的大圆环
         */
        int centre = getWidth() / 2; // 获取圆心的x坐标
        // int radius = (int) (centre - roundOuterWidth / 2); // 圆环的半径
        int radius = (int) (centre - roundInnerWidth / 2); // 圆环的半径
        paint.setColor(roundColor); // 设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setStrokeWidth(roundOuterWidth); // 设置圆环的宽度
        paint.setAntiAlias(true); // 消除锯齿
        // canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
        RectF ovalOut = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限
        canvas.drawArc(ovalOut, 134, 270, false, paint);

        Log.e("log", centre + "");

        /**
         * 画进度百分比
         */
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
        // int percent = (int) (((float) progress / (float) max) * 100); //
        // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
        int percent = progress; // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
        float textWidth = paint.measureText(percent + ""); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

        if (textIsDisplayable && percent != 0 && style == STROKE) {
            canvas.drawText(percent + "", centre - textWidth / 2, centre + textSize / 2, paint); // 画出进度百分比
        }

        /**
         * 画圆弧 ，画圆环的进度
         */

        // 设置进度是实心还是空心
        paintProgress.setStyle(Paint.Style.STROKE); // 设置空心
        paintProgress.setAntiAlias(true); // 消除锯齿
        paintProgress.setStrokeWidth(roundInnerWidth);
        int radiusInner = (int) (centre - roundInnerWidth / 2); // 圆环的半径
        // paint.setStrokeWidth(roundInnerWidth); // 设置圆环的宽度
        // paint.setColor(roundProgressColor); // 设置进度的颜色
        SweepGradient sg = new SweepGradient(centre, getHeight() / 2, new int[] {
                Color.rgb(0x9b, 0xec, 0xfa), Color.rgb(0xcc, 0xe8, 0xef),
                Color.rgb(0xfd, 0x75, 0x75), Color.rgb(0xc8, 0x35, 0x58)
        }, null);
        Matrix mMatrix = new Matrix();
        mMatrix.setRotate(134, centre, getHeight() / 2);
        sg.setLocalMatrix(mMatrix);
        paintProgress.setShader(sg);
        RectF oval = new RectF(centre - radiusInner, centre - radiusInner, centre
                + radiusInner, centre + radiusInner); // 用于定义的圆弧的形状和大小的界限

        switch (style) {
            case STROKE: {
                paint.setStyle(Paint.Style.STROKE);
                // canvas.drawArc(oval, 90, 360 * progress / max, false, paint);
                // // 根据进度画圆弧
                canvas.drawArc(oval, 134, 270 * progress / max, false, paintProgress); // 根据进度画圆弧
                break;
            }
            case FILL: {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0)
                    canvas.drawArc(oval, 90, 360 * progress / max, true, paint); // 根据进度画圆弧
                break;
            }
        }

    }

    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     * 
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     * 
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     * 
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getRoundOuterWidth() {
        return roundOuterWidth;
    }

    public void setRoundOuterWidth(float roundOuterWidth) {
        this.roundOuterWidth = roundOuterWidth;
    }

    public float getRoundInnerWidth() {
        return roundInnerWidth;
    }

    public void setRoundInnerWidth(float roundInnerWidth) {
        this.roundInnerWidth = roundInnerWidth;
    }

}

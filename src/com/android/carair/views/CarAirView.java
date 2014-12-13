
package com.android.carair.views;

import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class CarAirView extends LinearLayout {
    private ChartClickListener mChartClickListener;
    private ChartViewTouchListener mChartViewTouchListener;

    public CarAirView(Context context) {
        super(context);
        this.context = context;
    }

    public CarAirView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setCarAirView(List<double[]> x, List<double[]> values, double xMin, double xMax,
            double yMin, double yMax, String xtitle, String ytitle) {
        // invalidate();
        // this.graphicalView = new
        // CarAirChart(x,values,xMin,xMax,yMin,yMax).execute(context);
        if (graphicalView != null) {
            this.removeView(graphicalView);
        }
        setGraphicalView(new CarAirChart(x, values, xMin, xMax, yMin, yMax, xtitle, ytitle)
                .execute(context));
        this.addView(graphicalView, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));

        // graphicalView.invalidate();
        invalidate();
        // graphicalView.refreshDrawableState();
        // graphicalView.repaint();
        this.graphicalView.setOnClickListener(new ChartViewClick());
        this.graphicalView.setOnTouchListener(new ChartViewTouch());
    }

    private GraphicalView graphicalView;
    Context context;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // this.graphicalView.draw(canvas);
    }

    class ChartViewClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 获取当前点击点
            SeriesSelection seriesSelection = graphicalView.getCurrentSeriesAndPoint();

            if (seriesSelection == null) {
                return;
            }
            // int x = (int) seriesSelection.getXValue();
            // double value = seriesSelection.getValue();
            if (mChartClickListener != null) {
                mChartClickListener.onClick(seriesSelection);
            }
        }
    }
    
    class ChartViewTouch implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            SeriesSelection seriesSelection = graphicalView.getCurrentSeriesAndPoint();
            if (seriesSelection != null) {
                if (mChartViewTouchListener != null) {
                    mChartViewTouchListener.onTouch(seriesSelection, event);
                }
            }
            return false;
        }

    }

    public interface ChartViewTouchListener {
        public void onTouch(SeriesSelection seriesSelection, MotionEvent event);
    }

    public void setGraphicalView(GraphicalView graphicalView) {
        this.graphicalView = graphicalView;
    }

    public ChartClickListener getmChartClickListener() {
        return mChartClickListener;
    }

    public void setmChartClickListener(ChartClickListener mChartClickListener) {
        this.mChartClickListener = mChartClickListener;
    }

    public interface ChartClickListener {
        public void onClick(SeriesSelection seriesSelection);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public ChartViewTouchListener getmChartViewTouchListener() {
        return mChartViewTouchListener;
    }

    public void setmChartViewTouchListener(ChartViewTouchListener mChartViewTouchListener) {
        this.mChartViewTouchListener = mChartViewTouchListener;
    }
}

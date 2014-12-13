/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.carair.views;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Average temperature demo chart.
 */
public class CarAirChart extends AbstractChart {

	List<double[]> x = new ArrayList<double[]>();
	List<double[]> values = new ArrayList<double[]>();
	double xMin,xMax,yMin,yMax;
	String xtitle,ytitle;

	

	public CarAirChart(List<double[]> x, List<double[]> values, double xMin,double xMax, double yMin, double yMax,String xtitle,String ytitle) {
		super();
		this.x = x;
		this.values = values;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.xtitle = xtitle;
		this.ytitle = ytitle;
	}

	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	public String getName() {
		return "Average temperature";
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return "The average temperature in 4 Greek islands (line chart)";
	}
	

	/**
	 * Executes the chart
	 * 
	 * @param context
	 *           
	 * @return the built intent
	 */
	public GraphicalView execute(Context context) {
		String[] titles = new String[] { "test1", "test2"};
		int[] colors = new int[] { Color.WHITE,Color.rgb(0x52, 0x4b, 0x79) };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE,PointStyle.CIRCLE};
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(false);
		}
		setChartSettings(renderer, null, xtitle, ytitle, xMin, xMax,yMin, yMax, Color.WHITE, Color.WHITE);
		renderer.setZoomEnabled(true);
		renderer.setPanEnabled(true);
		renderer.setPointSize(9f);
		renderer.setXLabels(12);
		renderer.setYLabels(12);
		renderer.setClickEnabled(true);
		renderer.setSelectableBuffer(30);//点击区域大小  
		renderer.setLabelsTextSize(18);
		renderer.setShowGrid(true);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setShowLegend(false);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomButtonsVisible(false);
		renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
		renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

		renderer.setGridColor(Color.WHITE);
		renderer.setXLabelsColor(Color.WHITE);
		renderer.setYLabelsColor(0, Color.WHITE);

		// 设置背景颜色
		renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0)); // 不能用transparent代替
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setApplyBackgroundColor(true);// 使背景颜色生效

		XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
//		XYSeries series = dataset.getSeriesAt(0);
//		series.addAnnotation("Vacation", 6, 30);
		// Intent intent = ChartFactory.getLineChartIntent(context, dataset,
		// renderer,"Average temperature");
		GraphicalView graphicalView = ChartFactory.getLineChartView(context,dataset, renderer);
		return graphicalView;
	}

}

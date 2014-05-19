package com.android.carair.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.views.MyChartView;
import com.android.carair.views.MyChartView.Mstyle;
import com.android.carair.views.Tools;

public class HistoryFragment extends BaseFragment{

    private Bundle saveInstanceState;
    MyChartView tu;
    HashMap<Double, Double> map;
	Double key=8.0;
	Double value=0.0;
	Tools tool=new Tools();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_history, null);
		
		tu = (MyChartView) mMainView.findViewById(R.id.trendview);
		tu.SetTuView(map,50,10,"x","y",true);
		map=new HashMap<Double, Double>();
		map.put(1.0, (double) 0);
    	map.put(3.0, 25.0);
    	map.put(4.0, 32.0);
    	map.put(5.0, 41.0);
    	map.put(6.0, 16.0);
    	map.put(7.0, 36.0);
    	map.put(8.0, 26.0);
    	tu.setTotalvalue(50);
    	tu.setPjvalue(10);
    	tu.setMap(map);
//		tu.setXstr("");
//		tu.setYstr("");
		tu.setMargint(20);
		tu.setMarginb(50);
		tu.setMstyle(Mstyle.Line);
		return mMainView;
	}
	
}

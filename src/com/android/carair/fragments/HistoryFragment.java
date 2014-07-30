
package com.android.carair.fragments;

import java.util.Iterator;
import java.util.TreeMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.Log;
import com.android.carair.utils.SerializableMap;
import com.android.carair.views.MyChartView;
import com.android.carair.views.MyChartView.Mstyle;
import com.android.carair.views.Tools;

public class HistoryFragment extends BaseFragment {

    MyChartView tu;
    TreeMap<Double, Double> map;
    TreeMap<Double, Double> mapOut;
    Double key = 8.0;
    Double value = 0.0;
    Tools tool = new Tools();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.i("hisoncreateview");
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_history, null);
        if (getArguments().containsKey("map")) {
            SerializableMap amap = (SerializableMap) getArguments().get("map");
            map = (TreeMap<Double, Double>) amap.getMap();
        }
        if (getArguments().containsKey("mapOut")) {
            SerializableMap amap = (SerializableMap) getArguments().get("mapOut");
            mapOut = (TreeMap<Double, Double>) amap.getMap();
        }

        tu = (MyChartView) mMainView.findViewById(R.id.trendview);
        // 计算最大值和平均值

        Iterator keys = mapOut.keySet().iterator();
        double totleV = 0;
        double maxV = -1;
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = mapOut.get(key);
            totleV += value;
            if (value > maxV) {
                maxV = value;
            }
        }

        int av = (int) (totleV / map.size());
        
        double totleVOut = 0;
        double maxVOut = -1;
        int avOut = 0;
//        if(maxV == 0){
            keys = map.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                double value = map.get(key);
                totleVOut += value;
                if (value > maxVOut) {
                    maxVOut = value;
                }
//            }

            avOut = (int) (totleVOut / map.size());
        }

        double max;
        int average;
        if(maxV > maxVOut){
            max = maxV;
        }else{
            max = maxVOut;
        }
        
        if(av > avOut){
            average = av;
        }else{
            average = avOut;
        }
        tu.SetTuView(map, mapOut, (int)max, average, "h", "pm", false);
        // map=new HashMap<Double, Double>();
        // map.put(1.0, (double) 0);
        // map.put(3.0, 25.0);
        // map.put(4.0, 32.0);
        // map.put(5.0, 41.0);
        // map.put(6.0, 16.0);
        // map.put(7.0, 36.0);
        // map.put(8.0, 26.0);
        // tu.setTotalvalue(50);
        // tu.setPjvalue(10);
        tu.setMap(map);
        // tu.setXstr("");
        // tu.setYstr("");
        tu.setMargint(20);
        tu.setMarginb(50);
        tu.setMstyle(Mstyle.Line);
        return mMainView;
    }

}


package com.android.carair.fragments;

import java.util.Iterator;
import java.util.TreeMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

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
    RadioGroup mRadioGroup;
    RadioButton rbPm;
    RadioButton rbTemp;
    TextView currentValueTitle;
    TextView currentIn;
    TextView currentOut;
    TreeMap<Double, Double> map;
    TreeMap<Double, Double> mapOut;
    TreeMap<Double, Double> mapTemp;
    TreeMap<Double, Double> mapOTemp;
    Double key = 8.0;
    Double value = 0.0;
    Tools tool = new Tools();
    int outPm = 0;
    int oTemp = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.i("hisoncreateview");
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_history, null);
        currentValueTitle  = (TextView) mMainView.findViewById(R.id.tvCurrentValueTitle);
        currentIn = (TextView) mMainView.findViewById(R.id.inCarValue);
        currentOut = (TextView) mMainView.findViewById(R.id.outCarValue);
        mRadioGroup = (RadioGroup) mMainView.findViewById(R.id.rbgroup);
        rbPm = (RadioButton) mMainView.findViewById(R.id.rbPm);
        rbTemp = (RadioButton) mMainView.findViewById(R.id.rbTem);
        rbPm.setChecked(true);
        currentIn.setText(HomeFragment.pmIn+"");
        currentValueTitle.setText("当前颗粒物");
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbPm.getId()) {
                    int pmIn = HomeFragment.pmIn;
                    if(pmIn <= 0){
                        pmIn = 0;
                    }
                    currentIn.setText(pmIn+"");
                    currentValueTitle.setText("当前颗粒物");
                    setPM();
                } else if (checkedId == rbTemp.getId()) {
                    int tempIn = HomeFragment.temIn;
                    if(tempIn <= 0){
                        tempIn = 0;
                    }
                    currentIn.setText(tempIn+"");
                    currentValueTitle.setText("当前温度(°C)");
                    setTemp();
                }
            }
        });
        if (getArguments().containsKey("map")) {
            SerializableMap amap = (SerializableMap) getArguments().get("map");
            map = (TreeMap<Double, Double>) amap.getMap();
        }
        if (getArguments().containsKey("mapOut")) {
            SerializableMap amap = (SerializableMap) getArguments().get("mapOut");
            mapOut = (TreeMap<Double, Double>) amap.getMap();
        }
        if(getArguments().containsKey("mapTemp")){
            SerializableMap amap = (SerializableMap) getArguments().get("mapTemp");
            mapTemp = (TreeMap<Double, Double>) amap.getMap();
        }
        if(getArguments().containsKey("mapOtemp")){
            SerializableMap amap = (SerializableMap) getArguments().get("mapOtemp");
            mapOTemp = (TreeMap<Double, Double>) amap.getMap();
        }

        tu = (MyChartView) mMainView.findViewById(R.id.trendview);
        // 计算最大值和平均值

        setPM();
        return mMainView;
    }
    
    private void setPM(){
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
        outPm = new Double(maxV).intValue();
        currentOut.setText(outPm+"");
        
        int av = (int) (totleV / map.size());

        double totleVOut = 0;
        double maxVOut = -1;
        int avOut = 0;
        // if(maxV == 0){
        keys = map.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = map.get(key);
            Log.i("value = " + value);
            totleVOut += value;
            if (value > maxVOut) {
                maxVOut = value;
            }
            // }

            avOut = (int) (totleVOut / map.size());
        }

        double max;
        int average;
        if (maxV > maxVOut) {
            max = maxV;
        } else {
            max = maxVOut;
        }

        if (av > avOut) {
            average = av;
        } else {
            average = avOut;
        }
        tu.SetTuView(map, mapOut, (int) max, average, "h", "pm", true);
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
//        tu.setMap(map);
        // tu.setXstr("");
        // tu.setYstr("");
        tu.setMargint(20);
        tu.setMarginb(80);
        tu.setMstyle(Mstyle.Curve);
        tu.invalidate();
    }
    
    private void setTemp(){
        Iterator keys = mapOTemp.keySet().iterator();
        double totleV = 0;
        double maxV = -1;
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = mapOTemp.get(key);
            totleV += value;
            if (value > maxV) {
                maxV = value;
            }
        }
        oTemp = new Double(maxV).intValue();
        currentOut.setText(oTemp+"");

        int av = (int) (totleV / mapTemp.size());

        double totleVOut = 0;
        double maxVOut = -1;
        int avOut = 0;
        // if(maxV == 0){
        keys = mapTemp.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = mapTemp.get(key);
            totleVOut += value;
            if (value > maxVOut) {
                maxVOut = value;
            }
            // }

            avOut = (int) (totleVOut / mapTemp.size());
        }

        double max;
        int average;
        if (maxV > maxVOut) {
            max = maxV;
        } else {
            max = maxVOut;
        }

        if (av > avOut) {
            average = av;
        } else {
            average = avOut;
        }
        tu.SetTuView(mapTemp, mapOTemp, (int) max, average, "h", "°C", true);
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
//        tu.setMap(mapTemp);
        // tu.setXstr("");
        // tu.setYstr("");
        tu.setMargint(20);
        tu.setMarginb(80);
        tu.setMstyle(Mstyle.Curve);
        tu.invalidate();
    }

}

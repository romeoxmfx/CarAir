
package com.android.carair.fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.achartengine.model.SeriesSelection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.Log;
import com.android.carair.utils.SerializableMap;
import com.android.carair.views.CarAirView;
import com.android.carair.views.CarAirView.ChartClickListener;
import com.android.carair.views.MyChartView;
import com.android.carair.views.Tools;
import com.android.goodhelpercarair.R;

public class HistoryFragment extends BaseFragment {

    MyChartView chartView;
    CarAirView carAirView ;
    RadioGroup mRadioGroup;
    RadioButton rbPm,rbTemperature;
    TextView currentValueTitle,currentIn,currentOut;
    TreeMap<Double, Double> pmMap,pmOutMap, temperatureMap, temperatureOutMap;
    
    Double key = 8.0;
    Double value = 0.0;
    Tools tool = new Tools();
    int pmOut = 0;
    int temperatureOut = 0;
    int in,out;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.i("hisoncreateview");
        mMainView = (FragmentViewBase) inflater.inflate( R.layout.carair_fragment_history, null);
        currentValueTitle  = (TextView) mMainView.findViewById(R.id.tvCurrentValueTitle);
        currentIn = (TextView) mMainView.findViewById(R.id.inCarValue);
        currentOut = (TextView) mMainView.findViewById(R.id.outCarValue);
        mRadioGroup = (RadioGroup) mMainView.findViewById(R.id.rbgroup);
        rbPm = (RadioButton) mMainView.findViewById(R.id.rbPm);
        rbTemperature = (RadioButton) mMainView.findViewById(R.id.rbTem);
        
        if (pmMap==null && getArguments().containsKey("map")) {
            SerializableMap amap = (SerializableMap) getArguments().get("map");
            pmMap = (TreeMap<Double, Double>) amap.getMap();
        }
        if (pmOutMap==null && getArguments().containsKey("mapOut")) {
            SerializableMap amap = (SerializableMap) getArguments().get("mapOut");
            pmOutMap = (TreeMap<Double, Double>) amap.getMap();
        }
        if(temperatureMap==null && getArguments().containsKey("mapTemp")){
            SerializableMap amap = (SerializableMap) getArguments().get("mapTemp");
            temperatureMap = (TreeMap<Double, Double>) amap.getMap();
        }
        if(temperatureOutMap==null && getArguments().containsKey("mapOtemp")){
            SerializableMap amap = (SerializableMap) getArguments().get("mapOtemp");
            temperatureOutMap = (TreeMap<Double, Double>) amap.getMap();
        }
        
        //当前默认选项
        rbPm.setChecked(true);
        currentValueTitle.setText("当前颗粒物");
        currentIn.setText(Tools.getVaule(HomeFragment.pmIn)+"");
        currentOut.setText(Tools.getVaule(HomeFragment.pmOut)+"");
        //chartView = (MyChartView) mMainView.findViewById(R.id.trendview);
        carAirView = (CarAirView) mMainView.findViewById(R.id.trendview);
        setPM();
        //carAirView.setCarAirView();
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbPm.getId()) {
                    int pmIn = Tools.getVaule(HomeFragment.pmIn);
                    currentValueTitle.setText("当前颗粒物");
                    currentIn.setText(pmIn+"");
                    setPM();
                } else if (checkedId == rbTemperature.getId()) {
                    int temperatureIn = Tools.getVaule(HomeFragment.temIn);
                    currentValueTitle.setText("当前温度(°C)");
                    currentIn.setText(temperatureIn+"");
                    setTemperature();
                }
            }
        });
        
        carAirView.setmChartClickListener(new ChartClickListener() {
            
            @Override
            public void onClick(SeriesSelection seriesSelection) {
                String value = (int)seriesSelection.getXValue() + "时点击数值为: " + (int)seriesSelection.getValue();
                Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
            }
        });
        
//        carAirView.setmChartViewTouchListener(new ChartViewTouchListener() {
//            
//            @Override
//            public void onTouch(SeriesSelection seriesSelection, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    Toast.makeText(getActivity(), seriesSelection.getValue()+"", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        return mMainView;
    }
    
    private void setPM(){
       set(pmMap, pmOutMap, "h", "pm");
    }
    
    private void setTemperature(){
       set(temperatureMap, temperatureOutMap, "h", "°C");
    }
    
    private void set(TreeMap<Double, Double> inMap,TreeMap<Double, Double> outMap,String xCoordinate,String ycoordinate) {
    	//车内
        double totleIn = 0;
        double maxIn = -1;
        int averageIn =0;
        Iterator<Double> keys = inMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = inMap.get(key);
            totleIn += value;
            if (value > maxIn) {
            	maxIn = value;
            }
        }
        Double lastKeyIn = inMap.lastKey();
        //TODO 当前值计算有误
        in = Double.valueOf(inMap.get(lastKeyIn)).intValue();
        currentIn.setText(in+"");
//        averageIn = (int) (totleIn / inMap.size());
        
        //车外
        double totleOut = 0;
        double maxOut = -1;
        int averageOut = 0;
        keys = outMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            double value = outMap.get(key);
            totleOut += value;
            if (value > maxOut) {
                maxOut = value;
            }
        }
        //TODO 当前值计算有误
        Double lastKeyOut = outMap.lastKey();
        out = Double.valueOf(outMap.get(lastKeyOut)).intValue();
        currentOut.setText(out+"");
//        averageOut = (int) (totleOut / outMap.size());

        double max = maxIn >= maxOut? maxIn:maxOut;
//        int average = averageIn>= averageOut? averageIn :averageOut;

        //chartView.SetChartView(inMap, outMap, Double.valueOf(max).intValue(), average, xCoordinate, ycoordinate, true);
        
        Set<Double> xSet = inMap.keySet();
        double[] xArray = new double[xSet.size()];
        int i=0;
        for(Double x:inMap.keySet()){
            String sx = x.toString();
            try {
              if(sx.length() > 4){
                  sx = sx.substring(4);
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
            xArray[i++]= Double.parseDouble(sx);
        }
        
        Set<Double> xSetOut = outMap.keySet();
        double[] xArrayOut = new double[xSetOut.size()];
        int j=0;
        for(Double x:outMap.keySet()){
            String sx = x.toString();
            try {
                if(sx.length() > 4){
                    sx = sx.substring(4);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            xArrayOut[j++]= Double.parseDouble(sx);
        }
        
        List<double[]> x = new ArrayList<double[]>();
        x.add(xArray);
        x.add(xArrayOut);
        
        Collection<Double> inCollection = inMap.values();
        double[] inArray = new double[inCollection.size()];
        i=0;
        for(Double y:inCollection){
        	inArray[i++]=y;
        }
        
        Collection<Double> outCollection = outMap.values();
        double[] outArray = new double[outCollection.size()];
        i=0;
        for(Double y:outCollection){
        	outArray[i++]=y;
        }
       
        List<double[]> values = new ArrayList<double[]>();
        values.add(inArray);
        values.add(outArray);
        
    	double xMin=xArray[0],xMax=xArray[xArray.length-1],yMin=0,yMax=max;
    	
        carAirView.setCarAirView(x,values,xMin,xMax,yMin,yMax*2,xCoordinate,ycoordinate);
	}

}

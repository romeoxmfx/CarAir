
package com.android.airhelper.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.airhelper.api.CarAirReqTask;
import com.android.airhelper.api.RespProtocolPacket;
import com.android.airhelper.fragments.HistoryFragment;
import com.android.carair.R;
import com.android.carair.airhelper.base.FragmentPageManager;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.SerializableMap;
import com.umeng.analytics.MobclickAgent;

public class HistoryActivity extends SherlockFragmentActivity {
    int type;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_container_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("趋势");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        if (getIntent() != null && getIntent().hasExtra("type")) {
            type = getIntent().getIntExtra("type", 0);
        }
        pb = (ProgressBar) findViewById(R.id.pb);
        // FragmentPageManager.getInstance().setFragmentManager(this.getSupportFragmentManager());
        // FragmentPageManager.getInstance().pushPageById(new HistoryFragment(),
        // HistoryFragment.class.getName(), R.id.fragment_container);
        requestHistory();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private SerializableMap getMap(String pm25, int dispnum) {
        SerializableMap myMap = new SerializableMap();
        TreeMap<Double, Double> map = new TreeMap<Double, Double>();

        if (!TextUtils.isEmpty(pm25)) {

            // 拆分数据
            String[] items = pm25.split(",");
            String[] keys = new String[items.length];
            String[] values = new String[items.length];

            Pattern pattern = Pattern.compile("[0-9]*");
            for (int i = 0; i < items.length; i++) {
                keys[i] = items[i].substring(0, items[i].indexOf(":"));
                values[i] = items[i].substring(items[i].indexOf(":") + 1,
                        items[i].length());
                if (!pattern.matcher(values[i]).matches()) {
                    values[i] = values[i].substring(values[i].indexOf(":") + 1);
                }
//                try {
//                    if(keys[i].length() > 4){
//                        keys[i] = keys[i].substring(4);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                double key = Double.parseDouble(keys[i]);
                double value = Double.parseDouble(values[i]);
                map.put(key, value);
            }
            //获取当前时间
            final Calendar c = Calendar.getInstance(); 
            int hour = c.get(Calendar.HOUR_OF_DAY);//获取当前的小时数 
            int month = c.get(Calendar.MONTH) + 1;//获取当前月份 
            int day = c.get(Calendar.DAY_OF_MONTH);//获取当前天
            String shour = Integer.toString(hour);
            String smonth = Integer.toString(month);
            String sday = Integer.toString(day);
            if(hour < 10){
                shour = "0" + hour;
            }
            if(month < 10){
                smonth = "0" + month;
            }
            if(day < 10){
                sday = "0" + day;
            }
            //拼装一个日期
            String today = smonth + sday + shour;
            Double dtoday = null;
            try {
                dtoday = Double.parseDouble(today);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Set<Double> s = map.keySet();
//            TreeSet<Double> sets = new TreeSet<Double>(s);
            ArrayList<Double> list = new ArrayList<Double>();
            list.addAll(s);
            Collections.sort(list);
//            Iterator keysets = sets.iterator();
            int start = 0;
            if(list.contains(dtoday)){
                //计算起始坐标
                int index = list.indexOf(dtoday);
                int size = list.size();
                if(size - index + 1 > dispnum){
                    start = index;
                }else{
                    start = index + (size - index - dispnum);
                }
            }else{
             // 截取dispnum个最近的数据
                start = map.size() - dispnum - 1;
                if (start <= 0) {
                    start = 0;
                }
            }
//            int i = 0;
            TreeMap<Double, Double> newMap = new TreeMap<Double, Double>();
//            while (keysets.hasNext()) {
//                Double key = (Double) keysets.next();
//                if (i >= start) {
//                    newMap.put(key, map.get(key));
//                }
//                i++;
//            }
            for (int i = start; i < start + dispnum; i++) {
                Double key = list.get(i);
                newMap.put(key, map.get(key));
            }
            myMap.setMap(newMap);
        }
        return myMap;
    }

    private void requestHistory() {
        new CarAirReqTask() {
            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    if (packet != null) {
                        int dispnum = packet.getRespMessage().getAppinfo().getDispnum();
                        String pm25 = packet.getRespMessage().getDevinfo().getPm25();
                        // pm25 =
                        // "062700:0,062701:0,062702:0,062703:0,062704:0,062705:0,062706:0,062707:0,062708:0,062709:0,062710:0,062711:0,062712:0,062713:0,062714:0,062715:0,062716:0,062717:0,062718:0,062719:0,062720:0,062721:0,062722:0,062723:0062800:0,062801:0,062802:0,062803:0,062804:0,062805:0,062806:0,062807:0,062808:0,062809:0,062810:0,062811:0,062812:0,062813:0,062814:0,062815:0,062816:0";
                        String pm25Out = packet.getRespMessage().getAir().getOpm25();
//                        String harmful = packet.getRespMessage().getAir().getHarmair();
                        
                        String temp = packet.getRespMessage().getDevinfo().getTemp();
                        String otemp = packet.getRespMessage().getAir().getOtemp();
                        SerializableMap myMap = getMap(pm25, dispnum);
                        SerializableMap myMapOut = getMap(pm25Out, dispnum);
                        SerializableMap myMapTemp = getMap(temp, dispnum);
                        SerializableMap myMapOTemp = getMap(otemp, dispnum);
                        
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("map", myMap);
                        bundle.putSerializable("mapOut", myMapOut);
                        bundle.putSerializable("mapTemp", myMapTemp);
                        bundle.putSerializable("mapOtemp", myMapOTemp);

                        // 打开fragment
                        FragmentPageManager.getInstance().setFragmentManager(
                                HistoryActivity.this.getSupportFragmentManager());
                        FragmentPageManager.getInstance().pushPageById(new HistoryFragment(),
                                HistoryFragment.class.getName(), R.id.fragment_container,
                                false, bundle);
                        pb.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(HistoryActivity.this, "获取数据失败，请重试", 1).show();
                finish();
            }

        }.history(this);

    }
}

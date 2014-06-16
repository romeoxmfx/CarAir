
package com.android.carair.fragments;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.AMap.CancelableCallback;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.android.carair.R;
import com.android.carair.activities.HistoryActivity;
import com.android.carair.activities.MapActivity;
import com.android.carair.adapters.MainListApapter;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.Loc;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.DeviceConfig;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.android.carair.views.PinnedSectionListView;

public class MainFragment extends BaseFragment {
    private MapView map;
    private AMap amap;
    private Bundle saveInstanceState;
    private Timer timer;

    MainListApapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.saveInstanceState = savedInstanceState;
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_main, null);

        PinnedSectionListView listView = (PinnedSectionListView) mMainView
                .findViewById(R.id.main_list);

        mAdapter = new MainListApapter(getActivity(), getResources()
                .getStringArray(R.array.item_title), this);

        listView.setAdapter(mAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                   Toast.makeText(getActivity(), "a", 1).show();
                Item item = (Item) arg0.getAdapter().getItem(arg2);
                if(item.type == Item.ITEM_IN_CAR || item.type == Item.ITEM_OUT_CAR){
                    //打开历史
                    Intent intent = new Intent();
                    intent.putExtra("type", item.type);
                    intent.setClass(MainFragment.this.getActivity(), HistoryActivity.class);
                    MainFragment.this.getActivity().startActivity(intent);
                }
            }
        });

        // new MyTask().execute("");
//        startTimer();

        return mMainView;
    }
    
    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
    }
    
    public void startTimer(){
        if(timer == null){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    query();
                    
                }
            }, 0, 1000 * 10);
        }
        
        
    }
    
    public void stopTimer(){
        if(timer != null){
            timer.cancel();
        }
    }

    private void query() {
        if(getActivity() == null){
            return;
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("pm25ValueInCar", packet.getRespMessage().getDevinfo().getPm25th());
                    map.put("concentrationOfPoisonousGasesValue", "100");
                    map.put("querying", "查询完成");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(new Date());
                    map.put("timeInCar", date);
                    map.put("battery", packet.getRespMessage().getDevinfo().getBattery());
                    map.put("conn", packet.getRespMessage().getDevinfo().getConn());
                    Item item = mAdapter.getItemByType(Item.ITEM_IN_CAR);
                    item.setMap(map);
//                    mAdapter.refreshItem(item);
                    
//                    map = new HashMap<String, String>();
                    map.put("opm25", packet.getRespMessage().getAir().getOpm25());
//                    Item itemout = mAdapter.getItemByType(Item.ITEM_OUT_CAR);
//                    itemout.setMap(map);
//                    mAdapter.refreshItem(itemout);
                    
                    //获取净化器当前状态
                    int ratio = -1;
                    int cleantimer = -1;
                    int autoclean = -1;
                    try {
                        ratio = Util.decodeDevCtrl(packet.getRespMessage().getDevctrl(),CarairConstants.TYPE_RATIO);
                        cleantimer = Util.decodeDevCtrl(packet.getRespMessage().getDevctrl(),CarairConstants.TYPE_TIMER_ENABLE);
                        autoclean = Util.decodeDevCtrl(packet.getRespMessage().getDevctrl(),CarairConstants.TYPE_AUTO_CLEAN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    map = new HashMap<String, String>();
                    map.put("ratio", Util.convertRatioString(ratio));
                    map.put("cleantimer", Util.converOnOffString(cleantimer));
                    map.put("autoclean", autoclean + "");
//                    Item itemsetting = mAdapter.getItemByType(Item.ITEM_SETTING);
//                    itemsetting.setMap(map);
                    mAdapter.refreshItem(item);
                    
                    
                    //保存loc 
                    Loc loc = packet.getRespMessage().getLoc();
                    if(loc != null && getActivity() != null){
                        Util.saveLoc(loc,getActivity());
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
               
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.query(getActivity());
    }

    public void setMap(MapView map) {
        this.map = map;
        map.onCreate(saveInstanceState);
        if (amap == null) {
            amap = map.getMap();
            amap.setOnMapClickListener(new OnMapClickListener() {
                
                @Override
                public void onMapClick(LatLng arg0) {
                    if(getActivity() != null){
                        getActivity().startActivity(new Intent(getActivity(),MapActivity.class));
                    }
                }
            });
        }
        setlocation();
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        amap.animateCamera(update, 1000, callback);
    }

    private void setlocation() {
        // if (mAMapLocationManager == null) {
        // if (getActivity() == null) {
        // return;
        // }
        // mAMapLocationManager =
        // LocationManagerProxy.getInstance(getActivity());
        // mAMapLocationManager.getLastKnownLocation(arg0)
        // }
        if (getActivity() != null) {
            Location location = DeviceConfig.getLocation(getActivity());
            if (location == null) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(
                        Context.LOCATION_SERVICE);
                // 返回所有已知的位置提供者的名称列表，包括未获准访问或调用活动目前已停用的。
                List<String> lp = lm.getAllProviders();
                Criteria criteria = new Criteria();
                criteria.setCostAllowed(false);
                // 设置位置服务免费
                criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 设置水平位置精度
                // getBestProvider 只有允许访问调用活动的位置供应商将被返回
                String providerName = lm.getBestProvider(criteria, true);
                location = DeviceConfig.getLocation(getActivity());
                if (location == null) {
                    return;
                }
            }
            LatLng lat = new LatLng(location.getLatitude(),
                    location.getLongitude());
            Log.i("location =" + location.getLatitude() + "," +
                    location.getLongitude());
            // LatLng lat = new LatLng(39.983456, 116.3154950);
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(lat);
            markerOption.draggable(true);
            markerOption.icon(
                    BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            Marker marker = amap.addMarker(markerOption);
            marker.showInfoWindow();

            changeCamera(
                    CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            lat, 18, 0, 30)), null);
        }
    }

    // @Override
    // public void onAttach(Activity activity) {
    // super.onAttach(activity);
    // ((MainActivity) activity).onSectionAttached(Const);
    // }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map != null) {
            map.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDestroy();
        }
    }

}

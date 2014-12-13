
package com.android.airhelper.activities;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.AMap.CancelableCallback;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.android.airhelper.api.Loc;
import com.android.carair.R;
import com.android.carair.utils.DeviceConfig;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class MapActivity extends SherlockFragmentActivity {
    private MapView map;
    private AMap amap;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        map = (MapView) findViewById(R.id.map);
        map.onCreate(arg0);

        if (amap == null) {
            amap = map.getMap();
        }

        amap.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                if ("我的位置".equals(arg0.getSnippet())) {
                    Toast.makeText(MapActivity.this, "我的位置", Toast.LENGTH_SHORT).show();
                    return true;
                } else if ("净化器位置".equals(arg0.getSnippet())) {
                    Toast.makeText(MapActivity.this, "净化器位置", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        amap.setInfoWindowAdapter(new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                return null;
            }
        });

        setlocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void setlocation() {
        boolean hasDeviceLoc = false;
        // Loc loc = Util.getSavedLoc(this);
        String[] save_location = Util.getLocation(this);
        if (!TextUtils.isEmpty(save_location[0]) && !TextUtils.isEmpty(save_location[1])) {
            try {
                Log.i("lat %s ,lng %s", save_location[0], save_location[1]);
                double lat_value = Double.parseDouble(save_location[0]);
                double lng_value = Double.parseDouble(save_location[1]);
                if (lat_value > 0 && lng_value > 0) {
                    LatLng latlng = new LatLng(lat_value,
                            lng_value);
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(latlng);
                    markerOption.draggable(false);
                    // markerOption.title("净化器");
                    markerOption.snippet("净化器位置");
                    markerOption.icon(
                            BitmapDescriptorFactory
                                    .fromResource(R.drawable.map_place));
                    // .title("净化器位置");
                    // .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    Marker marker = amap.addMarker(markerOption);
                    // marker.setTitle("净化器");
                    marker.showInfoWindow();

                    changeCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    latlng, 18, 0, 30)), null);
                    hasDeviceLoc = true;
                } else {
                    Toast.makeText(this, "无法准确获取汽车地理位置", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "无法获取汽车地理位置", Toast.LENGTH_LONG).show();
        }

        LocationManager lm = (LocationManager) this.getSystemService(
                Context.LOCATION_SERVICE);
        // 返回所有已知的位置提供者的名称列表，包括未获准访问或调用活动目前已停用的。
        List<String> lp = lm.getAllProviders();
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        // 设置位置服务免费
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 设置水平位置精度
        // getBestProvider 只有允许访问调用活动的位置供应商将被返回
        String providerName = lm.getBestProvider(criteria, true);
        Location location = DeviceConfig.getLocation(this);
        if (location == null) {
            location = DeviceConfig.getLocation(this);
        }
        if(location == null){
            Toast.makeText(this, "获取不到gps，无法得到我的位置", Toast.LENGTH_LONG).show();
            return;
        }
        LatLng lat = new LatLng(location.getLatitude(),
                location.getLongitude());
        Log.i("location =" + location.getLatitude() + "," +
                location.getLongitude());
        // LatLng lat = new LatLng(39.983456, 116.3154950);
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(lat);
        // markerOption.title("我");
        markerOption.snippet("我的位置");
        markerOption.icon(
                BitmapDescriptorFactory
                        .fromResource(R.drawable.flag));
        // markerOption.icon(
        // BitmapDescriptorFactory
        // .HUE_AZURE);
        markerOption.draggable(false);
        // .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = amap.addMarker(markerOption);
        // marker.setTitle("我");
        marker.showInfoWindow();
        if (!hasDeviceLoc) {
            changeCamera(
                    CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            lat, 18, 0, 30)), null);
        }
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        amap.animateCamera(update, 1000, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map != null) {
            map.onSaveInstanceState(outState);
        }
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
}

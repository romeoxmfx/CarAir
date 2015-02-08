
package com.android.carair.activities;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.android.carair.R;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class MapActivity extends SherlockFragmentActivity implements LocationSource,
        AMapLocationListener {
    private MapView map;
    private AMap amap;
    private LocationManagerProxy mLocationManagerProxy;
    private OnLocationChangedListener mListener;
    private AMapLocation aMapLocation;
    private boolean hasDeviceLoc;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        // mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        //
        // //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        // //在定位结束后，在合适的生命周期调用destroy()方法
        // //其中如果间隔时间为-1，则定位只定一次
        // mLocationManagerProxy.requestLocationData(
        // LocationProviderProxy.AMapNetwork, 60*1000, 15, this);
        //
        // mLocationManagerProxy.setGpsEnable(true);
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
        deactivate();
    }

    private void setlocation() {
        hasDeviceLoc = false;
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

        amap.setLocationSource(this);// 设置定位监听
        amap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        amap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        amap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
        MyLocationStyle style = new MyLocationStyle();
//        style.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.flag));
        // style.strokeWidth(0);
        // style.radiusFillColor(Color.TRANSPARENT);
        // style.strokeColor(Color.TRANSPARENT);
        amap.setMyLocationStyle(style);
    }

    // LocationManager lm = (LocationManager) this.getSystemService(
    // Context.LOCATION_SERVICE);
    // // 返回所有已知的位置提供者的名称列表，包括未获准访问或调用活动目前已停用的。
    // List<String> lp = lm.getAllProviders();
    // Criteria criteria = new Criteria();
    // criteria.setCostAllowed(false);
    // // 设置位置服务免费
    // criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 设置水平位置精度
    // // getBestProvider 只有允许访问调用活动的位置供应商将被返回
    // String providerName = lm.getBestProvider(criteria, true);
    // Location location = DeviceConfig.getLocation(this);
    // if (location == null) {
    // location = DeviceConfig.getLocation(this);
    // }
    // if(location == null){
    // Toast.makeText(this, "获取不到gps，无法得到我的位置", Toast.LENGTH_LONG).show();
    // return;
    // }
    // LatLng lat = new LatLng(location.getLatitude(),
    // location.getLongitude());
    // Log.i("location =" + location.getLatitude() + "," +
    // location.getLongitude());
    // // LatLng lat = new LatLng(39.983456, 116.3154950);
    // MarkerOptions markerOption = new MarkerOptions();
    // markerOption.position(lat);
    // // markerOption.title("我");
    // markerOption.snippet("我的位置");
    // markerOption.icon(
    // BitmapDescriptorFactory
    // .fromResource(R.drawable.flag));
    // // markerOption.icon(
    // // BitmapDescriptorFactory
    // // .HUE_AZURE);
    // markerOption.draggable(false);
    // // .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    // Marker marker = amap.addMarker(markerOption);
    // // marker.setTitle("我");
    // marker.showInfoWindow();
    // if (!hasDeviceLoc) {
    // changeCamera(
    // CameraUpdateFactory.newCameraPosition(new CameraPosition(
    // lat, 18, 0, 30)), null);
    // }

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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        // if(arg0 != null && arg0.getAMapException().getErrorCode() == 0){
        // //获取位置信息
        // Double geoLat = arg0.getLatitude();
        // Double geoLng = arg0.getLongitude();
        // }

        if (mListener != null && amapLocation != null) {
            if (amapLocation.getAMapException().getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                LatLng lat = new LatLng(amap.getMyLocation().getLatitude(),
                        amap.getMyLocation().getLongitude());
                if (!hasDeviceLoc) {
                    changeCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    lat, 18, 0, 30)), null);
                } else {
                    String[] save_location = Util.getLocation(MapActivity.this);
                    if (!TextUtils.isEmpty(save_location[0])
                            && !TextUtils.isEmpty(save_location[1])) {
                        try {
                            Log.i("lat %s ,lng %s", save_location[0], save_location[1]);
                            double lat_value = Double.parseDouble(save_location[0]);
                            double lng_value = Double.parseDouble(save_location[1]);
                            if (lat_value > 0 && lng_value > 0) {
                                LatLng latlng = new LatLng(lat_value,
                                        lng_value);
                                changeCamera(
                                        CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                                latlng, 18, 0, 30)), null);
                                hasDeviceLoc = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // private void stopLocation() {
    // if (mLocationManagerProxy != null) {
    // mLocationManagerProxy.removeUpdates(this);
    // mLocationManagerProxy.destory();
    // }
    // mLocationManagerProxy = null;
    // }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationManagerProxy == null) {
            mLocationManagerProxy = LocationManagerProxy.getInstance(this);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用destroy()方法
            // 其中如果间隔时间为-1，则定位只定一次
            mLocationManagerProxy.requestLocationData(
                    // LocationProviderProxy.AMapNetwork, 60 * 1000, 10, this);
                    LocationProviderProxy.AMapNetwork, -1, 10, this);
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }
}

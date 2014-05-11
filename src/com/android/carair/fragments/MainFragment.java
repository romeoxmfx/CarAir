
package com.android.carair.fragments;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.DeviceConfig;

public class MainFragment extends BaseFragment {
    String str;
    private MapView map;
    private AMap amap;

    // private LocationManagerProxy mAMapLocationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = (FragmentViewBase) inflater.inflate(
                    R.layout.carair_fragment_main, null);

            map = (MapView) mMainView.findViewById(R.id.map);
            map.onCreate(savedInstanceState);
            if (amap == null) {
                amap = map.getMap();
            }
        } else {
            if (mMainView.getParent() != null) {
                ((ViewGroup) mMainView.getParent()).removeView(mMainView);
            }
        }
        if (getArguments() != null) {
            str = getArguments().getString("text");
        }
        TextView tv = (TextView) mMainView.findViewById(R.id.tv);
        if (!TextUtils.isEmpty(str)) {
            tv.setText(str);
        }
        return mMainView;
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
//            LatLng lat = new LatLng(location.getAltitude(), location.getLatitude());
            LatLng lat = new LatLng(34.341568, 108.940174);
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(lat);
//            markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");
            markerOption.draggable(true);
            Marker marker = amap.addMarker(markerOption);
            marker.showInfoWindow();
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
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        setlocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

}

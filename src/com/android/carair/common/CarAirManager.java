
package com.android.carair.common;

import java.util.List;

import com.android.carair.api.Timer;
import com.android.carair.utils.Util;

import android.content.Context;

public class CarAirManager {
    private static CarAirManager instance = new CarAirManager();
    private List<Timer> timer;

    private String lat;
    private int state;// 用于app状态上报
    private boolean mConnection;// app链接状态

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    private String lng;

    private CarAirManager() {

    }

    public static CarAirManager getInstance() {
        return instance;
    }

    public synchronized void init(Context context) {
        if (!Util.hasRatio(context)) {
            Util.saveRatio(CarairConstants.RATIO_HIGH, context);
        }
    }

    public List<Timer> getTimer() {
        return timer;
    }

    public void setTimer(List<Timer> timer) {
        this.timer = timer;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean ismConnection() {
        return mConnection;
    }

    public void setmConnection(boolean mConnection) {
        this.mConnection = mConnection;
    }

}

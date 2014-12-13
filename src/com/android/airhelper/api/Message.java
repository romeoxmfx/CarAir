
package com.android.airhelper.api;

import java.util.List;

import com.google.gson.JsonArray;

public class Message {

    private DevInfo devinfo;
    private MobInfo mobinfo;
    private AppInfo appinfo;
    private Activity activity;
    private Store store;
    private Loc loc;
    private String devctrl;
    private String cs;
    private Air air;
    private int badge;

    public Air getAir() {
        return air;
    }

    public void setAir(Air air) {
        this.air = air;
    }

    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public String getDevctrl() {
        return devctrl;
    }

    public void setDevctrl(String devctrl) {
        this.devctrl = devctrl;
    }

    public String getCs() {
        return cs;
    }

    public void setCs(String cs) {
        this.cs = cs;
    }

    public DevInfo getDevinfo() {
        return devinfo;
    }

    public void setDevinfo(DevInfo devinfo) {
        this.devinfo = devinfo;
    }

    public MobInfo getMobinfo() {
        return mobinfo;
    }

    public void setMobinfo(MobInfo mobinfo) {
        this.mobinfo = mobinfo;
    }

    public AppInfo getAppinfo() {
        return appinfo;
    }

    public void setAppinfo(AppInfo appinfo) {
        this.appinfo = appinfo;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

}

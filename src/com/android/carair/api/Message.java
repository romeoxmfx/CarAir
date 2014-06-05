
package com.android.carair.api;

import java.util.List;

import com.google.gson.JsonArray;

public class Message {

    private DevInfo devinfo;
    private MobInfo mobinfo;
    private AppInfo appinfo;
    private Loc loc;
    private String devctrl;
    private String cs;
    private Air air;
    

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

}

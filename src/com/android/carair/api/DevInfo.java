
package com.android.carair.api;

import java.util.List;

public class DevInfo {

    private String id;
    private String mac;
    private String ts;
    private String states;
    private String conn;
    private String pm25;
    private String bettery;
    private List<Timer> timer;

    public String getConn() {
        return conn;
    }

    public void setConn(String conn) {
        this.conn = conn;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getBettery() {
        return bettery;
    }

    public void setBettery(String bettery) {
        this.bettery = bettery;
    }

    private String pm25th;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public String getPm25th() {
        return pm25th;
    }

    public void setPm25th(String pm25th) {
        this.pm25th = pm25th;
    }

    public List<Timer> getTimer() {
        return timer;
    }

    public void setTimer(List<Timer> timer) {
        this.timer = timer;
    }

}

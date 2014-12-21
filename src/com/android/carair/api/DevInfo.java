
package com.android.carair.api;

import java.util.List;

public class DevInfo {

    private String id;
    private String mac;
    private String ts;
    private String states;
    private String conn;
    private String pm25;
    private String battery;
    private String harmair;
    private String lat;
    private String lng;
    private String humi;
    private String temper;
    private String temp;
    private List<Timer> timer;
    private Sleep_period sleep_period;
    private Gyroscope gyroscope;

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

    public String getHarmair() {
        return harmair;
    }

    public void setHarmair(String harmair) {
        this.harmair = harmair;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

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

    public String getHumi() {
        return humi;
    }

    public void setHumi(String humi) {
        this.humi = humi;
    }

    public String getTemper() {
        return temper;
    }

    public void setTemper(String temper) {
        this.temper = temper;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public Gyroscope getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(Gyroscope gyroscope) {
        this.gyroscope = gyroscope;
    }

    public Sleep_period getSleep_period() {
        return sleep_period;
    }

    public void setSleep_period(Sleep_period sleep_period) {
        this.sleep_period = sleep_period;
    }

}

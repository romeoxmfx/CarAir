package com.android.carair.api;

public class DevInfo {

	private String id;
	private String mac;
	private String ts;
	private String states;
	private String conn;
	private String pm25;
	private String bettery;
	
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
	
}

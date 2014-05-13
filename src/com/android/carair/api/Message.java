package com.android.carair.api;

public class Message {

	private DevInfo devInfo;
	private MobInfo mobInfo;
	private AppInfo appInfo;
	private Loc reqLoc;
	private String devctrl;
	private String cs;
	public DevInfo getDevInfo() {
		return devInfo;
	}
	public void setDevInfo(DevInfo devInfo) {
		this.devInfo = devInfo;
	}
	public MobInfo getMobInfo() {
		return mobInfo;
	}
	public void setMobInfo(MobInfo mobInfo) {
		this.mobInfo = mobInfo;
	}
	public AppInfo getAppInfo() {
		return appInfo;
	}
	public void setAppInfo(AppInfo appInfo) {
		this.appInfo = appInfo;
	}
	public Loc getReqLoc() {
		return reqLoc;
	}
	public void setReqLoc(Loc reqLoc) {
		this.reqLoc = reqLoc;
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
	
}

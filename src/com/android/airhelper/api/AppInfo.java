
package com.android.airhelper.api;

public class AppInfo {

    private String ver;
    private String channel;
    private String notice;
    private int dispnum;
    private String badge;
    private Activity activity;

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getDispnum() {
        return dispnum;
    }

    public void setDispnum(int dispnum) {
        this.dispnum = dispnum;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

}

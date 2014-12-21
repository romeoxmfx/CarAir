
package com.android.carair.api;

public class AppInfo {

    private String ver;
    private String channel;
    private String notice;
    private int dispnum;
    private String badge;
    private Activity activity;
    private int has_share;
    private int has_humidity;
    private int has_gyroscopes;
    private int has_sleepperiod;
    private Copyright copyright;
    private String apnm;

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

    public int getHas_share() {
        return has_share;
    }

    public void setHas_share(int has_share) {
        this.has_share = has_share;
    }

    public int getHas_humidity() {
        return has_humidity;
    }

    public void setHas_humidity(int has_humidity) {
        this.has_humidity = has_humidity;
    }

    public int getHas_gyroscopes() {
        return has_gyroscopes;
    }

    public void setHas_gyroscopes(int has_gyroscopes) {
        this.has_gyroscopes = has_gyroscopes;
    }

    public int getHas_sleepperiod() {
        return has_sleepperiod;
    }

    public void setHas_sleepperiod(int has_sleepperiod) {
        this.has_sleepperiod = has_sleepperiod;
    }

    public Copyright getCopyright() {
        return copyright;
    }

    public void setCopyright(Copyright copyright) {
        this.copyright = copyright;
    }

    public String getApnm() {
        return apnm;
    }

    public void setApnm(String apnm) {
        this.apnm = apnm;
    }

}

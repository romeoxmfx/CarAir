
package com.android.carair.api;

import android.content.Context;

public interface CarAirServiceInterface {

    public void reg(Context context, String id);

    public void query(Context context);

    public void devctrl(Context context, boolean isopen, boolean useBattery);

    public void history(Context context);

    public void devWindCtrl(Context context, int wind, boolean isopen);

    public void configset(Context context,Sleep_period sleep, Gyroscope gyroscope);
    
    public void config(Context context);
    
    public void configset(Context context,Filter filter);
}

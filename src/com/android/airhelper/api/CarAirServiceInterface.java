
package com.android.airhelper.api;

import android.content.Context;

public interface CarAirServiceInterface {

    public void reg(Context context, String id);

    public void query(Context context);

    public void devctrl(Context context, boolean isopen, boolean useBattery);

    public void history(Context context);

    public void devWindCtrl(Context context, int wind, boolean isopen);

}
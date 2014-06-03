package com.android.carair.common;

import com.android.carair.utils.Util;

import android.content.Context;

public class CarAirManager {
    private static CarAirManager instance = new CarAirManager();
    private CarAirManager(){
        
    }
    public static CarAirManager getInstance(){
        return instance;
    }
    
    public synchronized void init(Context context){
        if(!Util.hasRatio(context)){
            Util.saveRatio(CarairConstants.RATIO_NORMAL, context);
        }
    }
}

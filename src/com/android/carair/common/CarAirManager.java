
package com.android.carair.common;

import java.util.List;

import com.android.carair.api.Timer;
import com.android.carair.utils.Util;

import android.content.Context;

public class CarAirManager {
    private static CarAirManager instance = new CarAirManager();
    private List<Timer> timer;

    private CarAirManager() {

    }

    public static CarAirManager getInstance() {
        return instance;
    }

    public synchronized void init(Context context) {
        if (!Util.hasRatio(context)) {
            Util.saveRatio(CarairConstants.RATIO_NORMAL, context);
        }
    }

    public List<Timer> getTimer() {
        return timer;
    }

    public void setTimer(List<Timer> timer) {
        this.timer = timer;
    }

}

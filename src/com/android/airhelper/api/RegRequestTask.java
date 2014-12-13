package com.android.airhelper.api;

import android.util.Log;

import com.android.airhelper.common.CarairConstants;
import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;

class RegRequestTask extends AsyncHttpHelper {

    @Override
    protected void onHttpSucceed(int type, BizResponse response) {
    	Log.d(CarairConstants.LOG_KEY, "onHttpSucceed");
    }

    @Override
    protected void onHttpFailed(int type, HttpErrorBean error) {
    	Log.d(CarairConstants.LOG_KEY, "onHttpFailed");
    }

}

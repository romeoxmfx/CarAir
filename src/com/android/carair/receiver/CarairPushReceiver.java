package com.android.carair.receiver;

import com.android.airhelper.api.CarAirReqTask;
import com.android.airhelper.api.RespProtocolPacket;
import com.android.carair.net.HttpErrorBean;
import com.igexin.sdk.PushConsts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class CarairPushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:
                String cid = bundle.getString("clientid");
                if(!TextUtils.isEmpty(cid)){
                    new CarAirReqTask() {
                        
                        @Override
                        public void onCompleteSucceed(RespProtocolPacket packet) {
                        }
                        
                        @Override
                        public void onCompleteFailed(int type, HttpErrorBean error) {
                        }
                    }.query(context, cid);
                }
                break;
            case PushConsts.GET_MSG_DATA:
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;
            
        }
    }

}

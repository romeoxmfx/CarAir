package com.android.carair.receiver;

import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.net.HttpErrorBean;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;

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
                Log.i("carair", "cid = " + cid);
                if(!TextUtils.isEmpty(cid)){
                    new CarAirReqTask() {
                        
                        @Override
                        public void onCompleteSucceed(RespProtocolPacket packet) {
                            Log.i("carair", "success send cid");
                        }
                        
                        @Override
                        public void onCompleteFailed(int type, HttpErrorBean error) {
                        }
                    }.query(context, cid);
                }
                break;
            case PushConsts.GET_MSG_DATA:
             // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");
                
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
//                boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
//                Log.d("GetuiSdkDemo", "第三方回执接口调用" + (result ? "成功" : "失败"));
                
                if (payload != null) {
                    String data = new String(payload);

                    Log.d("GetuiSdkDemo", "Got Payload:" + data);
                }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;
            
        }
    }

}

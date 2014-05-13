package com.android.carair.api;

import org.json.JSONObject;

import android.content.Context;

import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.request.DevctlReuqest;
import com.android.carair.request.HistoryRequest;
import com.android.carair.request.QueryRequest;
import com.android.carair.request.RegRequest;
import com.android.carair.utils.DeviceConfig;
import com.google.gson.Gson;

public abstract class CarAirReqTask extends AsyncHttpHelper implements CarAirServiceInterface{

	public abstract void onCompleteSucceed(RespProtocolPacket packet);
	public abstract void onCompleteFailed(int type, HttpErrorBean error);
	
	@Override
	public void reg(Context context) {

        try {
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", DeviceConfig.getIMSI(context));
            devinfo.put("mac", DeviceConfig.getMac(context));
            devinfo.put("ts", System.currentTimeMillis());

            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[1]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("mobinfo", mobileInfo);
            message.put("appinfo", appinfo);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 0)
                   .put("message", message)
                   .put("cs", "2185375313");

            RegRequest regRequest = new RegRequest(jsonObj.toString());
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void query(Context context) {
		 try {
	            JSONObject devinfo = new JSONObject();
//	            devinfo.put("id", DeviceConfig.getIMSI(context));
	            devinfo.put("id", "5614165");
	            devinfo.put("mac", DeviceConfig.getMac(context));
	            devinfo.put("ts", System.currentTimeMillis());
	            
	            JSONObject loc = new JSONObject();
	            loc.put("lat", "232.2");
	            loc.put("lng", "233.3");
	            loc.put("city", System.currentTimeMillis());

	            JSONObject appinfo = new JSONObject();
	            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
	            appinfo.put("channel", "autocube");
	            appinfo.put("state", "1");

	            JSONObject message = new JSONObject();
	            message.put("devinfo", devinfo);
	            message.put("loc", loc);
	            message.put("appinfo", appinfo);

	            JSONObject jsonObj = new JSONObject();
	            jsonObj.put("cmd", 1)
	                   .put("message", message)
	                   .put("cs", "2185375313");

	            QueryRequest regRequest = new QueryRequest(jsonObj.toString());
	            this.loadHttpContent(regRequest);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}

	@Override
	public void devctrl(Context context) {
		try {
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", DeviceConfig.getIMSI(context));
            devinfo.put("mac", DeviceConfig.getMac(context));
            devinfo.put("ts", System.currentTimeMillis());
            devinfo.put("states", "");
            devinfo.put("pm25th", "200");
            
            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");

            JSONObject message = new JSONObject();
            message.put("devctrl", "");
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 2)
                   .put("message", message)
                   .put("cs", "2185375313");

            DevctlReuqest regRequest = new DevctlReuqest(jsonObj.toString());
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void history(Context context) {
		try {
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", DeviceConfig.getIMSI(context));
            devinfo.put("mac", DeviceConfig.getMac(context));
            devinfo.put("ts", System.currentTimeMillis());
            
            JSONObject loc = new JSONObject();
            loc.put("city", "hangzhou");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("loc", loc);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 3)
                   .put("message", message)
                   .put("cs", "2185375313");

            HistoryRequest regRequest = new HistoryRequest(jsonObj.toString());
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	protected void onHttpSucceed(int type, BizResponse response) {
		Gson gson = new Gson();
		onCompleteSucceed(gson.fromJson(response.getRawResponse().toString(), RespProtocolPacket.class));
	}

	@Override
	protected void onHttpFailed(int type, HttpErrorBean error) {
		onCompleteFailed(type, error);
	}
	
}

package com.android.carair.api;

import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.android.carair.common.CarairConstants;
import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.request.DevctlReuqest;
import com.android.carair.request.HistoryRequest;
import com.android.carair.request.QueryRequest;
import com.android.carair.request.RegRequest;
import com.android.carair.utils.DeviceConfig;
import com.android.carair.utils.Util;
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
		        long ts = Util.getTs();
//		        long ts = 1400081320;
	            JSONObject devinfo = new JSONObject();
//	            devinfo.put("id", DeviceConfig.getIMSI(context));
	            devinfo.put("id", CarairConstants.DEVICE_ID);
	            devinfo.put("mac", "02:00:00:00:00:00");
	            devinfo.put("ts", ts);
//	            devinfo.put("ts", 1400081320);
	            
	            JSONObject loc = new JSONObject();
	            Location mloc = DeviceConfig.getLocation(context);
	            if(mloc != null){
	                loc.put("lat",mloc.getLatitude());
	                loc.put("lng", mloc.getLongitude());
	            }
	            if(Util.getSavedLoc(context) != null){
	                loc.put("city", Util.getSavedLoc(context).getCity());
	            }
//	            loc.put("city", "");

	            JSONObject appinfo = new JSONObject();
	            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
	            appinfo.put("channel", "autocube");
	            appinfo.put("state", 1);

	            JSONObject message = new JSONObject();
	            message.put("devinfo", devinfo);
	            message.put("loc", loc);
	            message.put("appinfo", appinfo);
	            message.put("cs", Util.checkSum(CarairConstants.DEVICE_ID, "02:00:00:00:00:00", ts));
//	            message.put("cs", "1304916411");

	            JSONObject jsonObj = new JSONObject();
	            jsonObj.put("cmd", 1)
	                   .put("message", message);
//	                   .put("cs", "1304916411");

	            QueryRequest regRequest = new QueryRequest(jsonObj.toString());
	            this.loadHttpContent(regRequest);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}

	@Override
	public void devctrl(Context context,boolean isopen) {
		try {
		    long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", CarairConstants.DEVICE_ID);
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            devinfo.put("states", Util.getStatus(context));
            devinfo.put("pm25th", "200");
            
            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");

            JSONObject message = new JSONObject();
            message.put("devctrl", Util.getDevctrl(isopen));
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("cs", Util.checkSum(CarairConstants.DEVICE_ID, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 2)
                   .put("message", message);
                   

            DevctlReuqest regRequest = new DevctlReuqest(jsonObj.toString());
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void history(Context context) {
		try {
		    long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", CarairConstants.DEVICE_ID);
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            
            Loc mloc = Util.getSavedLoc(context);
            
            JSONObject loc = new JSONObject();
            
            loc.put("city",mloc.getCity());

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("loc", loc);
            message.put("cs", Util.checkSum(CarairConstants.DEVICE_ID, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 3)
                   .put("message", message);

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

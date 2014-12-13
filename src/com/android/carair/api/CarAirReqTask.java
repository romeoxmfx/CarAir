
package com.android.carair.api;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;
import android.text.TextUtils;

import com.android.carair.activities.MainActivity;
import com.android.carair.common.CarAirManager;
import com.android.carair.common.CarairConstants;
import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.request.ActivityInfoClickReuqest;
import com.android.carair.request.ActivityInfoReuqest;
import com.android.carair.request.DevctlReuqest;
import com.android.carair.request.HistoryRequest;
import com.android.carair.request.QueryRequest;
import com.android.carair.request.RegRequest;
import com.android.carair.request.TimerRequest;
import com.android.carair.request.TimersetRequest;
import com.android.carair.utils.AESUtils;
import com.android.carair.utils.DeviceConfig;
import com.android.carair.utils.Log;
import com.android.carair.utils.RequestUtil;
import com.android.carair.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class CarAirReqTask extends AsyncHttpHelper implements CarAirServiceInterface {

    public abstract void onCompleteSucceed(RespProtocolPacket packet);

    public abstract void onCompleteFailed(int type, HttpErrorBean error);

    @Override
    public void reg(Context context, String id) {
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", id);
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("mobinfo", mobileInfo);
            message.put("appinfo", appinfo);
            int did = Integer.parseInt(id);
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 0)
                    .put("message", message);
            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            RegRequest regRequest = new RegRequest(compressed);
            this.loadHttpContent(regRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void query(Context context,String clientId) {
        try {
            long ts = Util.getTs();
            // long ts = 1400081320;
            JSONObject devinfo = new JSONObject();
            // devinfo.put("id", DeviceConfig.getIMSI(context));
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            // devinfo.put("ts", 1400081320);

            JSONObject loc = new JSONObject();
//            Location mloc = DeviceConfig.getLocation(context);
//            String[] mloc = Util.getLocation(context);
//            if (mloc != null && mloc.length == 2) {
//                loc.put("lat", mloc[0]);
//                loc.put("lng", mloc[1]);
//            }
            if (Util.getSavedLoc(context) != null
                    && !TextUtils.isEmpty(Util.getSavedLoc(context).getCity())) {
                loc.put("city", Util.getSavedLoc(context).getCity());
            }
            try {
                if(Util.getSavedLoc(context) != null){
                    Loc l = Util.getSavedLoc(context);
                    loc.put("lat", l.getLat());
                    loc.put("lng", l.getLng());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // loc.put("city", "");

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            appinfo.put("state", CarAirManager.getInstance().getState());
            CarAirManager.getInstance().setState(MainActivity.STATE_NORMAL);
            appinfo.put("type", "android");
            appinfo.put("getui_client_id", clientId);
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("loc", loc);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));
            // message.put("cs", "1304916411");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 1)
                    .put("message", message);
            // .put("cs", "1304916411");
            String json = jsonObj.toString();
            Log.i("query %s", json);
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            QueryRequest regRequest = new QueryRequest(compressed);
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void query(Context context) {
        try {
            long ts = Util.getTs();
            // long ts = 1400081320;
            JSONObject devinfo = new JSONObject();
            // devinfo.put("id", DeviceConfig.getIMSI(context));
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            // devinfo.put("ts", 1400081320);

            JSONObject loc = new JSONObject();
//            Location mloc = DeviceConfig.getLocation(context);
//            String[] mloc = Util.getLocation(context);
//            if (mloc != null && mloc.length == 2) {
//                loc.put("lat", mloc[0]);
//                loc.put("lng", mloc[1]);
//            }
            if (Util.getSavedLoc(context) != null
                    && !TextUtils.isEmpty(Util.getSavedLoc(context).getCity())) {
                loc.put("city", Util.getSavedLoc(context).getCity());
            }
            try {
                if(Util.getSavedLoc(context) != null){
                    Loc l = Util.getSavedLoc(context);
                    loc.put("lat", l.getLat());
                    loc.put("lng", l.getLng());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // loc.put("city", "");

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            appinfo.put("state", CarAirManager.getInstance().getState());
            appinfo.put("type", "android");
            CarAirManager.getInstance().setState(MainActivity.STATE_NORMAL);
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("loc", loc);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));
            // message.put("cs", "1304916411");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 1)
                    .put("message", message);
            // .put("cs", "1304916411");
            String json = jsonObj.toString();
            Log.i("query %s", json);
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            QueryRequest regRequest = new QueryRequest(compressed);
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void devctrl(Context context, boolean isopen, boolean useBattery) {
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            devinfo.put("states", Util.getStatusHeader(context, isopen,useBattery));
            devinfo.put("pm25th", Util.getWarningPM(context));
            devinfo.put("harmairth", Util.getWarningHarmful(context));
            devinfo.put("devctrl", isopen ? 1 : 0);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");
            
            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 2)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            DevctlReuqest regRequest = new DevctlReuqest(compressed);
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void devWindCtrl(Context context, int wind , boolean isopen) {
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);
            devinfo.put("states", Util.getWindStatusHeader(context, wind));
            devinfo.put("pm25th", Util.getWarningPM(context));
            devinfo.put("harmairth", Util.getWarningHarmful(context));
            devinfo.put("devctrl", isopen ? 1 : 0);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 2)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            DevctlReuqest regRequest = new DevctlReuqest(compressed);
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
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            Loc mloc = Util.getSavedLoc(context);

            JSONObject loc = new JSONObject();

            loc.put("city", mloc.getCity());

            JSONObject message = new JSONObject();
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            message.put("devinfo", devinfo);
            message.put("loc", loc);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 3)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            
            HistoryRequest regRequest = new HistoryRequest(compressed);
            this.loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void timer(Context context) {
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 5)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            
            TimerRequest regRequest = new TimerRequest(compressed);
            this.loadHttpContent(regRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void timerset(Context context, JSONArray timer) {
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            appinfo.put("timer", timer);
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 4)
                    .put("message", message);
            
            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();

            TimersetRequest regRequest = new TimersetRequest(compressed);
            this.loadHttpContent(regRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void activityinfo(Context context){
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 6)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            ActivityInfoReuqest regRequest = new ActivityInfoReuqest(compressed);
            this.loadHttpContent(regRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void activityinfoClick(Context context, String activityId, String type){
        try {
            long ts = Util.getTs();
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", Util.getDeviceId(context));
            devinfo.put("mac", "02:00:00:00:00:00");
            devinfo.put("ts", ts);

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(context));
            appinfo.put("channel", "autocube");
            appinfo.put("activityid", activityId);
            appinfo.put("type", type);
            
            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(context));
            mobileInfo.put("imei", DeviceConfig.getDeviceId(context));
            mobileInfo.put("mac", DeviceConfig.getMac(context));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", "android");
            mobileInfo.put("os_ver", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(context);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[0]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("appinfo", appinfo);
            message.put("mobinfo", mobileInfo);
            int did = 0;
            if(!TextUtils.isEmpty(Util.getDeviceId(context))){
                did = Integer.parseInt(Util.getDeviceId(context));
            }
            message.put("cs", Util.checkSum(did, "02:00:00:00:00:00", ts));

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 7)
                    .put("message", message);

            String json = jsonObj.toString();
            byte[] sec = RequestUtil.getSecret();
            byte[] output = AESUtils.encryptRequest(sec, json);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream(output.length);
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(output);
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            ActivityInfoClickReuqest regRequest = new ActivityInfoClickReuqest(compressed);
            this.loadHttpContent(regRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHttpSucceed(int type, BizResponse response) {
        Gson gson = new Gson();
        onCompleteSucceed(gson.fromJson(response.getRawResponse().toString(),
                RespProtocolPacket.class));
    }

    @Override
    protected void onHttpFailed(int type, HttpErrorBean error) {
        onCompleteFailed(type, error);
    }

}

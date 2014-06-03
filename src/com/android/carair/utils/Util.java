
package com.android.carair.utils;

import java.util.zip.CRC32;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;

import com.android.carair.api.Loc;
import com.android.carair.common.CarairConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Util {
    public static String checkSum(int deviceId, String mac, long ts) {
        try {
            CRC32 crc32 = new CRC32();
            crc32.update(mac.getBytes());
            crc32.update(String.valueOf(deviceId).getBytes("utf-8"));
            crc32.update(String.valueOf(ts).getBytes());
            return String.valueOf(crc32.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static void saveTimer(String time,Context context){
        try {
            try {
                SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
                String timer = sp.getString(CarairConstants.TIMER, "");
                JSONObject jo = null;
                if(!TextUtils.isEmpty(timer)){
                    jo = new JSONObject(timer);
                    JSONArray ja = jo.getJSONArray("timer");
                    ja.put(new JSONObject(time));
                }else{
                    jo = new JSONObject();
                    JSONArray ja = new JSONArray();
                    ja.put(new JSONObject(time));
                    jo.put("timer", ja);
                }
                Editor editor = sp.edit();
                editor.putString(CarairConstants.TIMER, jo.toString());
                editor.commit();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getTimer(Context context){
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            return sp.getString(CarairConstants.TIMER, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveRatio(int i, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            editor.putInt(CarairConstants.RATIO, i);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasRatio(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        if (sp.contains(CarairConstants.RATIO)) {
            return true;
        }
        return false;
    }

    public static int getRatio(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            return sp.getInt(CarairConstants.RATIO, CarairConstants.RATIO_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CarairConstants.RATIO_NORMAL;
    }

    public static void saveLoc(Loc loc, Context context) {
        if (!TextUtils.isEmpty(loc.getCity())) {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            if (!TextUtils.isEmpty(loc.getCity())) {
                editor.putString(CarairConstants.CITY, loc.getCity());
            }
            
            if(!TextUtils.isEmpty(loc.getDescription())){
                editor.putString(CarairConstants.DESCRIPION, loc.getDescription());
            }

//            if (!TextUtils.isEmpty(loc.getLat())) {
//                editor.putString(CarairConstants.LAT, loc.getLat());
//                editor.putString(CarairConstants.LNG, loc.getLng());
//            }

            editor.commit();
        }
    }

    public static Loc getSavedLoc(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        String city = sp.getString(CarairConstants.CITY, "");
        String des = sp.getString(CarairConstants.DESCRIPION, "");
//        String lat = sp.getString(CarairConstants.LAT, "");
//        String lng = sp.getString(CarairConstants.LNG, "");

        Loc loc = new Loc();
        loc.setCity(city);
        loc.setDescription(des);
//        loc.setLat(lat);
//        loc.setLng(lng);

        return loc;
    }

    public static long getTs() {
        long ts = System.currentTimeMillis();
        String ts_str = String.valueOf(ts);
        ts_str = ts_str.substring(0, 10);
        ts = Long.parseLong(ts_str);
        return ts;
    }

    /**
     * Bit转Byte
     */
    public static byte bitToByte(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {// 4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }
    
    public static String convertRatioString(int ratio){
        switch (ratio) {
            case CarairConstants.RATIO_HIGH:
                return "强劲控制";
            case CarairConstants.RATIO_LOW:
                return "轻度控制";
            case CarairConstants.RATIO_NORMAL:
                return "普通控制";
            default:
                return "";
        }
    }
    
    public static String converOnOffString(int on){
        switch (on) {
            case CarairConstants.ON:
                return "开启";
            case CarairConstants.OFF:
                return "关闭";
            default:
                return "";
        }
    }
    
    //获取控制器状态
    public static int decodeDevCtrl(String base64bytes,int type){
        int res = CarairConstants.OFF;
        try {
            byte[] bytes = Base64.decode(base64bytes, Base64.DEFAULT);
            if(bytes != null && bytes.length >0){
                byte states = bytes[0];
                switch (type) {
                    case CarairConstants.TYPE_RATIO:
                        if(((byte)(states >> 7 & 0x1))==0x1){//强劲控制
                            return CarairConstants.RATIO_HIGH;
                        }else if(((byte)(states >> 6 & 0x1))==0x1){
                            return CarairConstants.RATIO_NORMAL;//普通控制
                        }else if(((byte)(states >> 5 & 0x1))==0x1){
                            return CarairConstants.RATIO_LOW;//轻度控制
                        }
                        break;
                    case CarairConstants.TYPE_AUTO_CLEAN:
                        if(((byte)(states >> 4 & 0x1))==0x1){
                            return CarairConstants.ON;
                        }
                        break;
                    case CarairConstants.TYPE_TIMER_ENABLE:
                        if(((byte)(states >> 3 & 0x1))==0x1){
                            return CarairConstants.ON;
                        }
                        break;
                    default:
                        break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    // 0 1 2 3 4 5 6 7
    // 强劲控制
    // 1——打开
    // 0——关闭 普通控制
    // 1——打开
    // 0——关闭 轻度控制
    // 1——打开
    // 0——关闭 自动净化
    // 1——打开
    // 0——关闭 定时启动
    // 1——打开
    // 0——关闭 保留 保留 保留
    public static int getStatus(Context context) {
        int ratio = getRatio(context);
        char[] s = new char[] {
                '0', '0', '0', '0', '0', '0', '0', '0'
        };
        switch (ratio) {
            case CarairConstants.RATIO_HIGH:
                s[0] = '1';
                break;
            case CarairConstants.RATIO_LOW:
                s[2] = '1';
                break;
            case CarairConstants.RATIO_NORMAL:
                s[1] = '1';
                break;
            default:
                break;
        }

        String str = new String(s);
        int i = Integer.valueOf(str, 2);
        return i;
    }

    public static String getDevctrl(boolean isopen) {
        byte[] deStr = getBaseDevctrl();
        // deStr[0] = bitToByte("01000000");
        // deStr[0] = bitToByte("01000000");
        if (isopen) {
            deStr[0] = 0x01;
        } else {
            deStr[0] = 0x00;
        }
        return Base64.encodeToString(deStr, Base64.DEFAULT);
    }

    // 解析净化器当前状态
    public int decodeDevCtrl(String devctrl) {
        byte[] deStr = Base64.decode(devctrl, Base64.DEFAULT);
        return 0;
    }

    //
    public static byte[] getBaseDevctrl() {
        byte[] devctrl = new byte[32];
        // devctrl[0] = 0x10; //版本
        // devctrl[1] = 0x65; //ip
        // devctrl[2] = 0x65; //接受方ip
        // devctrl[3] = 127; //供应商代码
        // devctrl[4] = 0xFF-256; //DEVICEID
        // devctrl[5] = 0xFF-256; //DEVICEID
        // devctrl[6] = 0x01; //id
        // devctrl[7] = 0x01; //Time
        // devctrl[8] = 0x01; //pm2.5
        // devctrl[9] = 0x01; //key校验
        // devctrl[10] = 0x01; //key校验
        // devctrl[12] = 0xBB-256; //key校验
        // devctrl[13] = 0xff-256;//A
        // devctrl[14] = 0xff-256;//B
        // devctrl[15] = 0xCB-256;//C
        return devctrl;
    }
}

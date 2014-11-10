
package com.android.carair.utils;

import java.util.zip.CRC32;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;

import com.android.carair.api.Activity;
import com.android.carair.api.Loc;
import com.android.carair.api.Store;
import com.android.carair.common.CarAirManager;
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

    public static void saveTimer(String time, Context context) {
        try {
            try {
                SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
                String timer = sp.getString(CarairConstants.TIMER, "");
                JSONObject jo = null;
                if (!TextUtils.isEmpty(timer)) {
                    jo = new JSONObject(timer);
                    JSONArray ja = jo.getJSONArray("timer");
                    ja.put(new JSONObject(time));
                } else {
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

    public static String getTimer(Context context) {
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

    public static int getWindStatusHeader(Context context, int wind) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            int status = sp.getInt(CarairConstants.STATUS, 0);
            // 根据本地设置拼装status
            String str = byteToBit((byte) status);
            char[] s = str.toCharArray();
            // int ratio = getRatio(context);
            // int autoClean = getAutoClean(context);
            switch (wind) {
                case CarairConstants.RATIO_HIGH:
                    s[7] = '1';
                    s[6] = '0';
                    s[5] = '0';
                    break;
                case CarairConstants.RATIO_LOW:
                    s[5] = '1';
                    s[6] = '0';
                    s[7] = '0';
                    break;
                case CarairConstants.RATIO_AUTO:
                    s[6] = '1';
                    s[5] = '0';
                    s[7] = '0';
                    break;
                default:
                    break;
            }
            // if(!isOn){
            // s[7] = '0';
            // s[6] = '0';
            // s[5] = '0';
            // }
            // if (CarairConstants.ON == autoClean) {
            // s[3] = '1';
            // }
            // if (isOn) {
            // s[3] = '0';
            // } else {
            // s[3] = '1';
            // }
            int i = Integer.valueOf(new String(s), 2);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getStatusHeader(Context context, boolean isOn, boolean useBattery) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            int status = sp.getInt(CarairConstants.STATUS, 0);
            // 根据本地设置拼装status
            String str = byteToBit((byte) status);
            char[] s = str.toCharArray();
            // int ratio = getRatio(context);
            // int autoClean = getAutoClean(context);
            // switch (ratio) {
            // case CarairConstants.RATIO_HIGH:
            // s[7] = '1';
            // break;
            // case CarairConstants.RATIO_LOW:
            // s[5] = '1';
            // break;
            // case CarairConstants.RATIO_AUTO:
            // s[6] = '1';
            // break;
            // default:
            // break;
            // }
             if(useBattery){
             s[7] = '0';
             s[6] = '1';
             s[5] = '0';
             }
            // if (CarairConstants.ON == autoClean) {
            // s[3] = '1';
            // }
            if (isOn) {
                s[3] = '0';
            } else {
                s[3] = '1';
            }
            int i = Integer.valueOf(new String(s), 2);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void saveStatusHeader(int i, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            editor.putInt(CarairConstants.STATUS, i);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAutoClean(int i, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            editor.putInt(CarairConstants.AUTO_CLEAN, i);
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
            return sp.getInt(CarairConstants.RATIO, CarairConstants.RATIO_HIGH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CarairConstants.RATIO_HIGH;
    }

    public static int getAutoClean(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            return sp.getInt(CarairConstants.AUTO_CLEAN, CarairConstants.OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CarairConstants.OFF;
    }

    public static void saveActivity(Activity activity, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            JSONObject jo = new JSONObject();
            jo.put("title", activity.getTitle());
            jo.put("url", activity.getUrl());
            jo.put("id", activity.getId());
            jo.put("is_new", activity.getIs_new());
            jo.put("type", activity.getType());
            editor.putString(CarairConstants.ACTIVITY, jo.toString());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveStore(Store store, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            JSONObject jo = new JSONObject();
            jo.put("title", store.getTitle());
            jo.put("url", store.getUrl());
            jo.put("id", store.getId());
            jo.put("is_new", store.getIs_new());
            jo.put("type", store.getType());
            editor.putString(CarairConstants.STORE, jo.toString());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBadge(int badge, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            JSONObject jo = new JSONObject();
            jo.put("badge", badge);
            editor.putString(CarairConstants.BAEGE, jo.toString());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getBadge(Context context) {
        int badge = 0;
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            badge = sp.getInt("badge", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badge;
    }

    public static Activity getActivity(Context context) {
        Activity activity = null;
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            String joStr = sp.getString(CarairConstants.ACTIVITY, "");
            if (!TextUtils.isEmpty(joStr)) {
                JSONObject jo = new JSONObject(joStr);
                activity = new Activity();
                activity.setTitle(jo.optString("title", ""));
                activity.setId(jo.optString("id", ""));
                activity.setUrl(jo.optString("url", ""));
                activity.setIs_new(jo.optString("is_new", ""));
                activity.setType(jo.optString("type", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }
    
    public static Store getStore(Context context) {
        Store activity = null;
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            String joStr = sp.getString(CarairConstants.STORE, "");
            if (!TextUtils.isEmpty(joStr)) {
                JSONObject jo = new JSONObject(joStr);
                activity = new Store();
                activity.setTitle(jo.optString("title", ""));
                activity.setId(jo.optString("id", ""));
                activity.setUrl(jo.optString("url", ""));
                activity.setIs_new(jo.optString("is_new", ""));
                activity.setType(jo.optString("type", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    public static void saveWarningPM(int warning, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            editor.putInt(CarairConstants.WARNING_PM, warning);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveWarningHarmful(int harmful, Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            editor.putInt(CarairConstants.WARNING_HARMFUL, harmful);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getWarningPM(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            return sp.getInt(CarairConstants.WARNING_PM, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 200;
    }

    public static int getWarningHarmful(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            return sp.getInt(CarairConstants.WARNING_HARMFUL, 15);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 15;
    }

    public static void saveLoc(Loc loc, Context context) {
        if (!TextUtils.isEmpty(loc.getCity())) {
            SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
            Editor editor = sp.edit();
            // if (!TextUtils.isEmpty(loc.getCity())) {
            String city = "";
            try {
                city = loc.getCity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.putString(CarairConstants.CITY, city);
            // }

            if (!TextUtils.isEmpty(loc.getDescription())) {
                editor.putString(CarairConstants.DESCRIPION, loc.getDescription());
            }

            editor.commit();
        }
    }

    public static void saveLocation(Context context, String lat, String lng) {
        // SharedPreferences sp =
        // context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        // Editor editor = sp.edit();
        // if (!TextUtils.isEmpty(lat)) {
        // editor.putString(CarairConstants.LAT, lat);
        // }
        //
        // if (!TextUtils.isEmpty(lng)) {
        // editor.putString(CarairConstants.LNG, lng);
        // }
        // editor.commit();

        CarAirManager.getInstance().setLat(lat);
        CarAirManager.getInstance().setLng(lng);
    }

    public static void clearLocation() {
        CarAirManager.getInstance().setLat("");
        CarAirManager.getInstance().setLng("");
    }

    public static String[] getLocation(Context context) {
        // SharedPreferences sp =
        // context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        String[] result = new String[2];
        // result[0] = sp.getString(CarairConstants.LAT, "");
        // result[1] = sp.getString(CarairConstants.LNG, "");
        result[0] = CarAirManager.getInstance().getLat();
        result[1] = CarAirManager.getInstance().getLng();
        return result;
    }

    public static void saveDeviceId(String id, Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        Editor editor = sp.edit();

        if (!TextUtils.isEmpty(id)) {
            editor.putString(CarairConstants.DEVICE_KEY_ID, id);
        }
        editor.commit();
    }

    public static String getDeviceId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        return sp.getString(CarairConstants.DEVICE_KEY_ID, "");
    }

    public static void clearDeviceId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        Editor editor = sp.edit();
        if (sp.contains(CarairConstants.DEVICE_KEY_ID)) {
            editor.remove(CarairConstants.DEVICE_KEY_ID);
        }
        editor.commit();
    }

    public static Loc getSavedLoc(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        String city = sp.getString(CarairConstants.CITY, "");
        String des = sp.getString(CarairConstants.DESCRIPION, "");
        String lat = sp.getString(CarairConstants.LAT, "");
        String lng = sp.getString(CarairConstants.LNG, "");

        Loc loc = new Loc();
        loc.setCity(city);
        loc.setDescription(des);

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

    /**
     * 把byte转为字符串的bit
     */
    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    public static int decodeStatus(int i) {
        byte status = (byte) i;
        String s = byteToBit(status);
        char[] c = s.toCharArray();
        if ('1' == c[7] && '0' == c[6] && '0' == c[5]) {
            return CarairConstants.RATIO_HIGH;
        } else if ('1' == c[5] && '0' == c[7] && '0' == c[6]) {
            return CarairConstants.RATIO_LOW;
        } else if ('1' == c[6] && '0' == c[7] && '0' == c[5]) {
            return CarairConstants.RATIO_AUTO;
        } else {
            return -1;
        }
    }

    public static String convertRatioString(int ratio) {
        switch (ratio) {
            case CarairConstants.RATIO_HIGH:
                return "强劲控制";
            case CarairConstants.RATIO_LOW:
                return "轻度控制";
            case CarairConstants.RATIO_AUTO:
                return "普通控制";
            default:
                return "";
        }
    }

    public static String converOnOffString(int on) {
        switch (on) {
            case CarairConstants.ON:
                return "开启";
            case CarairConstants.OFF:
                return "关闭";
            default:
                return "";
        }
    }

    // 获取控制器状态
    public static int decodeDevCtrl(String base64bytes, int type) {
        int res = CarairConstants.OFF;
        try {
            byte[] bytes = Base64.decode(base64bytes, Base64.DEFAULT);
            if (bytes != null && bytes.length > 0) {
                byte states = bytes[0];
                switch (type) {
                    case CarairConstants.TYPE_RATIO:
                        if (((byte) (states >> 0 & 0x1)) == 0x1) {// 强劲控制
                            return CarairConstants.RATIO_HIGH;
                        } else if (((byte) (states >> 1 & 0x1)) == 0x1) {
                            return CarairConstants.RATIO_AUTO;// 普通控制
                        } else if (((byte) (states >> 2 & 0x1)) == 0x1) {
                            return CarairConstants.RATIO_LOW;// 轻度控制
                        }
                        break;
                    case CarairConstants.TYPE_AUTO_CLEAN:
                        if (((byte) (states >> 3 & 0x1)) == 0x1) {
                            return CarairConstants.ON;
                        }
                        break;
                    case CarairConstants.TYPE_TIMER_ENABLE:
                        if (((byte) (states >> 4 & 0x1)) == 0x1) {
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
    // public static int getStatus(Context context) {
    // int ratio = getRatio(context);
    // int autoClean = getAutoClean(context);
    // char[] s = new char[] {
    // '0', '0', '0', '0', '0', '0', '0', '0'
    // };
    // switch (ratio) {
    // case CarairConstants.RATIO_HIGH:
    // s[0] = '1';
    // break;
    // case CarairConstants.RATIO_LOW:
    // s[2] = '1';
    // break;
    // case CarairConstants.RATIO_NORMAL:
    // s[1] = '1';
    // break;
    // default:
    // break;
    // }
    // if (CarairConstants.ON == autoClean) {
    // s[3] = '1';
    // }
    // String str = new String(s);
    // int i = Integer.valueOf(str, 2);
    // return i;
    // }

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

    public static boolean convertIsTimerOn(int i) {
        byte b = (byte) i;
        if (((byte) ((b >> 0) & 0x1)) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static int setRepeatOn(boolean isUsed, int repeat) {
        byte b = (byte) repeat;
        if (isUsed) {
            if (((byte) ((b >> 0) & 0x1)) == 0) {
                b += 1;
            }
        } else {
            if (((byte) ((b >> 0) & 0x1)) == 1) {
                b -= 1;
            }
        }
        return b;
    }

    public static int statusToDevCtrl(int i) {
        // byte b = (byte) i;
        // if (((byte) ((b >> 4) & 0x1)) == 1) {
        // return 1;
        // } else {
        // return 0;
        // }

        byte status = (byte) i;
        String s = byteToBit(status);
        char[] c = s.toCharArray();
        if (c[3] == '0') {
            return 1;
        } else {
            return 0;
        }
    }

    public static String convertRepeat(int i) {
        byte b = (byte) i;
        StringBuffer sb = new StringBuffer();
        if (((byte) ((b >> 7) & 0x1)) == 1) {
            sb.append("日,");
        }
        if (((byte) ((b >> 6) & 0x1)) == 1) {
            sb.append("六,");
        }
        if (((byte) ((b >> 5) & 0x1)) == 1) {
            sb.append("五,");
        }
        if (((byte) ((b >> 4) & 0x1)) == 1) {
            sb.append("四,");
        }
        if (((byte) ((b >> 3) & 0x1)) == 1) {
            sb.append("三,");
        }
        if (((byte) ((b >> 2) & 0x1)) == 1) {
            sb.append("二,");
        }
        if (((byte) ((b >> 1) & 0x1)) == 1) {
            sb.append("一,");
        }
        String str = sb.toString();
        if (TextUtils.isEmpty(str)) {
            return "";
        } else {
            return str.substring(0, str.lastIndexOf(","));
        }
    }

    public static int getPMColor(int progress) {
        if (progress <= CarairConstants.PM_GREEM) {
            return 0xff64b8f7;
        } else if (progress <= CarairConstants.PM_BLUE && progress >= CarairConstants.PM_GREEM) {
            return 0xff64b8f7;
        } else {
            return 0xffb9566b;
        }
    }

    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static boolean isFirstLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        return sp.getBoolean(CarairConstants.FIRSTLOGININ, true);
    }

    public static void setFirstLogin(Context context, boolean login) {
        SharedPreferences sp = context.getSharedPreferences(CarairConstants.PREFERENCE, 0);
        Editor editor = sp.edit();
        editor.putBoolean(CarairConstants.FIRSTLOGININ, login);
        editor.commit();
    }
}

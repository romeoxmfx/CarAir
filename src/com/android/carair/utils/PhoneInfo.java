
package com.android.carair.utils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * 获取手机信息，例如imei、imsi、mac地址等等
 */
public class PhoneInfo {

    public static final String IMEI = "imei";
    public static final String IMSI = "imsi";
    public static final String MACADDRESS = "mac_address";

    private static String generateImei() {
        StringBuffer imei = new StringBuffer();

        // 添加当前秒数 毫秒数 5位
        long time = System.currentTimeMillis();
        String currentTime = Long.toString(time);
        imei.append(currentTime.substring(currentTime.length() - 5));

        // 手机型号 6位
        StringBuffer model = new StringBuffer();
        model.append(Build.MODEL.replaceAll(" ", ""));
        while (model.length() < 6) {
            model.append('0');
        }
        imei.append(model.substring(0, 6));

        // 随机数 4位
        Random random = new Random(time);
        long tmp = 0;
        while (tmp < 0x1000) {
            tmp = random.nextLong();
        }

        imei.append(Long.toHexString(tmp).substring(0, 4));

        return imei.toString();

    }

    /**
     * 获取imei，如果系统不能获取，则将动态产生一个唯一标识并保存
     * 
     * @param context Context实例
     * @return imsi字串
     */
    public static String getImei(Context context) {
        String imei = null;
        if (imei == null || imei.length() == 0) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Activity.TELEPHONY_SERVICE);
            imei = tm.getDeviceId(); // 获取imei的方法修改
            if (imei == null || imei.length() == 0) {
                imei = generateImei();
            }
            imei = imei.replaceAll(" ", "").trim();
            // imei 小于15位补全 jiuwan
            while (imei.length() < 15) {
                imei = "0" + imei;
            }
        }
        return imei.trim();
    }

    /**
     * 获取imsi，如果系统不能获取，则将动态产生一个唯一标识并保存
     * 
     * @param context ： Context实例
     * @return imsi字串
     */
    static public String getImsi(Context context) {
        String imsi = null;
        if (imsi == null || imsi.length() == 0) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Activity.TELEPHONY_SERVICE);
            imsi = tm.getSubscriberId();
            if (imsi == null || imsi.length() == 0) {
                imsi = generateImei();
            }
            imsi = imsi.replaceAll(" ", "").trim();
            // imei 小于15位补全 jiuwan
            while (imsi.length() < 15) {
                imsi = "0" + imsi;
            }
        }
        return imsi;
    }

    /**
     * 获取wifi 模块mac地址
     * 
     * @param context ： Context实例
     * @return wifi模块mac地址
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String wifiaddr = info.getMacAddress();
        return wifiaddr;
    }

    /**
     * 获取原始的imei，如果没有返回空字符串，
     * 
     * @param context : Context实例
     * @return 手机原生imei，获取失败则返回null
     */
    static public String getOriginalImei(Context context) {

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (imei != null)
            imei = imei.trim();
        return imei;
    }

    /**
     * 获取原始的imsi，如果没有返回空字符串，
     * 
     * @param context : Context实例
     * @return 原生imsi，获取失败则返回null
     */
    static public String getOriginalImsi(Context context) {

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi != null)
            imsi = imsi.trim();
        return imsi;
    }

    public static String getSerialNum() {
        String serialnum = null;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
        } catch (Exception ignored) {
        }

        return serialnum;
    }

    public static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    private static DisplayMetrics metrics = null;
    private static int screenWidthDp = 0;
    private static double screenInch = 0;

    public static float getDeviceDensity(Context context) {
        if (metrics == null) {
            WindowManager windowMgr = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            if (windowMgr != null) {
                Display display = windowMgr.getDefaultDisplay();
                metrics = new DisplayMetrics();
                display.getMetrics(metrics);
            }
        }
        return metrics.density;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getOsVersion() {
        return "android " + Build.VERSION.RELEASE;
    }

    public static PackageInfo getAppInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        if (windowMgr != null) {
            Display display = windowMgr.getDefaultDisplay();
            int screenWidth = display.getWidth();

            return screenWidth;
        }
        return 0;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        if (windowMgr != null) {
            Display display = windowMgr.getDefaultDisplay();
            int screenHeight = display.getHeight();

            return screenHeight;
        }
        return 0;
    }

    public static int getScreenWidthWithDp(Context context) {
        if (screenWidthDp == 0) {
            if (metrics != null && metrics.density != 0) {
                screenWidthDp = (int) (metrics.widthPixels / metrics.density);
            } else {
                WindowManager windowMgr = (WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE);
                if (windowMgr != null) {
                    Display display = windowMgr.getDefaultDisplay();
                    metrics = new DisplayMetrics();
                    display.getMetrics(metrics);
                    if (metrics.density != 0) {
                        screenWidthDp = (int) (metrics.widthPixels / metrics.density);
                    } else {
                    }

                }
            }
        } else {
        }
        return screenWidthDp;
    }

    public static double getScreenSizeWithInch(Context context) {
        if (screenInch == 0) {
            if (metrics != null) {
                screenInch = Math.sqrt(Math.pow(
                        (metrics.widthPixels / metrics.xdpi), 2)
                        + Math.pow((metrics.heightPixels / metrics.ydpi), 2));
            } else {
                WindowManager windowMgr = (WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE);
                if (windowMgr != null) {
                    Display display = windowMgr.getDefaultDisplay();
                    metrics = new DisplayMetrics();
                    display.getMetrics(metrics);
                    screenInch = Math
                            .sqrt(Math.pow(
                                    (metrics.widthPixels / metrics.xdpi), 2)
                                    + Math.pow(
                                            (metrics.heightPixels / metrics.ydpi),
                                            2));
                }
            }
        } else {
        }
        return screenInch;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

}

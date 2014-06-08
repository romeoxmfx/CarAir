
package com.android.carair.utils;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

/**
 * 提供获取设备信息和App信息的工具类库
 */
public class DeviceConfig {
    public static final int DEFAULT_TIMEZONE = 8;

    protected static final String CLASS_NAME = DeviceConfig.class.getName();
    protected static final String UNKNOW = "Unknown";

    private static final String MOBILE_NETWORK = "2G/3G";
    private static final String WIFI = "Wi-Fi";

    public static String replaceWrap(String origin) {
        String target = null;

        if (origin != null) {
            target = origin.replace("\r\n", "");
            target = target.replace("\r", "");
            target = target.replace("\n", "");
        }

        return target;
    }

    public static double getPoint(double data) {
        double d = data;
        int fInt = (int) d;
        BigDecimal b1 = new BigDecimal(Double.toString(d));
        BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
        double dPoint = b1.subtract(b2).floatValue();
        return dPoint;
    }

    public static boolean arrayEquals(byte[] one, byte[] two) {
        if (one != null && two != null && one.length == two.length) {
            int len = one.length;

            for (int i = 0; i < len; i++) {
                if (one[i] != two[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static String getEmulatorValue() {
        return Build.MODEL;
    }

    public static int getCurrentTime() {
        long time = System.currentTimeMillis() / 1000;
        return (int) time;
    }

    public static String getIMSI(Context context) {
        String IMSI = null;

        if (context != null) {
            TelephonyManager telManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            IMSI = telManager.getSubscriberId();
            if(TextUtils.isEmpty(IMSI)){
                IMSI = "";
            }
        }

        return IMSI;
    }

    public static String getManufacturer() {
        String manu = Build.MANUFACTURER;
        if (manu != null && manu.length() > 32) {
            manu = manu.substring(0, 32);
        }
        return manu;
    }

    public static int getAvailMemory(Context context) {
        int availMemory = -1;

        if (context != null) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo mi = new MemoryInfo();
            am.getMemoryInfo(mi);
            availMemory = new Long(mi.availMem / (1024 * 1024)).intValue();
        }

        return availMemory;
    }

    public static int getNetTraffic(Context context) {
        int num = 0;
        long netTraL = 0;
        int netTra = -1;
        String memPath = "/proc/net/dev";// 系统内存信息文件
        String info;
        String[] sizeInfo;
        FileReader reader = null;
        BufferedReader bufReader = null;

        try {
            reader = new FileReader(memPath);
            bufReader = new BufferedReader(reader, 1024);

            while (((info = bufReader.readLine()) != null) && (num < 2)) {
                if (info.indexOf("rmnet0:") != -1
                        || info.indexOf("wlan0:") != -1) {
                    sizeInfo = info.split("\\d+");
                    netTraL += Long.valueOf(sizeInfo[1]);
                    netTraL += Long.valueOf(sizeInfo[2]);

                    num++;
                }
            }

            if (num == 2) {
                netTra = new Long(netTraL / 1024).intValue();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufReader = null;
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }

        return netTra;
    }

    /**
     * 判断是否已安装App
     * 
     * @param packageInfo
     *            App包名
     * @param context
     * @return
     */
    public static boolean isAppInstalled(String packageInfo, Context context) {
        boolean installed = false;

        if (context != null) {
            PackageManager pm = context.getPackageManager();

            try {
                pm.getPackageInfo(packageInfo, PackageManager.GET_ACTIVITIES);
                installed = true;
            } catch (PackageManager.NameNotFoundException e) {
                installed = false;
            }
        }

        return installed;
    }

    /**
     * 判断系统语言是否为简体中文
     * 
     * @param context
     *            用于获取系统设置的语言环境
     * @return 当前设备系统语言是否为简体中文 (zh_CN) 时返回true,否则返回false (zh_TW,zh_,en...)
     */
    public static boolean isChinese(Context context) {
        boolean isChinese = false;

        if (context != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            isChinese = locale.toString().equals(Locale.CHINA.toString());
        }

        return isChinese;
    }

    /**
     * 获取当前设备已安装的应用集合 </p>
     * 
     * @param context
     *            用户获取已安装应用信息
     * @return 已安装应用的package信息集合
     */
    public static Set<String> getInstalledPackages(Context context) {
        Set<String> set = new HashSet<String>();

        if (context != null) {
            List<PackageInfo> pInfos = context.getPackageManager()
                    .getInstalledPackages(0);
            for (int i = 0; i < pInfos.size(); i++) {
                PackageInfo pInfo = pInfos.get(i);
                set.add(pInfo.packageName);
            }
        }
        return set;
    }

    /**
     * 判断设备当前屏幕方向是否为竖向
     * 
     * @param context
     * @return 当且仅当设备竖直显示返回 true 否则 返回 false
     */
    public static boolean isScreenPortrait(Context context) {
        if (context != null
                && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取宿主应用的版本号 (versionCode)
     * 
     * @param context
     * @return 宿主应用程序的版本号，即Mandifest 中的 versionCode 如果没指定则返回“Unknown”
     */
    public static String getAppVersionCode(Context context) {
        String verCode = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                int version_code = pInfo.versionCode;
                verCode = String.valueOf(version_code);
            } catch (NameNotFoundException e) {
            }
        }

        return verCode;
    }

    /**
     * 读取应用的版本（version name）
     * 
     * @param context
     * @return 返回 version name， 没有返回“Unknown”
     */
    public static String getAppVersionName(Context context) {
        String version = null;

        if (context != null) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                version = pInfo.versionName;
            } catch (NameNotFoundException e) {
                version = DeviceConfig.UNKNOW;
                Log.d(CLASS_NAME,
                        "Get app version exception,info:" + e.toString());
            }
        }

        return version;
    }

    /**
     * 判断当前应用是否具有指定的权限
     * 
     * @param context
     * @param permission
     *            权限信息的完整名称 如：<code>android.permission.INTERNET</code>
     * @return 当前仅当宿主应用含有 参数 permission 对应的权限 返回true 否则返回 false
     */
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;

        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }

        return result;
    }

    /**
     * 获取Application 中定义的label
     * 
     * @param context
     * @return
     */
    public static String getAppLabel(Context context) {
        String applicationName = "";

        if (context != null) {
            final PackageManager pm = context.getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(context.getPackageName(), 0);
            } catch (final NameNotFoundException e) {
                ai = null;
            }
            applicationName = (String) (ai != null ? pm.getApplicationLabel(ai)
                    : "");
        }

        return applicationName;
    }

    /**
     * 读取GPU信息
     * 
     * @param gl
     *            openGL实例
     * @return 返回数组 String[]{GPU Vender, GPU Reader}, 读不到将返回 String[]{null}
     */
    public static String[] getGPU(GL10 gl) {
        try {
            String[] res = new String[2];
            String vendor = gl.glGetString(GL10.GL_VENDOR);
            String renderer = gl.glGetString(GL10.GL_RENDERER);
            res[0] = vendor;
            res[1] = renderer;
            return res;
        } catch (Exception e) {
            Log.e(DeviceConfig.CLASS_NAME, "Could not read gpu infor:", e);
            return new String[0];
        }
    }

    /**
     * 获取设备的 Cpu 信息
     * 
     * @return 设备 Cpu 型号信息 可能返回""
     */
    public static String getCPU() {
        String cpuInfo = null;

        FileReader fstream = null;
        BufferedReader in = null;

        try {
            fstream = new FileReader("/proc/cpuinfo");
            if (fstream != null) {
                try {
                    in = new BufferedReader(fstream, 1024);
                    cpuInfo = in.readLine();
                    in.close();
                    fstream.close();
                } catch (IOException e) {
                    Log.e(DeviceConfig.CLASS_NAME,
                            "Could not read from file /proc/cpuinfo", e);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(DeviceConfig.CLASS_NAME, "Could not open file /proc/cpuinfo",
                    e);
        }

        if (cpuInfo != null) {
            int start = cpuInfo.indexOf(':') + 1;
            cpuInfo = cpuInfo.substring(start);
        }

        return cpuInfo != null ? cpuInfo.trim() : "";
    }

    /**
     * 获取设备唯一标识 </p> 注意：需要权限 android.permission.READ_PHONE_STATE
     * 
     * @param context
     * @return 返回设备唯一标识IMEI码 , 如果读取不到，将返回MAC 地址，MAC
     *         地址获取不到，返回Secure.ANDROID_ID。都获取不到返回""。
     */
    public static String getDeviceId(Context context) {
        String imei = "";

        if (context != null) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (tm != null) {
                try {
                    if (checkPermission(context, permission.READ_PHONE_STATE)) {
                        imei = tm.getDeviceId();
                    }
                } catch (Exception ex) {
                    Log.w(CLASS_NAME, "No IMEI.", ex);
                }

                if (TextUtils.isEmpty(imei)) {
                    Log.w(CLASS_NAME, "No IMEI.");

                    imei = getMac(context);
                    if (TextUtils.isEmpty(imei)) {
                        Log.w(CLASS_NAME,
                                "Failed to take mac as IMEI. Try to use Secure.ANDROID_ID instead.");
                        imei = Secure.getString(context.getContentResolver(),
                                Secure.ANDROID_ID);
                        Log.i(CLASS_NAME, "getDeviceId: Secure.ANDROID_ID: "
                                + imei);
                    }
                }
            }
        }

        return imei;
    }

    /**
     * 获取运营商信息
     * 
     * @param context
     * @return the alphabetic name of current registered operator. 出错返回"Unknow"
     */
    public static String getNetworkOperatorName(Context context) {
        String value = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                value = tm.getNetworkOperatorName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * gsmLac            gsm位置区码                                (0,65535)      -1
     * gsmCid            gsm 小区识别码                              >=0            -1
     * gsmRssi           gsm 信号强度[-113,0]                        dbm           -1
     * cdmaSystemId      系统标识(类似区号每个地市只有一个）              (0,65535)      -1
     * cdmaNetworkId     网络标识。一个基站就是一个蜂窝系统和一个网络的成员。 (0,65535)      -1
     * cdmaBaseStationId 基站小区号                                  (0,65535)      -1
     * cdmaLon           cdma手机检测到的基站经度码                     >=0            -1
     * cdmaLat           cdma手机检测到的基站纬度码                     >=0            -1
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int[] getDeviceCellInfo(Context context) {
        int[] info = new int[]{-1,-1,-1,-1,-1,-1,-1,-1};
        if (context != null) {
            try {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                CellLocation cellLocation = tm.getCellLocation();
                if (cellLocation instanceof GsmCellLocation) {
                    GsmCellLocation gsmCell = (GsmCellLocation) cellLocation;
                    info[0] = gsmCell.getLac();
                    info[1] = gsmCell.getCid();
                } else if (cellLocation instanceof CdmaCellLocation) {
                    CdmaCellLocation cdmaCell = (CdmaCellLocation) cellLocation;
                    info[3] = cdmaCell.getSystemId();
                    info[4] = cdmaCell.getNetworkId();
                    info[5] = cdmaCell.getBaseStationId();
                    info[6] = cdmaCell.getBaseStationLongitude();
                    info[7] = cdmaCell.getBaseStationLatitude();
                }
                
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
                    List<CellInfo> allCellInfo = tm.getAllCellInfo();
                    CellInfoGsm gsmInfo = null;
                    if(allCellInfo != null){
                        for(CellInfo ci : allCellInfo){
                            if(ci instanceof CellInfoGsm){
                                gsmInfo = (CellInfoGsm) ci;
                                break;
                            }
                        }
                    }
                    
                    if(gsmInfo != null && gsmInfo.getCellSignalStrength() != null){
                        info[2] = gsmInfo.getCellSignalStrength().getDbm();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return info;
    }
    
    /**
     * Service Set Identifier（服务集标识），可用于区分运营商wifi
     * 
     * @param context
     * @return
     */
    public static String getSSID(Context context) {
        String ssid = "-1";

        if (context != null) {
            try {
                WifiManager wifi = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                if (checkPermission(context,
                        "android.permission.ACCESS_WIFI_STATE")) {
                    WifiInfo info = wifi.getConnectionInfo();
                    ssid = info.getSSID();
                } else {
                    Log.w(CLASS_NAME,
                            "Could not get mac address.[no permission android.permission.ACCESS_WIFI_STATE");
                }
            } catch (Exception e) {
                Log.w(CLASS_NAME, "Could not get mac address." + e.toString());
            }
        }

        return ssid;
    }
    
    /**
     * 获取设备屏幕分辨率
     * 
     * @param context
     * @return 如:800*480 出错返回"Unknow"
     */
    public static String getDisplayResolution(Context context) {
        String value = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) (context
                        .getSystemService(Context.WINDOW_SERVICE));
                wm.getDefaultDisplay().getMetrics(metrics);

                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                value = String.valueOf(height) + "*" + String.valueOf(width);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * Get the mobile network access mode.
     * 
     * @param context
     * @return A 2-elements String array, 1st specifies the network type, the
     *         2nd specifies the network subtype. If the network cannot be
     *         retrieved, "Unknown" is filled instead.
     */
    public static String[] getNetworkAccessMode(Context context) {
        String[] res = new String[] {
                DeviceConfig.UNKNOW, DeviceConfig.UNKNOW
        };

        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission.ACCESS_NETWORK_STATE,
                    context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                res[0] = DeviceConfig.UNKNOW;
                return res;
            }

            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                res[0] = DeviceConfig.UNKNOW;
                return res;
            } else {
                NetworkInfo wifi_network = connectivity
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifi_network.getState() == NetworkInfo.State.CONNECTED) {
                    res[0] = DeviceConfig.WIFI;
                    return res;
                }
                NetworkInfo mobile_network = connectivity
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobile_network.getState() == NetworkInfo.State.CONNECTED) {
                    res[0] = DeviceConfig.MOBILE_NETWORK;
                    res[1] = mobile_network.getSubtypeName();
                    return res;
                }
            }
        }
        return res;
    }

    public static boolean isWiFiAvailable(Context context) {
        return context != null ? WIFI.equals(DeviceConfig
                .getNetworkAccessMode(context)[0]) : false;
    }

    /**
     * <p>
     * Get the {@link android.location.Location#member Location}
     * </p>
     * <b>NOTE:</b> 需要权限：<code>android.permission.ACCESS_FINE_LOCATION
     * </p>
     * 
     * @param context
     * @return 如果没有权限或无法获取位置信息返回 null
     * 
     */
    public static Location getLocation(Context context) {
        if (context != null) {
            LocationManager lm = null;
            try {
                lm = (LocationManager) context
                        .getSystemService(Context.LOCATION_SERVICE);
                if (DeviceConfig.checkPermission(context,
                        "android.permission.ACCESS_FINE_LOCATION")) {
                    final Location lastKnownLocation = lm
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        Log.d(DeviceConfig.CLASS_NAME, "get location from gps:"
                                + lastKnownLocation.getLatitude() + ","
                                + lastKnownLocation.getLongitude());
                        return lastKnownLocation;
                    }
                }

                if (DeviceConfig.checkPermission(context,
                        "android.permission.ACCESS_COARSE_LOCATION")) {
                    final Location lastKnownLocationNet = lm
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (lastKnownLocationNet != null) {
                        Log.d(DeviceConfig.CLASS_NAME,
                                "get location from network:"
                                        + lastKnownLocationNet.getLatitude()
                                        + ","
                                        + lastKnownLocationNet.getLongitude());
                        return lastKnownLocationNet;
                    }
                }

                Log.d(DeviceConfig.CLASS_NAME,
                        "Could not get location from GPS or Cell-id, lack ACCESS_COARSE_LOCATION or ACCESS_COARSE_LOCATION permission?");
            } catch (Exception ex) {
                Log.e(DeviceConfig.CLASS_NAME, ex.getMessage());
            }
        }

        return null;
    }

    /**
     * <p>
     * True if the device is connected or connection to network.
     * </p>
     * 需要权限: <code>android.permission.ACCESS_NETWORK_STATE</code> </p>
     * 
     * @param context
     * @return 如果当前有网络连接返回 true 如果网络状态访问权限或没网络连接返回false
     */
    public static boolean isOnline(Context context) {
        boolean result = false;

        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null)
                result = ni.isConnectedOrConnecting();
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 判断Sd Card 是否可读可写
     * 
     * @return 当且仅当Sdcard既可读又可写返回 true 否则返回false
     */
    public static boolean isSdCardWrittenable() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * 根据系统设置的区域(locale)获取时区
     * 
     * @param context
     *            获取系统设置的区域
     * @return 返回所在时区 如 locale 为 zh_CN 将返回: 8
     */
    public static int getTimeZone(Context context) {
        int value = DEFAULT_TIMEZONE;

        if (context != null) {
            try {
                Locale locale = getLocale(context);
                Calendar calendar = Calendar.getInstance(locale);
                if (calendar != null) {
                    value = calendar.getTimeZone().getRawOffset()
                            / (3600 * 1000);
                }
            } catch (Exception e) {
                Log.i(CLASS_NAME, "error in getTimeZone", e);
            }
        }

        return value;
    }

    /**
     * 读取国家和语言
     * 
     * @param context
     * @return 返回数组 String[]{Country, Language}, 读不到返回
     *         String[]{"Unknown","Unknown"}
     */
    public static String[] getLocaleInfo(Context context) {
        String[] cl = new String[2];

        if (context != null) {
            try {
                Locale locale = getLocale(context);

                if (locale != null) {
                    cl[0] = locale.getCountry();
                    cl[1] = locale.getLanguage();
                }

                if (TextUtils.isEmpty(cl[0]))
                    cl[0] = "Unknown";
                if (TextUtils.isEmpty(cl[1]))
                    cl[1] = "Unknown";
            } catch (Exception e) {
                Log.e(CLASS_NAME, "error in getLocaleInfo", e);
            }
        }

        return cl;

    }

    /**
     * 读取 user config locale , 取不到 返回 default locale
     * 
     * @param context
     * @return
     */
    private static Locale getLocale(Context context) {
        Locale locale = null;

        if (context != null) {
            try {
                Configuration userConfig = new Configuration();
                Settings.System.getConfiguration(context.getContentResolver(),
                        userConfig);
                if (userConfig != null) {
                    locale = userConfig.locale;
                }
            } catch (Exception e) {
                Log.e(CLASS_NAME, "fail to read user config locale");
            }

            if (locale == null) {
                locale = Locale.getDefault();
            }
        }

        return locale;
    }

    /**
     * 读取 Umeng Appkey
     * 
     * @param context
     * @return 返回 Appkey
     */
    public static String getAppkey(Context context) {
        String appkey = null;

        if (context != null) {
            try {
                PackageManager manager = context.getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(
                        context.getPackageName(), 128);

                if (info != null) {
                    String id = info.metaData.getString("UMENG_APPKEY");
                    if (id != null) {
                        appkey = id.trim();
                    } else {
                        Log.e(CLASS_NAME,
                                "Could not read UMENG_APPKEY meta-data from AndroidManifest.xml.");

                    }
                }
            } catch (Exception e) {
                Log.e(CLASS_NAME,
                        "Could not read UMENG_APPKEY meta-data from AndroidManifest.xml.",
                        e);
            }
        }

        return appkey;
    }

    /**
     * 读取手机MAC地址
     * 
     * @param context
     * @return 返回mac地址
     */
    public static String getMac(Context context) {
        String mac = null;

        if (context != null) {
            try {
                WifiManager wifi = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                if (checkPermission(context,
                        "android.permission.ACCESS_WIFI_STATE")) {
                    WifiInfo info = wifi.getConnectionInfo();
                    mac = info.getMacAddress();
                } else {
                    Log.w(CLASS_NAME,
                            "Could not get mac address.[no permission android.permission.ACCESS_WIFI_STATE");
                }
            } catch (Exception e) {
                Log.w(CLASS_NAME, "Could not get mac address." + e.toString());
            }
        }

        return mac;
    }

    /**
     * 读取分辨率
     * 
     * @param context
     * @return 返回分辨率 width*height ,否则返回 Unknown
     */
    public static String getResolution(Context context) {
        String value = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) (context
                        .getSystemService(Context.WINDOW_SERVICE));
                wm.getDefaultDisplay().getMetrics(metrics);

                int width = -1, height = -1;

                if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES) == 0) {
                    width = reflectMetrics(metrics, "noncompatWidthPixels");
                    height = reflectMetrics(metrics, "noncompatHeightPixels");
                }

                if (width == -1 || height == -1) {
                    width = metrics.widthPixels;
                    height = metrics.heightPixels;
                }

                StringBuffer msb = new StringBuffer();
                msb.append(width);
                msb.append("*");
                msb.append(height);

                value = msb.toString();
            } catch (Exception e) {
                Log.e(CLASS_NAME, "read resolution fail", e);
            }
        }

        return value;

    }

    private static int reflectMetrics(Object metrics, String field) {
        try {
            Field f = DisplayMetrics.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.getInt(metrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 读取运营商信息
     * 
     * @param context
     * @return 返回运营商信息，否则返回 Unknown
     */
    public static String getOperator(Context context) {
        String value = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                value = ((TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE))
                        .getNetworkOperatorName();
            } catch (Exception e) {
                Log.i(CLASS_NAME, "read carrier fail", e);
            }
        }

        return value;
    }

    /**
     * 读取当前时间
     * 
     * @param date
     * @return 返回格式 [yyyy-MM-dd HH:mm:ss]
     */
    public static String getTimeString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(date);
        return time;
    }

    /**
     * 读取当前日期
     * 
     * @return 返回格式 [yyyy-MM-dd]
     */
    public static String getToday() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String time = df.format(date);
        return time;
    }

    /**
     * 将字符产解析成 Date 对象
     * 
     * @param strDay
     *            输入字符串格式 [yyyy-MM-dd HH:mm:ss]
     * @return 返回 Date 对象， 否则 返回 null
     */
    public static Date toTime(String strDay) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sf.parse(strDay);
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取一段时间区间，单位为秒
     * 
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @return 返回时间区间，但是为秒
     */
    public static int getIntervalSeconds(Date startTime, Date endTime) {
        if (startTime.after(endTime)) {
            Date cal = startTime;
            startTime = endTime;
            endTime = cal;
        }
        long sl = startTime.getTime();
        long el = endTime.getTime();
        long ei = el - sl;
        return (int) (ei / (1000));
    }

    /**
     * 读取渠道信息
     * 
     * @param context
     * @return 返回渠道号，否则返回 Unknown
     */
    public static String getChannel(Context context) {
        String channel = DeviceConfig.UNKNOW;

        if (context != null) {
            try {
                PackageManager manager = context.getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(
                        context.getPackageName(), 128);

                if (info != null && info.metaData != null) {
                    Object idObject = info.metaData.get("UMENG_CHANNEL");
                    if (idObject != null) {
                        String id = idObject.toString();
                        if (id != null) {
                            channel = id;
                        } else {
                            Log.i(CLASS_NAME,
                                    "Could not read UMENG_CHANNEL meta-data from AndroidManifest.xml.");
                        }
                    }
                }
            } catch (Exception e) {
                Log.i(CLASS_NAME,
                        "Could not read UMENG_CHANNEL meta-data from AndroidManifest.xml.");
            }
        }

        return channel;
    }

    /**
     * 返回包名
     * 
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        return context != null ? context.getPackageName() : "";
    }

    /**
     * 返回应用名
     * 
     * @param context
     * @return
     */
    public static String getApplicationLable(Context context) {
        return context != null ? context.getPackageManager()
                .getApplicationLabel(context.getApplicationInfo()).toString()
                : "";
    }

    /**
     * 返回当前应用所在工程的状态
     * 
     * @param context
     * @return True 工程在Debug 状态， False release 状态
     */
    public static boolean isDebug(Context context) {
        boolean result = false;

        try {
            result = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
        }

        return result;
    }
    
    public static String getOsVersion(){
        return Build.VERSION.RELEASE;
    }
}


package com.android.carair.utils;

import java.util.zip.CRC32;

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
}

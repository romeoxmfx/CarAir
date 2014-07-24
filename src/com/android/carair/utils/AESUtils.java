
package com.android.carair.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.taobao.protostuff.Output;

import com.android.carair.common.CarairConstants;

/**
 * @author carlos carlosk@163.com
 * @version 创建时间：2012-5-17 上午9:48:35 类说明
 */

public class AESUtils {
    public static final String TAG = "AESUtils";

    public static String encrypt(String seed, String clearText) {
        // Log.d(TAG, "加密前的seed=" + seed + ",内容为:" + clearText);
        byte[] result = null;
        try {
            byte[] rawkey = getRawKey(seed.getBytes());
            result = encrypt(rawkey, clearText.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String content = toHex(result);
        // Log.d(TAG, "加密后的内容为:" + content);
        return content;

    }

    public static String encrypt(byte[] seed, String clearText) {
        // Log.d(TAG, "加密前的seed=" + seed + ",内容为:" + clearText);
        byte[] result = null;
        try {
            // byte[] rawkey = getRawKey(seed);
            result = encrypt(seed, clearText.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String content = bytesToHexString(result);
        // Log.d(TAG, "加密后的内容为:" + content);
        return content;

    }

    public static byte[] encryptRequest(byte[] seed, String clearText) {
        // Log.d(TAG, "加密前的seed=" + seed + ",内容为:" + clearText);
        byte[] result = null;
        byte[] output = null;
        try {
            // byte[] rawkey = getRawKey(seed);
            result = encrypt(seed, clearText.getBytes());
            byte[] iv = CarairConstants.tempIV.getBytes();
            byte[] salt = CarairConstants.tempSalt.getBytes();
            output = new byte[result.length + 32];
            System.arraycopy(result, 0, output, 0, result.length);
            System.arraycopy(iv, 0, output, result.length, 16);
            System.arraycopy(salt, 0, output, result.length + 16, 16);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // String content = bytesToHexString(result);
        // Log.d(TAG, "加密后的内容为:" + content);
        return output;
    }

    public static String decryptResponse(byte[] response) {
        byte[] iv = new byte[16];
        byte[] salt = new byte[16];
        byte[] res = new byte[response.length -32]; 
        String result = null;
        try {
            System.arraycopy(response, response.length - 16, salt, 0, 16);
            System.arraycopy(response, response.length - 32, iv, 0, 16);
            System.arraycopy(response, 0, res, 0, response.length - 32);
            byte[] sec = RequestUtil.getSecret(salt);
            result = decryptResponse(sec,res,iv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String decrypt(String seed, String encrypted) {
        // Log.d(TAG, "解密前的seed=" + seed + ",内容为:" + encrypted);
        byte[] rawKey;
        try {
            rawKey = getRawKey(seed.getBytes());
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            String coentn = new String(result);
            // Log.d(TAG, "解密后的内容为:" + coentn);
            return coentn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static byte[] getRawKeyScan(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(256, sr);
        SecretKey sKey = kgen.generateKey();
        byte[] raw = sKey.getEncoded();

        return raw;
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr);
        SecretKey sKey = kgen.generateKey();
        byte[] raw = sKey.getEncoded();

        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        String ivStr = RequestUtil.getRandomString(16);
        CarairConstants.tempIV = ivStr;
        byte[] iv = ivStr.getBytes();

        // Cipher cipher = Cipher.getInstance("AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
        // new byte[cipher.getBlockSize()]));
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
                iv));
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        // Cipher cipher = Cipher.getInstance("AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String decryptScan(byte[] raw, byte[] encrypted, byte[] iv) {
        String coentn = null;
        try {
            // byte[] rawKey = getRawKeyScan(raw);
            byte[] rawKey = new byte[32];
            System.arraycopy(raw, 0, rawKey, 0, 32);
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            // byte[] enc = toByte(encrypted);
            // Cipher cipher = Cipher.getInstance("AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                    iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            coentn = new String(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return coentn;
    }
    
    public static String decryptResponse(byte[] raw, byte[] encrypted, byte[] iv) {
        String coentn = null;
        try {
            // byte[] rawKey = getRawKeyScan(raw);
            byte[] rawKey = new byte[16];
            System.arraycopy(raw, 0, rawKey, 0, 16);
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            // byte[] enc = toByte(encrypted);
            // Cipher cipher = Cipher.getInstance("AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                    iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            coentn = new String(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return coentn;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        final String HEX = "0123456789ABCDEF";
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}

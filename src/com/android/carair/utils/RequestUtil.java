
package com.android.carair.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.android.airhelper.common.CarairConstants;

public class RequestUtil {
    private static final String PASSWORD = "8a3fe1ee64b0fb338eaf8aa2fce878d7e92b43128c42e1b48b4b84144df467b9";
    
    public static byte[] getSecret(byte[] salt) {
//      String content[] = new String[2];
      int KEY_SIZE = 16;
      byte[] result = new byte[KEY_SIZE];
      try {
          // 生成随机字符串
//          salt = "8a3fe1ee64b0fb33";
//           content[0] = salt;
          int DERIVATION_ROUNDS = 37;
          String derivedKey = PASSWORD;
          byte[] temp = derivedKey.getBytes();
          for (int i = 0; i < DERIVATION_ROUNDS; i++) {
              byte[] tempSalt = new byte[temp.length + salt.length];
              System.arraycopy(temp, 0, tempSalt, 0, temp.length);
              System.arraycopy(salt, 0, tempSalt, temp.length, salt.length);
              temp = getMD5(tempSalt);
          }
          // derivedKey = derivedKey.substring(0, KEY_SIZE);
          // result[1] = derivedKey;
          System.arraycopy(temp, 0, result, 0, KEY_SIZE);
//          content[1] = new String(result);
      } catch (Exception e) {
          e.printStackTrace();
      }
      return result;
  }

    public static byte[] getSecret() {
//        String content[] = new String[2];
        int KEY_SIZE = 16;
        byte[] result = new byte[KEY_SIZE];
        String salt = null;
        try {
            // 生成随机字符串
             salt = getRandomString(16);
//            salt = "8a3fe1ee64b0fb33";
             CarairConstants.tempSalt = salt;
//             content[0] = salt;
            int DERIVATION_ROUNDS = 37;
            String derivedKey = PASSWORD;
            byte[] temp = derivedKey.getBytes();
            for (int i = 0; i < DERIVATION_ROUNDS; i++) {
                byte[] tempSalt = new byte[temp.length + salt.getBytes().length];
                System.arraycopy(temp, 0, tempSalt, 0, temp.length);
                System.arraycopy(salt.getBytes(), 0, tempSalt, temp.length, salt.getBytes().length);
                temp = getMD5(tempSalt);
            }
            // derivedKey = derivedKey.substring(0, KEY_SIZE);
            // result[1] = derivedKey;
            System.arraycopy(temp, 0, result, 0, KEY_SIZE);
//            content[1] = new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getRandomString(int length) { // length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static byte[] getMD5(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(b);
        byte[] m = md5.digest();// 加密
        // return getString(m);
        return m;
    }

    public static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }
    
    public static String bytesToHexString(byte[] src){       
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

    // public static String encrypt() throws Exception {
    // try {
    // String data = "Hello world!";
    // String[] keys = getSecret();
    // String key = keys[1];
    // String iv = "e92b43128c42e1b4";
    //
    // Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
    // int blockSize = cipher.getBlockSize();
    //
    // // byte[] dataBytes = data.getBytes();
    // int paddingLength = 16 - (data.length() % 16);
    // String paddedPlaintext = data+(char)paddingLength*paddingLength;
    // int plaintextLength = paddedPlaintext.getBytes().length;
    // if (plaintextLength % blockSize != 0) {
    // plaintextLength = plaintextLength + (blockSize - (plaintextLength %
    // blockSize));
    // }
    //
    // // byte[] plaintext = new byte[paddedPlaintext.getBytes().length];
    // // System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
    //
    // SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
    // IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
    //
    // cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
    // byte[] encrypted = cipher.doFinal(paddedPlaintext.getBytes());
    //
    // // return new BASE64Encoder().encode(encrypted);
    // return AESUtils.toHex(encrypted);
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

}

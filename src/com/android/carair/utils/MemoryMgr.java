package com.android.carair.utils;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class MemoryMgr {
	
	public static final int ERROR=-1;
	
	/**
     * 获取SD信息
     * 
     * @return
     */
    public static long getSDSize()
    {
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSize();/* Block的size */
        // int totalBlocks = statFs.getBlockCount();/* 总Block数量 */
        long availableBlocks = statFs.getAvailableBlocks();/* 有效Block数量 */
        return availableBlocks * blockSize;
    }

    /**
     * 判断存储卡是否存在
     * 
     * @return
     */
    public static boolean checkSDCard()
    {
    	String state = android.os.Environment.getExternalStorageState();
        if (state != null && state.equals(
                android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static long getAvailableInternalMemorySize() {      
        File path = Environment.getDataDirectory();      
        StatFs stat = new StatFs(path.getPath());      
        long blockSize = stat.getBlockSize();      
        long availableBlocks = stat.getAvailableBlocks();      
        return availableBlocks * blockSize;      
    } 
    
    static public long getAvailableExternalMemorySize() {      
        if(externalMemoryAvailable()) {      
            File path = Environment.getExternalStorageDirectory();      
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();  
            long availableBlocks = stat.getAvailableBlocks(); 
            return availableBlocks * blockSize;      
        } else {      
            return ERROR;      
        }      
    }  
    
    static public boolean externalMemoryAvailable() {     
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);     
    } 
}

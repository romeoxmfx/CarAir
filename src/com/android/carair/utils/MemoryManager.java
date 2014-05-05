package com.android.carair.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

public class MemoryManager {

	private Application mContext=null;
	//ActivityManager句柄
	private ActivityManager mActivityManager = null;
	
	//预设进程最大占用内存
	private String mProcessName;
	private int mMaxMemory = 0;
	private int	mThresholdMemory = 0;
	
	//内存监听者列表
	private HashMap<String,MemoryManagerListener> mListeners = null;

	private static MemoryManager mInstance = null;  
	
	
	private MemoryManager()
	{
		mListeners = new HashMap<String,MemoryManagerListener>();
	}
	
	public synchronized static MemoryManager getInstance()
	{
		if(mInstance == null) {   
		     mInstance = new MemoryManager();   
		}   
		return mInstance;
	}
	
	/*
	* @param	cnt Activity上下文
	 * @param	processName 进程名
	 * @param	maxPercent	最大内存占比：取值1-100
	 * @param	thresholdSize 内存阈值占比,取值1-100
	 * 调用lowMemoryInd，通知相应监听者释放内存
	 */
	public void Init(Application cnt,String processName,
			int maxPercent,int thresholdPercent)
	{
		mContext = cnt;
		mActivityManager = (ActivityManager)(cnt.getSystemService(Context.ACTIVITY_SERVICE));
		mProcessName = processName;		
		
		ProcessMemoryInfo info = getProcessMemoryInfo(mProcessName);
		mMaxMemory = info.mem_limit*1024/100*maxPercent;
		mThresholdMemory = info.mem_limit*1024/100*thresholdPercent;
	}
	
	/**
	 * 获取进程ID
	 *
	 * @return	如果找到目标进程，则返回目标进程id,否则返回-1
	 * 
	*/
	public int getProcessID()
	{
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
				.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			
			if(mProcessName.equals(appProcessInfo.processName)){
				int pid = appProcessInfo.pid;
				return pid;
			}
		}
		return -1;
	}
	
	
	
	/**
	 * 获得指定进程的进程信息
	 *
	 * @param	ProcessName 目标进程名
	 * @param	ProcessMaxMemory 预设进程最大占用内存值
	 * 	
	 * @return	如果找到目标进程，则返回目标进程信息,否则返回null
	*/
	private ProcessMemoryInfo getProcessMemoryInfo(String ProcessName) {
		int mem_dalvik=0;
		int mem_native=0;
		int mem_limit =0;
		
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			// 内核版本3.0，dump命令无法运行
			mem_native = 0;
			mem_dalvik = 0;
		} else {
			// 内核版本2.6
			StringBuffer temp = new StringBuffer();
			try {
				// String[] cmd = {"/system/bin/sh", "-c", "dumpsys meminfo " +
				// PACKAGE_NAME + " | grep 'allocated'"};
				String cmd = "dumpsys meminfo " + ProcessName;
				Process process = Runtime.getRuntime().exec(cmd);
				InputStreamReader isr = new InputStreamReader(
						process.getInputStream());
				char[] c = new char[1024];
				int len = 0;
				while ((len = isr.read(c)) != -1) {
					temp.append(new String(c, 0, len));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return new ProcessMemoryInfo(mem_dalvik,mem_native,mMaxMemory,mem_limit);
			}
			String result = temp.toString();
			int indexStart = result.indexOf("alloc");
			if (indexStart == -1) {
				// 没有进程
				mem_native = 0;
				mem_dalvik = 0;
			} else {
				//
				int indexStop2 = result.indexOf("size");
				if( indexStop2 >= 0 && indexStop2 < indexStart)
				{
					String limits = result.substring(indexStop2, indexStart);
					String[] tokens2 = limits.split("\\s+");
					if( tokens2 != null && tokens2.length >= 6)
					{
						mem_limit = Integer.valueOf(tokens2[5]).intValue();						
					}
				}
				
				
				int indexStop = result.indexOf("free");
				result = result.substring(indexStart, indexStop);
				String[] tokens = result.split("\\s+");
				
				mem_native = Integer.valueOf(tokens[1]).intValue();
				mem_dalvik = Integer.valueOf(tokens[2]).intValue();
			}
		}
//		TaoLog.Logw(TAG," "+mem_dalvik+"  "+mem_native);
		return new ProcessMemoryInfo(mem_dalvik,mem_native,mMaxMemory,mem_limit);
	}
	
	/**
	 * 获得系统总内存大小
	 *
	 * @return	系统总内存大小（字节单位）
	*/
	public long getSystemTotalMemory() {  
        String str1 = "/proc/meminfo";// 系统内存信息文件  
        String str2;  
        String[] arrayOfString;  
        long initial_memory = 0;  
  
        try {  
            FileReader localFileReader = new FileReader(str1);  
            BufferedReader localBufferedReader = new BufferedReader(  
                    localFileReader, 8192);  
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小  
            
            if(str2 != null && !"".equals(str2)){
                arrayOfString = str2.split("\\s+");  
                initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte  
                localBufferedReader.close();  
            }
  
        } catch (IOException e) {  
        }
        return initial_memory;  
    } 
		
	/**
	 * 获得系统可用内存信息
	 *
	 * @return 系统内存信息
	*/
	public long getSystemAvailMemory(){
		ActivityManager.MemoryInfo temp = new ActivityManager.MemoryInfo();
		mActivityManager.getMemoryInfo(temp);
		long freeSize = temp.availMem;
		return freeSize;
	}
	
	/**
	 * 获取进程内存信息
	 *
	 * 	
	 * @return	如果找到进程，则返回进程内存信息,否则返回null
	*/
	public ProcessMemoryInfo getMemoryInfo()
    {
		ProcessMemoryInfo info = getProcessMemoryInfo(mProcessName);
		return info;
    }
	
	/**
	 * 获取进程里面本地资源的native内存信息
	 *
	 * 	
	 * @return	如果手机支持dump的话，则返回实际大小。
	 * 一般此函数返回0，仅仅用于跟踪，调试
	*/
	public int getNativeResourceMemorySize()
	{
		return Dump.getResourceMemorySize(mContext);
	}
	
	public int getListenerMemorySize()
	{
		int size=0;
		Iterator<Entry<String, MemoryManagerListener>> iter = mListeners.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Entry<String, MemoryManagerListener> entry = iter.next(); 
//		    String key = (String) entry.getKey(); 
		    MemoryManagerListener val = (MemoryManagerListener) entry.getValue(); 
		    size+=val.onGetMemory();
		}
		return size;
	}
	
	/**
	 * 增加内存监听
	 *
	 *@param name 监听器名称
	 * @param listener 监听器
	 * 	
	*/
	public void addListener(String name,MemoryManagerListener listener)
	{
		mListeners.put(name,listener);
	}
	
	/**
	 * 删除内存监听
	 *
	 *
	 * @param name 监听器名称
	 * 	
	*/
	public void removeListener(String name)
	{
		mListeners.remove(name);
	}
	
	/**
	 * 内存管理监听接口
	 *
	*/
	public interface MemoryManagerListener {
		/**
		 * 设置内存最大占用值
		 *
		*/
		public void onSetMaxMemory(int size);
		
		/**
		 * 获取监听者占用的内存
		 *
		*/
		public int onGetMemory();
		/**
		 * 响应内存不够时候，监听者应该释放内存
		 *
		*/
	    public void onLowMemory();
	}
	

	/**
	 * 内存检测，如果内存不足，发起gc，通知监听者释放内存
	 *
	*/
	public void memoryCheck()
	{
		ProcessMemoryInfo info = getMemoryInfo();
		if(info.getUsedSize()>=mThresholdMemory){
			System.gc();
		}
		
		//check the memory info after call system.gc()
		info = getMemoryInfo();
		if(info.getUsedSize()>=mThresholdMemory){
			lowMemoryInd();
		}
		
		//check the memory info after taobao application free
		info = getMemoryInfo();
		if(info.getUsedSize()>=mThresholdMemory){
		}
		return;
	}
	
	/**
	 * 发现内存不够时候，通知MemoryManager
	 *
	*/
	public void lowMemoryInd()
	{
		onLowMemory();
	}
	
	/**
	 * 内存不够时候，通知各个监听者释放内存
	 *
	*/
	private void onLowMemory()
	{
		Iterator<Entry<String, MemoryManagerListener>> iter = mListeners.entrySet().iterator(); 
		while (iter.hasNext()) { 
			Entry<String, MemoryManagerListener> entry = iter.next(); 
//		    String key = (String) entry.getKey(); 
		    MemoryManagerListener val = (MemoryManagerListener) entry.getValue(); 
		    val.onLowMemory();
		}
	}
	
	//内存信息对象
	public class ProcessMemoryInfo {
		int mem_dalvik;
		int mem_native;
		int mem_max;
		int mem_limit; //dalvik能分到的最大内存
		/**
		 * 构造内存信息
		 *
		 * @param maxMemSize 占用的内存大小,单位为kb
		 * 
		*/
		public ProcessMemoryInfo(int mem_dalvik,int mem_native,int mem_max,int mem_limit)
		{
			this.mem_dalvik = mem_dalvik;
			this.mem_native = mem_native;
			this.mem_max = mem_max;
			this.mem_limit = mem_limit;
		}
		
		/**
		 * 获取预设的最大内存
		 * 
		 * @return 预设的最大内存
		*/
		public int getMaxSize() {
			return mem_max;
		}
		
		/**
		 * 获取占用内存大小
		 *
		 * @return 占用内存大小
		*/
		public int getUsedSize() {
			return mem_dalvik+mem_native;
		}
		
		/**
		 * 获取一个应用程序最大的内存限制大小
		 *
		 * @return 最大的内存限制大小
		*/
		public int getLimitSize()
		{
			return mem_limit;
		}
		/**
		 * 获取dalvik占用内存大小
		 *
		 * @return 占用内存大小
		*/
		public int getDalvikUsedSize() {
			return mem_dalvik;
		}
		
		/**
		 * 获取native占用内存大小
		 *
		 * @return 占用内存大小
		*/
		public int getNativeUsedSize() {
			return mem_native;
		}
		
		/**
		 * 获取剩余内存
		 *
		 * @return 剩余内存
		*/
		public int getFreeSize() {
			return getMaxSize() - getUsedSize();
		}
	}
}

package com.android.airhelper.common;

import com.android.carair.imagepool.ImagePool;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Looper;

/**
 * 该类为taosdk全局的接口类
 * @author xiaolong
 *
 */
public class CarairSDK {
	
	/**
	 * Don't let anyone instantiate this class.
	 */
	private CarairSDK() {};

	static boolean isInit = false; 
	static Object initLock = new Object();
	
	public static boolean waitInit()
	{
		
		
		
		if( !isInit)
		{
			
			if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()){
			}
			
			try
			{
				synchronized(initLock)
				{
					initLock.wait();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		return isInit;
	}
	
	private static void _init(Context context, String baseUrl, String appkey, boolean withSecurity) {
		if( !isInit)
		{
			
			SDKConfig.getInstance().setGlobalContext(context);
			
//			initCommonComponent(context,withSecurity);
			
			//cookie 初始化
			
			try
			{
				synchronized(initLock)
				{
					initLock.notifyAll();
				}
			}
			catch( Exception e  )
			{
				e.printStackTrace();
			}
			isInit = true;
		}
	}
	/**
	 *  为Taosdk工具类做初始化工作，在使用taosdk之前必须调用此方法
	 */
	public static void init(Context context, String baseUrl, String appkey) {
		
		_init(context,baseUrl,appkey,true);
		
	}
	
	/**
	 *  为Taosdk工具类做初始化工作，在使用taosdk之前必须调用此方法
	 *  传入appkey，此函数内不进行黑匣子初始化
	 */
	public static void initWithoutSecurity(Context context, String baseUrl, String appkey) {
		_init(context,baseUrl,appkey,false);
	}
	
	/**
	 *  为Taosdk工具类做初始化工作，在使用taosdk之前必须调用此方法
	 *  该初始化方法主要针对imagepool有需求的
	 */
	public static void init(Application context, String baseUrl, String appkey, String userAgent, String picPattern) {
		SDKConfig.getInstance().setGlobalContext(context);
		ImagePool.instance().Init(context, userAgent, picPattern);
	}
	
	/**
	 *  为Taosdk工具类做初始化工作，在使用taosdk之前必须调用此方法，
	 * @deprecated
	 */
//	public static void init(Context context, String appkey, String appSecret, String api3Base,String ttid, IEcodeProvider provider) {
//		
//		SDKConfig.getInstance().setGlobalContext(context)
//									   .setGlobalBaseUrl(api3Base)
//									   .setGlobalIEcodeProvider(provider)
//									   .setGlobalTTID(ttid)
//									   .setGlobalAppSecret(appSecret);
//		initCommonComponent(context,true);
//		
//	}
	
	/**
	 *  为Taosdk工具类做初始化工作，在使用taosdk之前必须调用此方法，
	 *  通过saveFileRootDir指定文件保存的根目录文件夹名称，若不指定，则默认为taobao
	 *  @deprecated
	 */
//	public static void init(Context context, String appkey, String appSecret, String api3Base,String ttid, IEcodeProvider provider, String saveFileRootDir) {
//		
//		SDKConfig.getInstance().setGlobalContext(context)
//		   .setGlobalAppkey(appkey)
//		   .setGlobalBaseUrl(api3Base)
//		   .setGlobalIEcodeProvider(provider)
//		   .setGlobalTTID(ttid)
//		   .setGlobalAppSecret(appSecret)
//		   .setGlobalSaveFileRootDir(saveFileRootDir);
//			
//		initCommonComponent(context,true);
//		
//	}
	
	
}

package com.android.airhelper.common;

import android.app.Application;
import android.content.Context;


/**
 * 
 * @author wb-wangxiaolong
 * 全局配置类，可以随时更改，获取配置
 *
 */
public class SDKConfig {
	/**
	 * Don't let anyone instantiate this class.
	 */
	private SDKConfig() {
		
	}
	
	private static class SingletonHolder{
		private static SDKConfig instance = new SDKConfig();
	}
	
	public static SDKConfig getInstance(){
		return SingletonHolder.instance;
	}
	
	/**
	 * 保存android平台所依赖的全局上下文引用
	 * 主要用于不依赖android平台特性的插件模块引用
	 * 在调用{@code sContext}之前，必须在Andriod平台下
	 * 初始化taosdk
	 * {@link  CarairSDK#init(Context)} or
	 * {@link CarairSDK#init(Application, String, String)}}
	 */
	private static Context sContext;
	
	/**
	 *  全局的Application
	 */
	private static Application sApplication;
	/**
	 *  配置是否对url参数加签
	 */
	private static boolean sIsSigned = true;
	
	
	/**
	 * 请求服务端的base url
	 * 调用之前{@link  CarairSDK#init(Context, String, String, String, String, IEcodeProvider)}
	 */
	private static String sApiBaseUrl;

	/**
	 * 应用ttid
	 */
	private static String sTTID;
	
	/**
	 * 应用的版本号
	 */
	private static String sVersion = null;
	
	/**
     * DNS缓存的过期时间
     */
    private static String sDnsExpireInterval = null;
	
	/**
	 * start
	 * UserTrack埋点需要的，开始
	 * 调用之前{@link  TaoSDK#init(Context, String, String)} 
	 */
//	private static String sAppkey;
//	private static String sAppSecret;
//	private static IEcodeProvider sProvider;
	/** end */
	
	/**
	 * 应用在sd卡中的文件存储根目录，主要用于图片等文件的缓存
	 */
	private static String sAppSaveFileRootDir = SDKConstants.STR_TAOBAO;
	
	public SDKConfig setGlobalIsSign(boolean sign) {
		sIsSigned = sign;
		return this;
	}
	
	public boolean getGlobalIsSign() {
		return sIsSigned;
	}
	
	public SDKConfig setGlobalContext(Context context) {
		sContext = context;
		return this;
	}
	
	public SDKConfig setGlobalApplication(Application application) {
		sApplication = application;
		return this;
	}
	
	public Application getGlobalApplication() {
		return sApplication;
	}

//	
//	public SDKConfig setGlobalAppkey(String appkey) {
//		sAppkey = appkey;
//		return this;
//	}
//	
//	public SDKConfig setGlobalAppSecret(String appSecret) {
//		sAppSecret= appSecret;
//		return this;
//	}
	
//	public String getGlobalAppSecret () {
//		return sAppSecret;
//	}
	
	public SDKConfig setGlobalBaseUrl(String baseUrl) {
		sApiBaseUrl = baseUrl;
		return this;
	}
	
	public SDKConfig setGlobalTTID(String ttid) {
		sTTID = ttid;
		return this;
	}
	
//	public SDKConfig setGlobalIEcodeProvider(IEcodeProvider ecodeProvider) {
//		sProvider = ecodeProvider;
//		return this;
//	}
	
	public SDKConfig setGlobalSaveFileRootDir(String  saveFileRootDir) {
		if ( !"".equals(saveFileRootDir) && null != saveFileRootDir  ) {
			 sAppSaveFileRootDir = saveFileRootDir;
		}
		return this;
	}
	
	public Context getGlobalContext() {
		return sContext;
	}
	
//	public String getGlobalAppkey() {
//		return sAppkey;
//	}
	
	public String getGlobalBaseUrl() {
		return sApiBaseUrl;
	}
	
	public String getGlobalTTID() {
		return sTTID;
	}
	
//	public IEcodeProvider getGlobalIEcodeProvider() {
//		return sProvider;
//	}
	
	public String getGlobalSaveFileRootDir() {
		return sAppSaveFileRootDir;
	}
	
	public SDKConfig setGlobalVersion(String version) {
		sVersion = version;
		return this;
	}
	
    public String getGlobalVersion() {
    	return sVersion;
    }	
    
    public SDKConfig setDnsExpireInterval(String interval) {
        sDnsExpireInterval = interval;
        return this;
    }
    
    public String getDnsExpireInterval() {
        return sDnsExpireInterval;
    }
}

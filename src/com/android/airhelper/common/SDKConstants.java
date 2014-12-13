package com.android.airhelper.common;


/**
 *  存放taosdk全局常量
 * @author xiaolong
 *
 */
public class SDKConstants {
	
	/**
	 * Don't let anyone instantiate this class.
	 */
	private SDKConstants() {};
	
	public static final String PIC_SEPARATOR = "|";	// separator in pic url: ***.jpg|***.webp
	public static final String STR_TAOBAO = "carair";
	
	public static final String KEY_TTID = "ttid";
	public static final String KEY_API = "api";
	public static final String KEY_VERSION= "v";
	public static final String KEY_ECODE = "ecode";
	public static final String KEY_DATA = "data";
	public static final String KEY_DEVICEID = "deviceId";
	public static final String KEY_IMEI = "imei";
	public static final String KEY_IMSI = "imsi";
	public static final String KEY_TIME = "t";
	public static final String KEY_APPKEY = "appKey";
	public static final String KEY_APPSECRET = "appSecret";
	public static final String KEY_SIGN = "sign";
	public static final String KEY_SID = "sid";
	public static final String KEY_UA = "wua";
	
	
	//这里定义UserTrack EventID SDK 从 21000-30000
	//必须按顺序排练
	//Page_ImgStat Page_ApiStat
	public static int ID_SDK_MIN = 21001; //最小值
			
	public static int ID_PAGE_HTTPS_CERT_ERR = 26666;
	public static int ID_PAGE_NW_SSLERROR = 26667;
	//public static int ID_PAGE_IMG_STAT = 26688; //总体的联网性能埋点，已经移除
	//public static int ID_PAGE_API_STAT = 26689;
	public static int ID_PAGE_SINGLE_API_STAT = 26690;
	public static int ID_PAGE_API_TIME_STAT = 27001;
	public static int ID_PAGE_SIGN_STAT = 27002;
	public static int ID_PAGE_NATIVE_WEBVIEW = 27003;
	public static int ID_PAGE_APROXY_ERROR = 27004; //用于APROXY Error调用时发现服务端返回Error Code的情况


    public static int ID_PAGE_JSON_EXCEPTION = 27005; //
    public static int ID_PAGE_PARSE_TASKID_ERR=27006;//解析taskId异常

   
    public static int ID_PAGE_HOST_IP=27007;//host-ip信息收集
    
    public static int ID_PAGE_SPDU_REPORT = 27008; //
    public static int ID_PAGE_SDK_LOCKED=27009;//解析taskId异常

    public static int ID_PAGE_SDK_BIGPIPE_DOWNGRADE =27010;//big pipe降级了
    public static final int ID_PAGE_SDK_DATA_LEN_INCONSISTENT = 27011;//反射数据和长度不一致
    public static final int ID_CACHESTATISTICS = 27012;//反射数据和长度不一致
    
    public static final int ID_PAGE_SDK_BASE = 29000;
   	public static final int ID_PAGE_SDK_41X_REDIRECT = ID_PAGE_SDK_BASE+17;
   	public static final int ID_PAGE_SDK_41X_UNLOCKED_SUCCESS = ID_PAGE_SDK_BASE+2;
   	public static final int ID_PAGE_SDK_420_LOCKED = ID_PAGE_SDK_BASE+3;
   	public static final int ID_PAGE_SDK_420_LOCKED_IN_10MIN = ID_PAGE_SDK_BASE+4;
   	public static final int ID_PAGE_SDK_DNS_CHANGE_IP = ID_PAGE_SDK_BASE+11;
   	public static final int ID_PAGE_SDK_DNS_CHANGE_IP_REQUEST_RESULT = ID_PAGE_SDK_BASE+12;
 	public static final int ID_PAGE_SDK_ASYNC_TASK = ID_PAGE_SDK_BASE+13;
 	public static final int ID_PAGE_SDK_ASYNC_TASK_PUSH = ID_PAGE_SDK_BASE+14;
 	public static final int ID_PAGE_SDK_ASYNC_TASK_TIMEOUT = ID_PAGE_SDK_BASE+15;
 	
 	public static final int ID_500ERR = ID_PAGE_SDK_BASE+16;
	
 	
 	public static final int ID_NETWORK_EXCP = 29998;
 	
 	//type----------------
	public static final int TYPE_CONN_EXECP = 4;
	public static final int TYPE_HTTPDNS_MSG = 5;	
 	public static final int TYPE_SPDY_FIRST = 99;
	
 	
 	public static final int ID_DEBUG_PERF = 29999; //
 	//type------------
 	public static final int TYPE_SPDY_DOWNGRADE = 1;
 	public static final int TYPE_SPDY_RESUE = 2;
 	public static final int TYPE_SPDY_SPEED = 3;
 
 	
 	
 	
 	
    public static int ID_SDK_MAX = 30000; //最大值
	
	
	//network ExceptionCode
	public static int CODE_REDIERECT_EXCEPTION = 1; //重定向异常，次数超过5次
	public static int CODE_APIOVERFLOW_EXCEPTION = 2; 
	public static int CODE_SOCKETTIMEOUT_EXCEPTION = 3; 
	public static int CODE_FILENOTFOUND_EXCEPTION = 4;
	public static int CODE_SSLHANDSHAKE_EXCEPTION = 5;
	public static int CODE_EOF_EXCEPTION = 6;
	public static int CODE_OTHER_EXCEPTION = 7;
	public static int CODE_DNSTHREAD_TIMEOUT = 10; //DNS线程没有在规定时间内解析完成，发送的请求
	public static int CODE_DNSPRE_EXCEPTION = 11; //DNS预解析异常
}

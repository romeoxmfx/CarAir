package com.android.carair.net;

import java.util.HashMap;

public class ErrorConstant {
	// 系统异常
	public final static int STSTEM_ERROR = -2000;
	public final static int ERRCODE_PROTOCOL_PARAM_LOST_FAILSE = STSTEM_ERROR - 1;// 参数错误
	public final static int ERRCODE_APP_ACCESS_API_FAIL = STSTEM_ERROR - 2;// 白名单
	public static final int ERRCODE_AUTH_REJECT = STSTEM_ERROR - 3;// 签名验证
	// 登录异常
	public static final int ERR_SID_INVALID = STSTEM_ERROR - 4;// sid无效
	public static final int ERR_HAS_HANDLER = STSTEM_ERROR - 5;
	
	//https 证书验证失败
	public static final int ERR_HTTPS_CERT_INVALID = STSTEM_ERROR - 6; //提示："安全证书由您没有选定信任的公司颁发！"
	public static final int ERR_HTTPS_CERT_EXPIRED = STSTEM_ERROR - 7; //提示："安全证书已过期或还未生效！"
	
	
	// 原api result 定义异常
	public static final int API_RESULT_SDCARD_WRITE_ERROR = -7;
	public static final int API_RESULT_TOO_LARGE_RESPOSE = -6;
	public static final int API_RESULT_REDIRECT_MANY = -5;
	public static final int API_RESULT_NETWORK_ERROR = -4;
	public static final int API_RESULT_DNS_ERROR = -3;
	public static final int API_RESULT_BAD_PARAM = -2;
	public static final int API_RESULT_USER_CANCEL = -1;
	public static final int API_RESULT_UNKNOWN = -1000;
	//public static final int API_RESULT_SUCCESS = HttpStatus.SC_OK;
	public static final int API_RESULT_FAILED = -10002;
	
	public static final String CODE_SUCCESS = "SUCCESS";
	public static final String CODE_ERR_PARAM = "PARAM_ERR";
	public static final String CODE_ERR_SID_INVALID = "ERR_SID_INVALID";
	public static final String CODE_ERR_OTHER = "FAIL";
	public static final String CODE_ERR_SYSTEM = "SYSTEM_ERROR";
	
	static HashMap<String, Integer> errCodeMap = new HashMap<String, Integer>();
	static {
		// 系统异常情况 err str 转 err code
		errCodeMap.put("ERRCODE_PROTOCOL_PARAM_LOST_FAILSE", ERRCODE_PROTOCOL_PARAM_LOST_FAILSE);
		errCodeMap.put("ERRCODE_APP_ACCESS_API_FAIL", ERRCODE_APP_ACCESS_API_FAIL);
		errCodeMap.put("ERRCODE_AUTH_REJECT", ERRCODE_AUTH_REJECT);
		// sid 异常
		errCodeMap.put("ERR_SID_INVALID", ERR_SID_INVALID);
	}

	/**
	 * 从返回的String形式的Error，获得ErrorCode
	 * 
	 * @param key
	 *            错误的字符串描述
	 * @return 返回错误码，见类的常量定义
	 */
	public static Integer getErrCodeByErrMsg(String key) {
		Integer errcode = null;
		errcode = errCodeMap.get(key);
		if (errcode == null) {
			errcode = API_RESULT_UNKNOWN;
		}
		return errcode;
	}
}

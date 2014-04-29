package com.android.carair.net;

import org.apache.http.HttpStatus;

import com.taobao.munion.utils.MuLogUtil;

import android.text.TextUtils;

/**
 * ApiResult 描述Api执行结果对象 包括结果值，描述，数据和服务端提供的过期时间 HTTP返回的Response Code定义参见
 * http://developer.android.com/reference/org/apache/http/HttpStatus.html
 * */
public class ApiResult {

	// HTTP Response Code定义 使用HttpStatus中的定义
	// http://developer.android.com/reference/org/apache/http/HttpStatus.html
	// 200 OK 表示成功

	// Server端返回的缺省失效时间
	public static final int DEFAULT_SERVER_EXPIRETIME = 30 * 24 * 3600; // 缺省365天，单位秒,

	// 成员定义
	/**
	 * 错误码 或者 HTTPResponse值，见常量定义
	 */
	public int resultCode; // 错误码 或者 HTTPResponse值，见常量定义
	
	/**
	 * 错误描述
	 */
	public String description; // 错误描述

	
	
	// API 返回码
	public String errCode; 
	// API 返回信息
	public String errDescription;
	
	/**
	 * 数据的有效时间，单位秒
	 */
	int expireTime; // 数据的有效时间，单位秒
	
	public int timeoutTime =0; //当链接失败时记录超时时间

	// 预定义的内部使用的ApiResult对象
	static ApiResult Cancelled = new ApiResult(ErrorConstant.API_RESULT_USER_CANCEL);
	static ApiResult BadParam = new ApiResult(ErrorConstant.API_RESULT_BAD_PARAM);
	static ApiResult DNSError = new ApiResult(ErrorConstant.API_RESULT_DNS_ERROR);

	/**
	 * 数据流
	 */
	public byte[] bytedata; // 数据流

	public Object data;// 解析后数据结果，对应api结果中data数组
	/**
	 * 构造函数
	 */
	public ApiResult() {
		resultCode = 0;
		bytedata = null;
		description = "";
		expireTime = DEFAULT_SERVER_EXPIRETIME;
	}
	/**
	 * 构造函数
	 */
	public ApiResult(int result) {
		resultCode = result;
		bytedata = null;
		description = "";
		expireTime = DEFAULT_SERVER_EXPIRETIME;
	}

	/**
	 * 构造函数
	 */
	public ApiResult(int res, String des, byte[] data) {
		resultCode = res;
		description = des;
		bytedata = data;
		expireTime = DEFAULT_SERVER_EXPIRETIME;
	}

	public boolean isSuccess() {
		return resultCode == HttpStatus.SC_OK;
	}
	
	public boolean isApiSuccess()
	{
		return TextUtils.equals(errCode,ErrorConstant.CODE_SUCCESS);
	}

}

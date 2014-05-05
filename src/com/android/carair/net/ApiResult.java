package com.android.carair.net;

import org.apache.http.HttpStatus;

import android.text.TextUtils;

/**
 * ApiResult 描述Api执行结果对象 包括结果值，描述，数据和服务端提供的过期时间 HTTP返回的Response Code定义参见
 * http://developer.android.com/reference/org/apache/http/HttpStatus.html
 * 其他Error Code 见ErrorConstant.java类中的常量定义
 * */
public class ApiResult {

	// HTTP Response Code定义 使用HttpStatus中的定义
	// http://developer.android.com/reference/org/apache/http/HttpStatus.html
	// 200 OK 表示成功

	// Server端返回的缺省失效时间
	static final int DEFAULT_SERVER_EXPIRETIME = 30 * 24 * 3600; // 缺省365天，单位秒,
	static final String NETWORK_NOT_AVAILABLE = "网络不可用，请检查您的手机是否联网！";

	
	// 成员定义
	/**
	 * 错误码 或者 HTTPResponse值，见常量定义
	 */
	@Deprecated
	public int resultCode; // 错误码 或者 HTTPResponse值，见常量定义
	
	

	/**
	 * 错误描述
	 */
	@Deprecated
	public String description; // 错误描述

	/**
	 * API 返回码
	 */
	@Deprecated
	public String errCode; 

	/**
	 *  API 返回信息
	 */
	@Deprecated
	public String errDescription;
	
	/**
	 * 数据的有效时间，单位秒
	 */
	int expireTime; // 数据的有效时间，单位秒
	
	@Deprecated
	public int timeoutTime =0; //当链接失败时记录超时时间

	// 预定义的内部使用的ApiResult对象
	static ApiResult Cancelled = new ApiResult(ErrorConstant.API_RESULT_USER_CANCEL);
	static ApiResult BadParam = new ApiResult(ErrorConstant.API_RESULT_BAD_PARAM);
	static ApiResult DNSError = new ApiResult(ErrorConstant.API_RESULT_DNS_ERROR);

	/**
	 * 数据流
	 */
	@Deprecated
	public byte[] bytedata; // 数据流

	@Deprecated
	public Object data;// 解析后数据结果，对应api结果中data数组
	
	private String spdyIP = ""; //标记请求是否在SPDY链路上完成
	

	private String errorData; //错误时返回的相关数据(例如 41x时的 redirectUrl)
	
    /**
	 * 同步接口返回的apiID,和异步接口返回的一样
	 */
	public ApiID syncApiID;
	
	
    public boolean isUseSpdy() {
		return spdyIP.length() > 1;
	}
	public void setSpdyIP(String ip) {
		spdyIP= ip;
	}
	public String getSpdyIP() {
		return spdyIP;
	}

	
	/**
	 * 异步化
	 */
//	private AsyncMtopResult asyncParam;
//	public AsyncMtopResult getAsyncResult() {
//		return asyncParam;
//	}
//	public void setAsyncParam(AsyncMtopResult asyncParam) {
//		this.asyncParam = asyncParam;
//	}
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

	/** 
	 * 网络请求是否成功
	 * 
	 * **/
	public boolean isSuccess() {
		return resultCode == HttpStatus.SC_OK;
	}
	
	/** 
	 * 网络请求成功的前提下，API调用的返回值是否成功
	 * 
	 * **/
	public boolean isApiSuccess()
	{
		return TextUtils.equals(errCode,ErrorConstant.CODE_SUCCESS);
	}
	
	/** 
	 * 获得失败时的错误描述
	 * 
	 * **/
	public String getDescription()
	{
		if(isSuccess())
		{
			return errDescription;
		}
		else
			return description;
	}

	//
	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getErrDescription() {
		return errDescription;
	}
	public void setErrDescription(String errDescription) {
		this.errDescription = errDescription;
	}
	public int getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}
	public int getTimeoutTime() {
		return timeoutTime;
	}
	public void setTimeoutTime(int timeoutTime) {
		this.timeoutTime = timeoutTime;
	}
	
	public byte[] getBytedata() {
		return bytedata;
	}
	public void setBytedata(byte[] bytedata) {
		this.bytedata = bytedata;
	}
	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isApiLockedResult() {
		// TODO Auto-generated method stub
		return (resultCode==420||resultCode==499||resultCode==599);
	}
	public boolean is41XResult(){
		return (resultCode>=410&&resultCode<=419);
	}
	public boolean isSystemError(){
		return ErrorConstant.CODE_ERR_SYSTEM.equalsIgnoreCase(getErrCode());
	}
	public String getErrorData() {
        return errorData;
    }
    public void setErrorData(String errorData) {
        this.errorData = errorData;
    }
    
    //定义了需要用UserTrack上报的结果
    boolean needReportResult()
    {
    	
    	if( resultCode == HttpStatus.SC_OK || 
//    	    resultCode == ErrorConstant.API_RESULT_ASYNC_REQUEST || 
    	    resultCode == ErrorConstant.API_RESULT_USER_CANCEL ||
    	    resultCode == HttpStatus.SC_NOT_FOUND 
    	  )
    	{
    		return false;
    	}
    	return true;
    }
    
    public boolean isTimeout()
    {
    	return (resultCode == ErrorConstant.API_RESULT_NETWORK_ERROR && this.timeoutTime > 0 );
    }
}

package com.android.carair.net;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class HttpErrorBean implements Parcelable
{
	
	public static final String EXTRA_KEY = "error_info";
	
	public static final int ERROR_UNDEFINE = -1; //本地错误
	public static final int ERROR_UNKONWN = -2; //未知错误
	public static final int ERROR_ILLEGAL_ARGUMENTS = -3;//illegal_arguments传入参数错误
	public static final int ERROR_ILLEGAL_CLIENT_TYPE = -4;//illegal_client_type错误的客户端类型
	public static final int ERROR_ILLEGAL_VERSION_VERSION = -5;//illegal_client_version错误的客户端版本
	public static final int ERROR_UNAUTHORIZED_USER = -6;//unauthorized_user用户登录Token验证错误
	public static final int ERROR_DIGEST_VALIDATE_FAIL = -7;//digest_validate_fail传入的请求参数签名验证错误
	public static final int ERROR_CATCHED_RESULT = -10;//异常捕获
	public static final int ERROR_FAILED_REQUEST = -50;//TOP返回处理失败
	public static final int ERROR_EXCEPTION_RESULT = -51;//TOP返回异常
	public static final int ERROR_SERVER_EXCEPTION = -52;//服务端异
	public static final int ERROR_COMMODITY_EXPIRED = -54;//宝贝下架
	public static final int ERROR_PASSWORD = 720; //密码错误
	public static final int ERROR_COLLECTION_FULL = 728; //收藏已满
	
	private int mErrorType = ERROR_UNDEFINE;
	private String mErrorDesc = "网络错误";
	
	public HttpErrorBean(JSONObject json) {
		parseError(json);
	}
	
	public int getErrorType() {
		return mErrorType;
	}
	
	public String getErrorDesc() {
		return mErrorDesc;
	}
	
	private void parseError(JSONObject json) {
		if (json == null) return;
		mErrorDesc = json.optString("desc");
		try {
		    mErrorType = Integer.parseInt(json.optString("code"));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return mErrorType + " : " + mErrorDesc;
	}

	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1)
	{
		// TODO Auto-generated method stub
		dest.writeInt(mErrorType);
		dest.writeString(mErrorDesc);
	}
	
	public void readFromParcel(Parcel in) {
		mErrorType = in.readInt();
		mErrorDesc = in.readString();
    }
	
	public static final Parcelable.Creator<HttpErrorBean> CREATOR = new
			Parcelable.Creator<HttpErrorBean>() {

				@Override
				public HttpErrorBean createFromParcel(Parcel source) {
					// TODO Auto-generated method stub
					return new HttpErrorBean(source);
				}

				@Override
				public HttpErrorBean[] newArray(int size) {
					// TODO Auto-generated method stub
					return new HttpErrorBean[size];
				}
		
	};
	
	private HttpErrorBean(Parcel source) {
		readFromParcel(source);
	}
	
}

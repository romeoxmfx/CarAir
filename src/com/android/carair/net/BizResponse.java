package com.android.carair.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.munion.common.MunionConfigManager;
import com.taobao.munion.utils.MuLogUtil;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * The response for HTTP request. Only support JSON protocol.
 */
public class BizResponse implements Cloneable
{
	public static final String TAG = "BizResponse";
	public static final boolean _DEBUG = false;
	
	public static final int HTTP_RESPONSE_OK =1;
	public static final int HTTP_FALIED = -1;
	public static final int HTTP_REQUEST_INVALIDATE = -100;
	public static final int ERROR_UNDEFINE = 0;
	
    public static final int HTTP_OK = 200;
    public static final String HTTP_SIGN_ILLEGAL = "800";
	

	public static final String EXTRA_KEY = "response";

	protected JSONObject mJSONObject;
	protected int mErrorCode = ERROR_UNDEFINE;
	
	protected ApiProperty mRequestApiProperty;

	public BizResponse(JSONObject json, ApiProperty requestApiProperty)
	{
		mJSONObject = json;
		if(mJSONObject != null && mJSONObject.optString("code").equals(HTTP_SIGN_ILLEGAL))
		{
			MunionConfigManager.getInstance().InitParams();
		}
		setRequestApiProperty(requestApiProperty);
	}

	public BizResponse(int errorCode)
	{
		mErrorCode = errorCode;
	}

	public boolean isSuccess()
	{
		boolean isRedirect = mRequestApiProperty.getConnectionHeader() != null && Integer.valueOf(mRequestApiProperty.getConnectionHeader().get(ApiConnector.RESPONSE_CODE)) == 302;
		return isRedirect || (mJSONObject != null ? mJSONObject.optBoolean("success") : false);
	}

	public void setErrorCode(int errorCode)
	{
		mErrorCode = errorCode;
	}

	public int getErrorCode()
	{
		return mErrorCode;
	}

	public JSONObject getJsonValue(String key)
	{
		return mJSONObject != null ? mJSONObject.optJSONObject(key) : null;
	}

	/**
	 */
	public Object getObjectValue(String key)
	{
		return mJSONObject != null ? mJSONObject.opt(key) : null;
	}

	public JSONObject getRawResponse()
	{
		return mJSONObject;
	}
	
	public JSONObject setRawResponse(JSONObject jso)
	{
		return mJSONObject = jso;
	}

	public boolean checkContainsKey(String key)
	{
		return mJSONObject != null ? mJSONObject.has(key) : false;
	}

	public void clean()
	{
		mJSONObject = null;
		mErrorCode = 0;
	}

	public Map<String, Object> parseJson2Map(JSONObject json)
	{
		Map<String, Object> response = new HashMap<String, Object>();
		for (Iterator<?> iter = json.keys(); iter.hasNext();)
		{
			String key = iter.next().toString();
			response.put(key, json.optString(key));
		}
		return response;
	}

	@Override
	public String toString()
	{
		return mJSONObject != null ? mJSONObject.toString() : "";
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		BizResponse response = (BizResponse) super.clone();
		return response;
	}
//
//	@Override
//	public int describeContents()
//	{
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags)
//	{
//		dest.writeString(mJSONObject.toString());
//	}

//	public static final Parcelable.Creator<BizResponse> CREATOR = new Parcelable.Creator<BizResponse>()
//	{
//		@Override
//		public BizResponse createFromParcel(Parcel source)
//		{
//			try
//			{
//				BizResponse response = new BizResponse(new JSONObject(source.readString()));
//				return response;
//			}
//			catch (JSONException e)
//			{
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//		@Override
//		public BizResponse[] newArray(int size)
//		{
//			return new BizResponse[size];
//		}
//	};

//	/**
//	 * 将数据流转化为BizResponse对象
//	 * 
//	 * @param in
//	 * @return
//	 */
//	public static BizResponse in2Response(InputStream in)
//	{
//		if (in == null)
//		{
//			return null;
//		}
//		JSONObject json = in2Json(in);
//		if (json == null)
//		{
//			return null;
//		}
//		BizResponse response = new BizResponse(json);
//		return response;
//	}

	/**
	 * 将数据流转化为JSONObject对象
	 * 
	 * @param in
	 * @return
	 */
	public static JSONObject in2Json(InputStream in)
	{
		if (in == null)
		{
			return null;
		}
		String responseString = in2Str(in);
		try
		{
			return new JSONObject(responseString);
		}
		catch (JSONException e)
		{
			Log.e(TAG, "JSONException ", e);
		}
		return null;
	}

	/**
	 * 将数据流转化为 String 对象
	 * 
	 * @param is
	 * @return
	 */
	public static String in2Str(InputStream is)
	{
		if (is == null)
		{
			return null;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer("");
		String line = null;
		String NL = System.getProperty("line.separator");
		try
		{
			while ((line = in.readLine()) != null)
			{
				sb.append(line + NL);
			}
		}
		catch (OutOfMemoryError e) {
			Log.e(TAG, "OutOfMemoryError", e);
		}
		catch (IOException e)
		{
			Log.e(TAG, "IOException ", e);
		}
		finally
		{
			try
			{
				if (is != null) is.close();
				if (in != null) in.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				Log.e(TAG, "IOException ", e);
			}
		}
		return sb.toString();
	}

	public ApiProperty getRequestApiProperty()
	{
		return mRequestApiProperty;
	}

	public void setRequestApiProperty(ApiProperty mRequestApiProperty)
	{
		this.mRequestApiProperty = mRequestApiProperty;
	}
}

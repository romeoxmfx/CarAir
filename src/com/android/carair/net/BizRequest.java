package com.android.carair.net;

import android.os.Parcel;
import android.os.Parcelable;

import com.taobao.munion.common.MunionConfigManager;
import com.taobao.munion.common.MunionConstants;
import com.taobao.munion.p4p.statistics.model.AnticheatInfo;
import com.taobao.munion.utils.MuLogUtil;
import com.taobao.munion.utils.MuTextUtil;
import com.taobao.munion.utils.ParameterDigestUtils;
import com.taobao.munion.utils.PhoneInfo;
import com.taobao.munion.utils.UtdidFromUmeng;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class BizRequest implements Cloneable, Parcelable, ConnectorHelper {
	private Map<String, Object> mRequestPairs = new TreeMap<String, Object>();
	protected ApiProperty mApiProperty;

	public BizRequest() {
	}

	public void addParam(String key, String value) {
		try {
			mRequestPairs.put(key,
					URLEncoder.encode(String.valueOf(value), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addParam(String key, long value) {
		mRequestPairs.put(key, String.valueOf(value));
	}

	public void addParams(Map<String, String> pairs) {
		mRequestPairs.putAll(pairs);
	}

	public void removeParam(String key) {
		mRequestPairs.remove(key);
	}

	public Object getValue(String key) {
		return mRequestPairs.get(key);
	}

	// public void setModule(String module)
	// {
	// mRequestPairs.put(MunionConstants.REQUEST_MODULE, module);
	// }
	//
	// public void setAction(String action)
	// {
	// mRequestPairs.put(MunionConstants.REQUEST_ACTION, action);
	// }

	public void setApi(String api) {
		mRequestPairs.put("api", api);
	}

	public String getApi() {
		Object apiObj = mRequestPairs.get("api");
		if (apiObj != null) {
			return apiObj.toString();
		} else {
			return "";
		}

	}

	public void parseJSONToRequest(JSONObject json) {
		if (json != null) {
			for (Iterator<?> iter = json.keys(); iter.hasNext();) {
				String key = iter.next().toString();
				mRequestPairs.put(key, json.optString(key));
			}
		}
	}

	/**
	 * 添加公用的request参数
	 */
	protected void addCommParams() {
		// Context context = MunionConfigManager.getInstance().getContext();
		// addParam(MunionConstants.REQUEST_PARAM_IMSI,
		// PhoneInfo.getImei(context));
		addParam("utdId", UtdidFromUmeng.getInstance().getUtdid());
		addParam(MunionConstants.REQUEST_PARAM_NAME,
				PhoneInfo.getAppInfo().packageName);
		addParam(MunionConstants.REQUEST_PARAM_CLIENT_VERSION,
				PhoneInfo.getAppInfo().versionName);
		addParam(MunionConstants.REQUEST_PARAM_CLIENT_SOURCE,
				MunionConstants.PLATFORM);
		addParam(MunionConstants.REQUEST_PARAM_API_VERSION,
				MunionConstants.API_VERSION);
		// addParam("screen_width", PhoneInfo.getScreenWidth(context));
		// addParam("screen_height", PhoneInfo.getScreenWidth(context));
		addParam("appKey", MunionConfigManager.getInstance().getAppKey());
		addParam("width", PhoneInfo.getScreenWidth());
		addParam("height", PhoneInfo.getScreenHeight());

		// addParam(MunionConstants.REQUEST_PARAM_IMSI,
		// PhoneInfo.getImsi(context));
		mRequestPairs.remove(MunionConstants.REQUEST_PARAM_DIGEST);
		if (!MuTextUtil.isNullOrEmpty(MunionConfigManager.sSecret)) {
			addParam(MunionConstants.REQUEST_PARAM_DIGEST,
					ParameterDigestUtils.digest(mRequestPairs,
							MunionConfigManager.sSecret));
		}

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mRequestPairs.toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		BizRequest request = (BizRequest) super.clone();
		request.mRequestPairs = new HashMap<String, Object>(mRequestPairs);
		return request;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeMap(mRequestPairs);
	}

	public static final Parcelable.Creator<BizRequest> CREATOR = new Parcelable.Creator<BizRequest>() {
		@SuppressWarnings("unchecked")
		@Override
		public BizRequest createFromParcel(Parcel source) {
			BizRequest request = new BizRequest();
			request.mRequestPairs = source.readHashMap(null);
			return request;
		}

		@Override
		public BizRequest[] newArray(int size) {
			return new BizRequest[size];
		}
	};

	@Override
	public String getApiUrl() {
		return MunionConfigManager.getInstance().getHost();
	}

	@Override
	public Object syncPaser(byte[] arg0) {
		String resString = new String(arg0);
		JSONObject responseJsonObject;
		try {
			if (mApiProperty.getConnectionHeader() != null
					&& Integer.valueOf(mApiProperty.getConnectionHeader().get(
							ApiConnector.RESPONSE_CODE)) == 302) {
				BizResponse httpResponse = new BizResponse(null, mApiProperty);
				return httpResponse;
			} else {
				responseJsonObject = new JSONObject(resString);
				BizResponse httpResponse = new BizResponse(responseJsonObject,
						mApiProperty);
				return httpResponse;
			}
		} catch (Exception e) {
			MuLogUtil.loge("syncPaser exception " + e.toString());
		}
		return null;
	}

	// public BizResponse sendRequest()
	// {
	// ApiProperty ap = new ApiProperty();
	// HashMap<String, String> header = new HashMap<String, String>();
	// header.put("Content-Type",
	// "application/x-www-form-urlencoded;charset=utf-8");
	// header.put(MunionConstants.REQUEST_HEAD_ACCEPT_ENCODING, "gzip");
	// ap.setConnectionHeader(header);
	//
	// byte[] data = null;
	// try
	// {
	// String postData = getPostData();
	// MuLogUtil.log("postData = " + postData);
	// data = postData.getBytes(HTTP.UTF_8);
	// } catch (UnsupportedEncodingException e)
	// {
	// e.printStackTrace();
	// }
	// if (data != null)
	// {
	// ap.setPostData(data);
	// BizResponse response = (BizResponse)
	// ApiRequestMgr.getInstance().syncConnect(this, ap);
	// return response;
	// }
	// else
	// {
	// return null;
	// }
	// }

	protected void setupApiProperty() {
		mApiProperty = new ApiProperty();
		setCommonHeaders(mApiProperty);
	}

	public BizResponse sendRequest() {
		if (!getApi().equals("com.taobao.alimama.favorite.getSecret")) {
			MunionConfigManager.getInstance().checkInitStatus();
		}
		setupApiProperty();
		String getData = getGetData();
		MuLogUtil.log("getData = " + getApiUrl() + getData);
		mApiProperty.setGetData(getData);
		BizResponse response = (BizResponse) ApiRequestMgr.getInstance()
				.syncConnect(this, mApiProperty);
		return response;
	}

	/**
	 * 添加request请求头参数
	 * 
	 * @param request
	 * @param headers
	 */
	protected void setCommonHeaders(ApiProperty ap) {
		if (ap.m_connHeaders == null) {
			ap.m_connHeaders = new HashMap<String, String>();
		}
		ap.m_connHeaders.put("Content-Type",
				"application/x-www-form-urlencoded;charset=utf-8");
		ap.m_connHeaders.put("Accept-Encoding", "gzip");
	}

	/**
	 * 添加request请求头参数
	 * 
	 * @param request
	 * @param headers
	 */
	protected void setAntiHeaders(ApiProperty ap) {
		if (ap.m_connHeaders == null) {
			ap.m_connHeaders = new HashMap<String, String>();
		}
		ap.m_connHeaders.put("User-Agent", MunionConstants.REQUEST_HEAD_UA);
		ap.m_connHeaders.put("Referer", "native null refer");

		try {
			String accept = AnticheatInfo.getInstance(
					MunionConfigManager.getInstance().getContext(), null)
					.encode();
			android.util.Log.i("statistics", "Http accept data: " + accept);
			ap.m_connHeaders.put("Accept", accept);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getGetData() {
		StringBuilder sb = new StringBuilder();
		addCommParams();
		Iterator<String> keys = mRequestPairs.keySet().iterator();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object value = mRequestPairs.get(key);

			sb.append(String.format("%s=%s", key, value));

			if (keys.hasNext()) {
				sb.append("&");
			}
		}

		return sb.toString().trim();
	}

}

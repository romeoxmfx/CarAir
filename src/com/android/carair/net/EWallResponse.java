package com.android.carair.net;

import org.json.JSONObject;

public class EWallResponse extends BizResponse
{

	public EWallResponse(JSONObject json, ApiProperty requestApiProperty)
	{
		super(json, requestApiProperty);
		// TODO Auto-generated constructor stub
	}

	public boolean isSuccess()
	{
		return mJSONObject != null ? true : false;
	}
}

package com.android.carair.request;

import com.android.carair.common.CarairConstants;
import com.android.carair.net.ApiProperty;
import com.android.carair.net.BizRequest;

public class ConfigRequest extends BizRequest {
    
    public ConfigRequest(String requestJson) {
        try {
            this.postData = requestJson.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ConfigRequest(byte[] requestJson) {
        try {
            this.postData = requestJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void setupApiProperty()
    {
        mApiProperty = new ApiProperty();
        mApiProperty.setPost(true);
        setCommonHeaders(mApiProperty);
    }
    
    @Override
    public String getApiUrl()
    {
        return CarairConstants.REQUEST_URL+"/app/config";
    }
}

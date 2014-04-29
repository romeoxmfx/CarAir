
package com.android.carair.net;

import org.json.JSONObject;

import com.taobao.munion.utils.MuLogUtil;
import com.umeng.newxp.net.NETConstants;

public class EWallRequest extends BizRequest
{
    @Override
    public String getApiUrl()
    {
        // return "http://w.m.taobao.com/api/q?";
        // return "http://10.232.135.111/api/q?";
        return NETConstants.REQUEST_URL_LIST[0];

    }

    // @Override
    // protected void setupApiProperty()
    // {
    // mApiProperty = new ApiProperty();
    // setAntiHeaders(mApiProperty);
    // }

    @Override
    protected void addCommParams()
    {
    }

    @Override
    public Object syncPaser(byte[] arg0)
    {
        String resString = new String(arg0);
        JSONObject responseJsonObject;
        try
        {
            responseJsonObject = new JSONObject(resString);
            EWallResponse httpResponse = new EWallResponse(responseJsonObject, mApiProperty);
            return httpResponse;
        } catch (Exception e)
        {
            MuLogUtil.loge("syncPaser exception " + e.toString());
        }
        return null;
    }
}

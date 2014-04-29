package com.android.carair.net;

import java.util.concurrent.Future;

public class ApiID 
{
	Future<ApiResult> m_future;
	ApiConnector m_conn;
	public ApiProperty m_apiProperty;

	ApiID(Future<ApiResult> id , ApiConnector conn)
	{
		m_future = id;
		m_conn = conn;
	}
}
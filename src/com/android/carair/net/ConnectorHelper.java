package com.android.carair.net;


/**
 * ConnectorHelper接口，定义了如何生成请求URL和解析返回的数据
 * 注：兼容历史的机制
 */
public interface ConnectorHelper {
	
	/**
	 * 获得请求的完整URL	
	 * @return 返回整个URL的链接地址	 
	 */
	public String getApiUrl();		                //返回整个URL的链接地址
	
	/**
	 * 同步解析器   用于将数据下载完成后的解析
	 * @all  返回的数据
	 * @return 返回应用解析出来的DO
	 */
	public Object syncPaser(byte[] all);	
		
	
}

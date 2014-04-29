/*----------------------------------------------------------------------------------------------
 *
 * Copyright (c) 2003-2011 Taobao.com 
 *
 * This file is Taobao property. Taobao's trade secret, proprietary and confidential information is contained in this file.
 * 
 * The information and code contained in this file is only for authorized Taobao employees to design, create, modify, or review.
 * 
 * DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER AUTHORIZATION.
 *
 * You shall not disclose confidential information and shall use it only in accordance with 
 * the terms of the contract agreement you entered into with Taobao.com
 *
 *-------------------------------------------------------------------------------------------------*/

/**
 * Filename : ApiConnectorStatusListenet.java
 * @owner : honghua.jcc
 * Creation time : 2011-11-30
 * Description : 
 */
package com.android.carair.net;

/**
 * ApiConnectorStatusListener接口
 * 定义了所有ApiConnector完成后调用的回调接口，一般通过实现这个接口来做数据统计
 */
public interface ApiConnectorStatusListener {
	void onFinish(int resultCode, int dataLength, long timeConsume, String url);
}

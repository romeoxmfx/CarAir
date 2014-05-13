package com.android.carair.api;

/**
 * car air 请求协议包实体
 * @author jhw
 *
 */
public class RespProtocolPacket {

	private String status;
	private Message message;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Message getRespMessage() {
		return message;
	}
	public void setRespMessage(Message respMessage) {
		this.message = respMessage;
	}
	
}

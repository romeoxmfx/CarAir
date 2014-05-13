package com.android.carair.api;

/**
 * car air 请求协议包实体
 * @author jhw
 *
 */
public class ReqProtocolPacket{

	private String cmd;
	private Message message;
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public Message getReqMessage() {
		return message;
	}
	public void setReqMessage(Message reqMessage) {
		this.message = reqMessage;
	}
}

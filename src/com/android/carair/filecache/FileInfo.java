package com.android.carair.filecache;

public interface FileInfo{
	public String getFileName();				//获取文件名
	public void setFileName(String fileName);	//设置文件名
	public long getFileInfoPos();				//获取文件信息写入偏移
	public void setFileInfoPos(long pos);		//设置文件信息写入地址
	public long getFileInfoTime(long currentTime);	//获取fileinfo内时间相关的信息，如果不存在，则返回默认值defaultTime
	//组装文件信息字符串，此接口返回的文件信息字符串长度必须保持一致
	public byte[] composeFileInfoStr();			
	
	/*
	 * 文件信息失效
	 * 调用此接口后,通过composeFileInfoStr接口获取的文件信息无效，但文件信息长度必须保持一致。
	 */
	public void invalidate();					
}

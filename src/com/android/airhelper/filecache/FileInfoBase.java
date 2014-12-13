package com.android.airhelper.filecache;

import java.io.UnsupportedEncodingException;

public class FileInfoBase implements Comparable<FileInfoBase>,FileInfo{
	
	public static final char DIVISION = '-';
	public static final char PARTITION = '_';
	
	
	private String fileName;		//文件名
	private long lastAccess = 0;		//最近访问时间
	//private long lastModify = 0;		//最近修改时间
	//private int accessedTime = 0;		//访问次数
	private long iofoPos;
	private boolean validate = true;
	@Override
	public int compareTo(FileInfoBase another) {
		// TODO Auto-generated method stub
		if(this == another)
			return 0;
		if(lastAccess > ((FileInfoBase)another).lastAccess)
			return 1;
		else
			return -1;
//		return lastAccess.compareTo(((FileInfoBase)another).lastAccess);
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public long getLastAccess(){
		return lastAccess;
	}
	
//	public long getLastModify(){
//		return lastModify;
//	}
	
	public void setFileName(String fileName){
		
		this.fileName = fileName;
	}
	
	public void SetLastAccess(long lastAccess){
		
		this.lastAccess = lastAccess;
	}
	
//	public void SetLastModify(long lastModify){
//		
//		this.lastModify = lastModify;
//	}
	
	/*
	 * 文件信息转byte数组
	 * 格式：访问时间_最近修改时间最近访问时间-文件名
	 */
	public byte[] composeFileInfoStr(){
		StringBuffer filePath = new StringBuffer();
//		if(lastModify != 0)
//			filePath.append(lastModify);
//		else
//			filePath.append("0000000000000");
		if(lastAccess != 0)
			filePath.append(lastAccess);
		else
			filePath.append("0000000000000");
		if(filePath.length()<13){
			int size = 13-filePath.length();
			for(int i=0;i<size;i++){
				filePath.insert(0, "0");
			}
		}
		if(validate)
			filePath.append(DIVISION);
		else
			filePath.append(PARTITION);
		filePath.append(fileName);
		try {
			return filePath.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public long getFileInfoPos() {
		// TODO Auto-generated method stub
		return iofoPos;
	}

	@Override
	public void setFileInfoPos(long pos) {
		// TODO Auto-generated method stub
		iofoPos = pos;
	}


	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		validate = false;
	}

	@Override
	public long getFileInfoTime(long defaultTime) {
		// TODO Auto-generated method stub
		return lastAccess;
	}
	
}

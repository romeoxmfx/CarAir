package com.android.carair.filecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileBuffer {
	
	private int capacity = 0;
	private int contentLen = 0;
//	private FileOutputStream fOs;
//	private FileInputStream fIn;
	private FileChannel fileChannel;
	//private FileChannel readChannel;
	private ReentrantReadWriteLock readWriteLock;
	private int[] blockOffset;
	private int blockLen;
	private boolean validate;
	
	protected FileBuffer(int[] blockOffset,int blockLen,FileChannel fileChannel){
		this.capacity = blockOffset.length*blockLen;
		this.blockLen = blockLen;
		this.blockOffset = blockOffset;
		this.fileChannel = fileChannel;
		validate = true;
		readWriteLock = new ReentrantReadWriteLock();
	}
	public int capacity(){
		return capacity;
	}
	
	/*
	 * 读取缓存内容
	 */
	public byte[] read(){
		//读取锁
		readWriteLock.readLock().lock();
		try{
			//分配内存
			if(!validate)
				return null;
			int readLen = contentLen>capacity ? capacity:contentLen;
			byte[] data = new byte[readLen];
			int i = 0;
			try {
				
				//读取数据
				while(readLen>0){
					//分段读入数据
					ByteBuffer buffer;
					if(readLen >= blockLen)
						buffer = ByteBuffer.wrap(data, i*blockLen, blockLen);
					else
						buffer = ByteBuffer.wrap(data, i*blockLen, readLen);
					fileChannel.read(buffer, blockOffset[i]);
					readLen -= blockLen;
					i++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} 
			return data;
		}finally{
			readWriteLock.readLock().unlock();
		}
		
	}
	
	/*
	 * 写入缓存内容
	 */
	public boolean write(byte[] data) throws OverFlowException{
		readWriteLock.writeLock().lock();
		
		try{
			if(!validate)
				return false;
			//写入数据过长，抛出异常
			if(data.length > capacity)
				throw new OverFlowException("write data is too large. data length is "+data.length+". buffer capacity is "+capacity);
			//初始化文件长度
			contentLen = 0;
			int writeLen = data.length;
			int i = 0;
			
			try {
				
				//写入数据
				while(writeLen>0){
					//分段写入数据
					ByteBuffer buffer;
					if(writeLen >= blockLen)
						buffer = ByteBuffer.wrap(data, i*blockLen, blockLen);
					else
						buffer = ByteBuffer.wrap(data, i*blockLen, writeLen);
					fileChannel.write(buffer, blockOffset[i]);
					writeLen -= blockLen;
					i++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} 
			contentLen = data.length;
			return true;
		}finally{
			readWriteLock.writeLock().unlock();
		}
	}
	

	@SuppressWarnings("serial")
	public class OverFlowException extends Exception{

		public OverFlowException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public OverFlowException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
			// TODO Auto-generated constructor stub
		}

		public OverFlowException(String detailMessage) {
			super(detailMessage);
			// TODO Auto-generated constructor stub
		}

		public OverFlowException(Throwable throwable) {
			super(throwable);
			// TODO Auto-generated constructor stub
		}
		
	}
	/*
	 * 获取存储块信息
	 */
	protected int[] getBlockOffset(){
		return blockOffset;
	}
	
	protected int getBlockLen(){
		return blockLen;
	}
	
	/*
	 * 获取缓存文件信息
	 */
	protected FileChannel getFileChannel(){
		return fileChannel;
	}
	
//	protected FileChannel getFileIs(){
//		return readChannel;
//	}
	
	protected void invalidate(){
		fileChannel = null;
		//writeChannel = null;
		validate = false;
	}
}

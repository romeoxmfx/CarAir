package com.android.carair.filecache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.android.carair.filecache.FileBuffer.OverFlowException;



public class FileBufferPool {
	
	

	//默认sdcard缓存大小
	private static int SD_MAX_CAPACITY = 1024*1024*10;
	//默认手机缓存大小
	private static int PHONE_MAX_CAPACITY = 1024*1024*5;
	//默认块大小
	private static int BLOCK_SIZE = 256;
	
	private RandomAccessFile fOs;
	private FileInputStream fIn;
	private FileChannel fileChannel;
	//private FileChannel readChannel;
	private boolean init = false;
	private String fileName;
	private boolean isInSdcard;
	private int maxCapacity;
	private int blockSize;
	private BlockStateMgr stateMgr;
	private HashMap<String,FileBuffer>  storedPool;
	private ReentrantReadWriteLock readWriteLock;
	
	protected FileBufferPool(String fileName,Boolean sdcard){
		this.fileName = fileName;
		isInSdcard = sdcard;
		readWriteLock = new ReentrantReadWriteLock();
	}

	public boolean isInSdcard(){
		return isInSdcard;
	}
	/*
	 * 读取数据
	 */
	public byte[] read(String key){
		if(init){
			readWriteLock.readLock().lock();
			try{
				//是否存在该数据
				if(storedPool.containsKey(key)){
					//存在
					FileBuffer buffer = storedPool.get(key);
					return buffer.read();
				}else
					return null;
			}finally{
				readWriteLock.readLock().unlock();
			}
		}
		return null;
	}
	
	/*
	 * 写入数据，
	 * 提供更新能力
	 */
	public boolean write(String key,byte[] data){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				//是否存在该数据
				if(storedPool.containsKey(key)){
					//存在
					FileBuffer buffer = storedPool.get(key);
					if(buffer.capacity() >= data.length)//可以容纳新数据
						return buffer.write(data);
					else{
						//不能容纳新数据，则重新分配
						FileBuffer buffer1 = falloc(data.length);
						//分配成功
						if(buffer1 != null){
							if(buffer1.write(data)){
								//写入成功
								//回收前缓存
								ffree(buffer);
								//保存当前缓存
								storedPool.put(key, buffer1);
								return true;
							}else{
								//写入失败
								//回收内存
								ffree(buffer1);
								return false;
							}
							
						}else
							return false;
					}
				}else{
					//不存在
					FileBuffer buffer = falloc(data.length);
					//分配成功
					if(buffer != null){
						if(buffer.write(data)){
							//写入成功
							//保存当前缓存
							storedPool.put(key, buffer);
							return true;
						}else{
							//写入失败
							//回收内存
							ffree(buffer);
							return false;
						}
							
					}else
						return false;
				}
					
			}catch(OverFlowException e){
				e.printStackTrace();
			}finally{
			
				readWriteLock.writeLock().unlock();
			}
		}
		return false;
	}
	public boolean erase(String key){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				//存在
				if(storedPool.containsKey(key)){
					FileBuffer buffer = storedPool.get(key);
					storedPool.remove(key);
					ffree(buffer);
					return true;
				}else
					return false;
			}finally{
				readWriteLock.writeLock().unlock();
			}
		}
		return false;
		
	}
	/**
	 * 清除缓存
	 */
	public void clear(){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				//回收缓存块
				Object[] buffers = storedPool.values().toArray();
				for(int i=0;i<buffers.length;i++){
					FileBuffer buffer = (FileBuffer) buffers[i];
					ffree(buffer);
				}
				storedPool.clear();
				//清除文件
				try {
					fileChannel.truncate(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}finally{
				readWriteLock.writeLock().unlock();
			}
		}
	}
	
	public synchronized boolean init(int maxCapacity,int blockSize){
		if(!init){
			File cacheFile = new File(fileName);
			cacheFile.getParentFile().mkdirs();
			storedPool = new HashMap<String,FileBuffer>();
			//创建缓存文件
			if(!cacheFile.exists())
				try {
					cacheFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
			
//			//打开写入IO
//			try {
//				fIn = new FileInputStream(cacheFile.getAbsolutePath());
//				readChannel = fIn.getChannel();
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//				return false;
//			}
			
			//打开读取IO
			try {
				fOs = new RandomAccessFile(cacheFile.getAbsolutePath(),"rw");
				fileChannel = fOs.getChannel();
				fileChannel.truncate(0);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			//设置文件缓存容量
			if(maxCapacity == 0)
				if(isInSdcard)
					this.maxCapacity = SD_MAX_CAPACITY;
				else
					this.maxCapacity = PHONE_MAX_CAPACITY;
			else
				this.maxCapacity = maxCapacity;
			
			//设置文件缓存块大小
			if(blockSize == 0)
				this.blockSize = BLOCK_SIZE;
			else
				this.blockSize = blockSize;
			
			//创建状态管理器
			stateMgr = new BlockStateMgr(maxCapacity/blockSize);
			
			init = true;
		}
		return true;
	}
	
	/*
	 * 析构，清理资源
	 */
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		if(fOs != null)
			try{
				fOs.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		if(fileChannel != null)
			try{
				fileChannel.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		if(fIn != null)
			try{
				fIn.close();
			}catch(Exception e){
				e.printStackTrace();
			}
//		if(readChannel != null)
//			try{
//				readChannel.close();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
		super.finalize();
	}
	
	/*
	 * 分配文件缓存块
	 */
	protected FileBuffer falloc(int size){
		if(init){
			if(size > maxCapacity)
				return null;
			//计算需要文件缓存块的数量
			int blockNum = size/blockSize;
			if(size%blockSize != 0)
				blockNum++;
			
			int[] blockOffset = new int[blockNum];
			int maxBlockNum = stateMgr.getBlockNum();
			int j = 0;
			//循环查找空闲块
			for(int i=0;i<maxBlockNum;i++){
				if(stateMgr.getBlockState(i)){//块可用
					blockOffset[j] = i*blockSize;
					j++;
					//查询完成
					if(j == blockOffset.length)
						break;
				}
			}
			//分配成功
			if(j == blockOffset.length){
				//占用分配的块
				for(int i=0;i<blockOffset.length;i++)
					stateMgr.setBlockState(false, blockOffset[i]/blockSize);
				return new FileBuffer(blockOffset,blockSize,fileChannel);
			}
		}
		return null;
	}
	
	/*
	 * 释放文件缓存块
	 */
	protected void ffree(FileBuffer buffer){
		if(init){
			//判断该缓存是否属于该文件缓存池
			if(buffer.getFileChannel() != fileChannel || buffer.getBlockLen() != blockSize)
				return;
			//归还缓存块
			int[] blockOffset = buffer.getBlockOffset();
			for(int i=0;i<blockOffset.length;i++){
				stateMgr.setBlockState(true, blockOffset[i]/blockSize);
			}
			buffer.invalidate();
		}
	}
	
	class BlockStateMgr{
		
		//块数量
		private int blockNum;
		//块状态
		private byte[] blockStates;
		
		protected BlockStateMgr(int blockNum){
			this.blockNum = blockNum;
			//计算存储状态所需的byte数
			int byteNum = blockNum/8;
			//多余的位数
			int otioseBit = 8-blockNum%8;
			
			if(otioseBit != 0)
				byteNum++;
			blockStates = new byte[byteNum];
			
			for(int i = 0;i<byteNum;i++)
				blockStates[i] = 0;
			
			//置多余位为1
			if(otioseBit != 0){
				blockStates[byteNum-1] = 1;
				for(int j = 0;j<otioseBit;j++)
					blockStates[byteNum-1] *= 2;
				blockStates[byteNum-1]--;
			}
			
		}
		
		public int getBlockNum(){
			return blockNum;
		}
		
		/*
		 * 获取当前位是否被占用
		 * true 未占用
		 * false 占用
		 */
		public boolean getBlockState(int blockIndex){
			
			if(blockIndex > blockNum || blockIndex < 0)
				return false;
			//定位存储状态的byte位置
			int byteIndex = blockIndex/8;
			int redundantBit = blockIndex%8;

			byte state = blockStates[byteIndex];
			//获取状态位的过滤数
			byte mask = 1;
			for(int i=0;i<7-redundantBit;i++)
				mask *= 2;
			return (state&mask) == 0;
			
		}
		
		/*
		 * 设置当前位是否被占用
		 * true 未占用
		 * false 占用
		 */
		public void setBlockState(boolean state,int blockIndex){
			if(blockIndex > blockNum || blockIndex < 0)
				return;
			//定位存储状态的byte位置
			int byteIndex = blockIndex/8;
			int redundantBit = blockIndex%8;
			
			//获取状态位的过滤数
			byte mask = 1;

			for(int i=0;i<7-redundantBit;i++)
				mask *= 2;
			if(state)//设置位为未占用
				blockStates[byteIndex] &= ~mask;
			else//设置位为占用
				blockStates[byteIndex] |= mask;
			
		}
		
	}
	
	
}
